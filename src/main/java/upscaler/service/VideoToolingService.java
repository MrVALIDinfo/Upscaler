package upscaler.service;

import upscaler.model.VideoEncoderOption;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VideoToolingService {
    private static final List<VideoEncoderOption> PREFERRED_ENCODERS = List.of(
            new VideoEncoderOption("AUTO", "Auto (Stable CPU)", "Prefer the most reliable bundled path. Defaults to libx264 when available.", false),
            new VideoEncoderOption("libx264", "H.264 CPU", "Best compatibility and the most predictable quality/performance balance.", false),
            new VideoEncoderOption("h264_nvenc", "NVIDIA NVENC H.264", "Hardware encoder for NVIDIA GPUs. Fastest path when supported by the local FFmpeg build.", true),
            new VideoEncoderOption("hevc_nvenc", "NVIDIA NVENC HEVC", "Hardware HEVC encoder for NVIDIA GPUs. Smaller files, slower decode on older devices.", true),
            new VideoEncoderOption("h264_qsv", "Intel Quick Sync H.264", "Hardware encoder for Intel iGPU / media engines when FFmpeg exposes QSV.", true),
            new VideoEncoderOption("hevc_qsv", "Intel Quick Sync HEVC", "Intel hardware HEVC path when available.", true),
            new VideoEncoderOption("h264_amf", "AMD AMF H.264", "Hardware encoder for AMD GPUs when FFmpeg is built with AMF support.", true),
            new VideoEncoderOption("hevc_amf", "AMD AMF HEVC", "AMD hardware HEVC path when available.", true),
            new VideoEncoderOption("h264_videotoolbox", "Apple VideoToolbox H.264", "Hardware encoder for Apple Silicon / macOS media engines.", true),
            new VideoEncoderOption("hevc_videotoolbox", "Apple VideoToolbox HEVC", "Apple hardware HEVC path when available.", true),
            new VideoEncoderOption("mpeg4", "MPEG-4 Fallback", "Fallback encoder for minimal FFmpeg builds without libx264.", false)
    );

    public boolean isFfmpegAvailable() {
        return commandSucceeds("ffmpeg", "-version");
    }

    public boolean isFfprobeAvailable() {
        return commandSucceeds("ffprobe", "-version");
    }

    public boolean supportsMotionInterpolation() {
        String filters = commandOutput("ffmpeg", "-hide_banner", "-filters");
        return filters.toLowerCase(Locale.ROOT).contains("minterpolate");
    }

    public void requireVideoTools() throws IOException {
        List<String> missing = new ArrayList<>();
        if (!isFfmpegAvailable()) {
            missing.add("ffmpeg");
        }
        if (!isFfprobeAvailable()) {
            missing.add("ffprobe");
        }
        if (!missing.isEmpty()) {
            throw new IOException("Missing required video tools in PATH: " + String.join(", ", missing));
        }
    }

    public List<VideoEncoderOption> listAvailableEncoders() {
        Map<String, VideoEncoderOption> selected = new LinkedHashMap<>();
        selected.put("AUTO", PREFERRED_ENCODERS.getFirst());

        String encodersOutput = commandOutput("ffmpeg", "-hide_banner", "-encoders").toLowerCase(Locale.ROOT);
        for (VideoEncoderOption option : PREFERRED_ENCODERS) {
            if (option.isAuto()) {
                continue;
            }
            if (encodersOutput.contains(option.id().toLowerCase(Locale.ROOT))) {
                selected.put(option.id(), option);
            }
        }

        if (!selected.containsKey("libx264")) {
            selected.put("mpeg4", encoderById("mpeg4"));
        }
        return List.copyOf(selected.values());
    }

    public VideoEncoderOption resolveRequestedEncoder(String requestedId) {
        if (requestedId == null || requestedId.isBlank() || "AUTO".equalsIgnoreCase(requestedId)) {
            return autoEncoder();
        }

        List<VideoEncoderOption> available = listAvailableEncoders();
        for (VideoEncoderOption option : available) {
            if (option.id().equalsIgnoreCase(requestedId)) {
                return option;
            }
        }
        throw new IllegalArgumentException("Requested video encoder is not available in the current FFmpeg build: " + requestedId);
    }

    public String buildEnvironmentSummary() {
        List<VideoEncoderOption> encoders = listAvailableEncoders();
        String defaultEncoder = autoEncoder().displayName();
        return "ffmpeg=" + (isFfmpegAvailable() ? "ok" : "missing") +
                " • ffprobe=" + (isFfprobeAvailable() ? "ok" : "missing") +
                " • interpolation=" + (supportsMotionInterpolation() ? "minterpolate" : "missing") +
                " • encoders=" + Math.max(0, encoders.size() - 1) +
                " • default=" + defaultEncoder;
    }

    private VideoEncoderOption autoEncoder() {
        List<VideoEncoderOption> available = listAvailableEncoders();
        for (VideoEncoderOption option : available) {
            if ("libx264".equals(option.id())) {
                return option;
            }
        }
        return available.stream()
                .filter(option -> "mpeg4".equals(option.id()))
                .findFirst()
                .orElse(encoderById("mpeg4"));
    }

    private VideoEncoderOption encoderById(String id) {
        return PREFERRED_ENCODERS.stream()
                .filter(option -> option.id().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow();
    }

    private boolean commandSucceeds(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String commandOutput(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            process.waitFor(10, TimeUnit.SECONDS);
            return output.toString();
        } catch (Exception ignored) {
            return "";
        }
    }
}
