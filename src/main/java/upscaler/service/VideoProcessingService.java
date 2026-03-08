package upscaler.service;

import upscaler.config.AppDirectories;
import upscaler.model.VideoEncoderOption;
import upscaler.model.VideoMetadata;
import upscaler.model.VideoQualityPreset;
import upscaler.model.VideoUpscaleRequest;
import upscaler.model.VideoUpscaleResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class VideoProcessingService {
    private final VideoMetadataService metadataService;
    private final VideoToolingService toolingService;
    private final RealEsrganService upscaleService;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();

    public VideoProcessingService(VideoMetadataService metadataService, VideoToolingService toolingService, RealEsrganService upscaleService) {
        this.metadataService = metadataService;
        this.toolingService = toolingService;
        this.upscaleService = upscaleService;
    }

    public VideoUpscaleResult process(VideoUpscaleRequest request, VideoProgressListener listener)
            throws IOException, InterruptedException, UpscaleCancelledException {
        cancellationRequested.set(false);
        toolingService.requireVideoTools();
        AppDirectories.ensureLayout();

        VideoMetadata metadata = metadataService.probe(request.inputFile());
        ensureInterpolationSupport(request, metadata);
        Path workspace = Files.createTempDirectory(AppDirectories.appHome(), "video-job-");
        Path extractedFramesDir = workspace.resolve("frames-src");
        Path finalFramesDir = extractedFramesDir;
        Files.createDirectories(extractedFramesDir);

        try {
            listener.onStage("Extracting video frames", 0);
            extractFrames(request, metadata, extractedFramesDir, listener);
            checkCancelled();

            if (request.scaleFactor() > 1) {
                listener.onStage("Upscaling extracted frames", 10);
                finalFramesDir = upscaleService.upscaleSequence(
                        extractedFramesDir,
                        request.model(),
                        request.scalePlan(),
                        request.engineGpuId(),
                        request.tileSize(),
                        request.threadProfile(),
                        request.ttaEnabled(),
                        new UpscaleProgressListener() {
                            @Override
                            public void onStatus(String message, int currentPass, int totalPasses) {
                                listener.onStage(
                                        message,
                                        10 + Math.round(((currentPass - 1) * 70f) / Math.max(1, totalPasses))
                                );
                            }

                            @Override
                            public void onLog(String line) {
                                listener.onLog(line);
                            }
                        },
                        line -> {
                            if (line.matches("\\d+(\\.\\d+)?%")) {
                                double percent = Double.parseDouble(line.replace("%", ""));
                                listener.onStage("Upscaling frames", 10 + (int) Math.round((percent / 100d) * 70d));
                            } else {
                                listener.onLog(line);
                            }
                        }
                );
            }

            checkCancelled();
            listener.onStage("Encoding final video", 82);
            encodeVideo(request, metadata, finalFramesDir, listener);
            listener.onStage("Render complete", 100);

            int outputWidth = request.scaleFactor() <= 1 ? metadata.width() : metadata.width() * request.scaleFactor();
            int outputHeight = request.scaleFactor() <= 1 ? metadata.height() : metadata.height() * request.scaleFactor();
            int outputFps = request.targetFrameRate() <= 0 ? (int) Math.round(metadata.frameRate()) : request.targetFrameRate();
            return new VideoUpscaleResult(request.outputFile(), metadata, outputWidth, outputHeight, outputFps);
        } finally {
            currentProcess.set(null);
            deleteDirectoryQuietly(workspace);
        }
    }

    public void cancelCurrentJob() {
        cancellationRequested.set(true);
        upscaleService.cancelCurrentJob();
        Process process = currentProcess.getAndSet(null);
        if (process != null) {
            process.descendants().forEach(handle -> {
                try {
                    handle.destroy();
                } catch (Exception ignored) {
                }
            });
            process.destroy();
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private void extractFrames(VideoUpscaleRequest request, VideoMetadata metadata, Path framesDir, VideoProgressListener listener)
            throws IOException, InterruptedException, UpscaleCancelledException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(request.inputFile().toString());
        command.add("-map");
        command.add("0:v:0");
        command.add("-vf");
        command.add("fps=" + formatFps(metadata.frameRate()));
        command.add("-progress");
        command.add("pipe:1");
        command.add("-nostats");
        command.add(framesDir.resolve("%08d.png").toString());

        runFfmpeg(command, metadata.durationSeconds(), 0, 10, "Extracting frames", listener);
    }

    private void encodeVideo(VideoUpscaleRequest request, VideoMetadata metadata, Path framesDir, VideoProgressListener listener)
            throws IOException, InterruptedException, UpscaleCancelledException {
        Path outputParent = request.outputFile().toAbsolutePath().getParent();
        if (outputParent != null) {
            Files.createDirectories(outputParent);
        }
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-framerate");
        command.add(formatFps(metadata.frameRate()));
        command.add("-i");
        command.add(framesDir.resolve("%08d.png").toString());
        command.add("-i");
        command.add(request.inputFile().toString());
        command.add("-map");
        command.add("0:v:0");
        command.add("-map");
        command.add("1:a?");

        String filter = buildVideoFilter(metadata, request);
        if (!filter.isBlank()) {
            command.add("-vf");
            command.add(filter);
        }

        VideoEncoderOption encoder = toolingService.resolveRequestedEncoder(request.videoEncoderId());
        if ("libx264".equals(encoder.id())) {
            VideoQualityPreset preset = request.qualityPreset();
            command.add("-c:v");
            command.add("libx264");
            command.add("-preset");
            command.add(preset.x264Preset());
            command.add("-crf");
            command.add(Integer.toString(preset.crf()));
            command.add("-pix_fmt");
            command.add("yuv420p");
        } else if ("mpeg4".equals(encoder.id())) {
            command.add("-c:v");
            command.add("mpeg4");
            command.add("-q:v");
            command.add("2");
        } else {
            command.add("-c:v");
            command.add(encoder.id());
            command.add("-pix_fmt");
            command.add("yuv420p");
            applyHardwareEncoderTuning(command, encoder, request.qualityPreset());
        }

        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("192k");
        command.add("-movflags");
        command.add("+faststart");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-nostats");
        command.add(request.outputFile().toString());

        runFfmpeg(command, metadata.durationSeconds(), 82, 18, "Encoding video", listener);
    }

    private void ensureInterpolationSupport(VideoUpscaleRequest request, VideoMetadata metadata) throws IOException {
        if (request.targetFrameRate() > metadata.frameRate() && !toolingService.supportsMotionInterpolation()) {
            throw new IOException("The local FFmpeg build does not expose the minterpolate filter required for motion interpolation.");
        }
    }

    private String buildVideoFilter(VideoMetadata metadata, VideoUpscaleRequest request) {
        List<String> filters = new ArrayList<>();
        if (request.scaleFactor() > 1 && request.scalePlan().requiresPostResize()) {
            filters.add(buildScaleFilter(request.scalePlan().postResizeFactor()));
        }

        int targetFps = request.targetFrameRate();
        if (targetFps <= 0) {
            return String.join(",", filters);
        }
        if (metadata.frameRate() <= 0) {
            filters.add("fps=" + targetFps);
            return String.join(",", filters);
        }
        if (targetFps > metadata.frameRate()) {
            filters.add("minterpolate=fps=" + targetFps + ":mi_mode=mci:mc_mode=aobmc:vsbmc=1");
        } else if (targetFps < metadata.frameRate()) {
            filters.add("fps=" + targetFps);
        }
        return String.join(",", filters);
    }

    private String buildScaleFilter(double ratio) {
        return "scale=iw*" + formatRatio(ratio) + ":ih*" + formatRatio(ratio) + ":flags=lanczos";
    }

    private String formatRatio(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private void runFfmpeg(
            List<String> command,
            double durationSeconds,
            int phaseStart,
            int phaseSpan,
            String stageName,
            VideoProgressListener listener
    ) throws IOException, InterruptedException, UpscaleCancelledException {
        listener.onLog("$ " + String.join(" ", command));
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        currentProcess.set(process);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("out_time_ms=")) {
                    long micros = parseLong(line.substring("out_time_ms=".length()), 0L);
                    if (durationSeconds > 0) {
                        double seconds = micros / 1_000_000d;
                        double phasePercent = Math.max(0d, Math.min(1d, seconds / durationSeconds));
                        int overall = phaseStart + (int) Math.round(phasePercent * phaseSpan);
                        listener.onStage(stageName, Math.min(100, overall));
                    }
                } else if (line.startsWith("progress=end")) {
                    listener.onStage(stageName, Math.min(100, phaseStart + phaseSpan));
                } else if (!line.startsWith("progress=") && !line.startsWith("speed=") && !line.startsWith("bitrate=") && !line.startsWith("frame=") && !line.isBlank()) {
                    listener.onLog(line);
                }

                if (cancellationRequested.get()) {
                    cancelCurrentJob();
                }
            }
        }

        int exitCode = process.waitFor();
        currentProcess.compareAndSet(process, null);
        if (cancellationRequested.get()) {
            throw new UpscaleCancelledException("Video processing cancelled by user.");
        }
        if (exitCode != 0) {
            throw new IOException("ffmpeg exited with code " + exitCode + " during stage: " + stageName);
        }
    }

    private void checkCancelled() throws UpscaleCancelledException {
        if (cancellationRequested.get()) {
            throw new UpscaleCancelledException("Video processing cancelled by user.");
        }
    }

    private String formatFps(double fps) {
        double normalized = fps > 0 ? fps : 30d;
        return String.format(Locale.US, "%.6f", normalized);
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void applyHardwareEncoderTuning(List<String> command, VideoEncoderOption encoder, VideoQualityPreset preset) {
        String id = encoder.id();
        if (id.contains("nvenc")) {
            command.add("-preset");
            command.add(switch (preset) {
                case FAST -> "p3";
                case BALANCED -> "p5";
                case QUALITY -> "p6";
                case MAXIMUM -> "p7";
            });
            command.add("-cq");
            command.add(Integer.toString(switch (preset) {
                case FAST -> 24;
                case BALANCED -> 21;
                case QUALITY -> 19;
                case MAXIMUM -> 17;
            }));
            command.add("-b:v");
            command.add("0");
        } else if (id.contains("qsv")) {
            command.add("-global_quality");
            command.add(Integer.toString(switch (preset) {
                case FAST -> 26;
                case BALANCED -> 23;
                case QUALITY -> 21;
                case MAXIMUM -> 19;
            }));
        } else if (id.contains("videotoolbox")) {
            command.add("-q:v");
            command.add(Integer.toString(switch (preset) {
                case FAST -> 65;
                case BALANCED -> 70;
                case QUALITY -> 75;
                case MAXIMUM -> 80;
            }));
        }
    }

    private void deleteDirectoryQuietly(Path root) {
        if (root == null || Files.notExists(root)) {
            return;
        }
        try {
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
