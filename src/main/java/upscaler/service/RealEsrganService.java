package upscaler.service;

import upscaler.model.ModelDefinition;
import upscaler.model.ScalePlan;
import upscaler.model.UpscaleRequest;
import upscaler.model.UpscaleResult;
import upscaler.runtime.BundledRuntimeManager;
import upscaler.runtime.PreparedRuntime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RealEsrganService {
    private final BundledRuntimeManager runtimeManager;
    private final ModelRegistry modelRegistry;
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);

    public RealEsrganService(BundledRuntimeManager runtimeManager, ModelRegistry modelRegistry) {
        this.runtimeManager = runtimeManager;
        this.modelRegistry = modelRegistry;
    }

    public UpscaleResult upscale(UpscaleRequest request, UpscaleProgressListener listener)
            throws IOException, InterruptedException, UpscaleCancelledException {
        Objects.requireNonNull(request);
        Objects.requireNonNull(listener);

        cancellationRequested.set(false);
        PreparedRuntime runtime = runtimeManager.prepareRuntime();
        ModelDefinition model = request.model();
        Path modelDirectory = modelRegistry.resolveModelDirectory(model, runtime);
        ensureModelFiles(model, modelDirectory);

        Path workspace = Files.createTempDirectory(runtime.rootDirectory(), "job-");
        List<String> history = new ArrayList<>();

        try {
            BufferedImage originalImage = ImageIO.read(request.inputFile().toFile());
            if (originalImage == null) {
                throw new IOException("The source image is unreadable.");
            }
            Path currentInput = workspace.resolve("input-0.png");
            Files.copy(request.inputFile(), currentInput, StandardCopyOption.REPLACE_EXISTING);

            List<Integer> passes = request.scalePlan().passes();
            for (int index = 0; index < passes.size(); index++) {
                checkCancelled();
                int factor = passes.get(index);
                int passNumber = index + 1;
                Path currentOutput = workspace.resolve("output-" + passNumber + ".png");
                listener.onStatus("Pass " + passNumber + "/" + passes.size() + "  •  scale x" + factor, passNumber, passes.size());
                runPass(runtime, currentInput, currentOutput, request, modelDirectory, factor, history, listener);
                currentInput = currentOutput;
            }

            BufferedImage image = ImageIO.read(currentInput.toFile());
            if (image == null) {
                throw new IOException("Real-ESRGAN finished but produced an unreadable image.");
            }
            Path outputPath = currentInput;
            if (request.scalePlan().requiresPostResize()) {
                int targetWidth = Math.max(1, originalImage.getWidth() * request.scalePlan().targetScale());
                int targetHeight = Math.max(1, originalImage.getHeight() * request.scalePlan().targetScale());
                listener.onStatus("Post-resizing to exact x" + request.scalePlan().targetScale(), passes.size(), passes.size());
                image = ImageResizer.resize(image, targetWidth, targetHeight);
                outputPath = workspace.resolve("output-final.png");
                ImageIO.write(image, "png", outputPath.toFile());
                history.add("$ internal-resize " + targetWidth + "x" + targetHeight);
            }
            listener.onStatus("Upscale complete", passes.size(), passes.size());
            return new UpscaleResult(outputPath, image, List.copyOf(history));
        } finally {
            currentProcess.set(null);
        }
    }

    public Path upscaleSequence(
            Path inputDirectory,
            ModelDefinition model,
            ScalePlan scalePlan,
            String engineGpuId,
            int tileSize,
            String threadProfile,
            boolean ttaEnabled,
            UpscaleProgressListener listener,
            java.util.function.Consumer<String> logConsumer
    ) throws IOException, InterruptedException, UpscaleCancelledException {
        Objects.requireNonNull(inputDirectory);
        Objects.requireNonNull(model);
        Objects.requireNonNull(scalePlan);
        Objects.requireNonNull(listener);
        Objects.requireNonNull(logConsumer);

        cancellationRequested.set(false);
        PreparedRuntime runtime = runtimeManager.prepareRuntime();
        Path modelDirectory = modelRegistry.resolveModelDirectory(model, runtime);
        ensureModelFiles(model, modelDirectory);

        Path workspace = Files.createTempDirectory(runtime.rootDirectory(), "sequence-");
        Path currentInput = inputDirectory;
        Path currentOutput = inputDirectory;
        List<String> history = new ArrayList<>();

        try {
            List<Integer> passes = scalePlan.passes();
            for (int index = 0; index < passes.size(); index++) {
                checkCancelled();
                int factor = passes.get(index);
                int passNumber = index + 1;
                currentOutput = workspace.resolve("pass-" + passNumber);
                Files.createDirectories(currentOutput);
                listener.onStatus("Frame pass " + passNumber + "/" + passes.size() + "  •  scale x" + factor, passNumber, passes.size());
                runPass(
                        runtime,
                        currentInput,
                        currentOutput,
                        model.name(),
                        modelDirectory,
                        factor,
                        engineGpuId,
                        tileSize,
                        threadProfile,
                        ttaEnabled,
                        history,
                        logConsumer
                );
                currentInput = currentOutput;
            }
            listener.onStatus("Frame sequence upscale complete", passes.size(), passes.size());
            return currentOutput;
        } finally {
            currentProcess.set(null);
        }
    }

    public void cancelCurrentJob() {
        cancellationRequested.set(true);
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

    private void runPass(
            PreparedRuntime runtime,
            Path input,
            Path output,
            UpscaleRequest request,
            Path modelDirectory,
            int scaleFactor,
            List<String> history,
            UpscaleProgressListener listener
    ) throws IOException, InterruptedException, UpscaleCancelledException {
        List<String> command = buildCommand(
                runtime,
                input,
                output,
                request.model().name(),
                modelDirectory,
                scaleFactor,
                request.engineGpuId(),
                request.tileSize(),
                request.threadProfile(),
                request.ttaEnabled()
        );
        history.add("$ " + String.join(" ", command));

        Process process = new ProcessBuilder(command)
                .directory(runtime.rootDirectory().toFile())
                .redirectErrorStream(true)
                .start();
        currentProcess.set(process);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
                listener.onLog(line);
                if (cancellationRequested.get()) {
                    cancelCurrentJob();
                }
            }
        }

        int exitCode = process.waitFor();
        currentProcess.compareAndSet(process, null);
        if (cancellationRequested.get()) {
            throw new UpscaleCancelledException("Upscale cancelled by user.");
        }
        if (exitCode != 0) {
            throw new IOException("Real-ESRGAN exited with code " + exitCode + ". Check diagnostics for details.");
        }
        if (!Files.exists(output)) {
            throw new IOException("Real-ESRGAN finished without creating the output file.");
        }
    }

    private void runPass(
            PreparedRuntime runtime,
            Path input,
            Path output,
            String modelName,
            Path modelDirectory,
            int scaleFactor,
            String engineGpuId,
            int tileSize,
            String threadProfile,
            boolean ttaEnabled,
            List<String> history,
            java.util.function.Consumer<String> logConsumer
    ) throws IOException, InterruptedException, UpscaleCancelledException {
        List<String> command = buildCommand(
                runtime,
                input,
                output,
                modelName,
                modelDirectory,
                scaleFactor,
                engineGpuId,
                tileSize,
                threadProfile,
                ttaEnabled
        );
        history.add("$ " + String.join(" ", command));

        Process process = new ProcessBuilder(command)
                .directory(runtime.rootDirectory().toFile())
                .redirectErrorStream(true)
                .start();
        currentProcess.set(process);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
                logConsumer.accept(line);
                if (cancellationRequested.get()) {
                    cancelCurrentJob();
                }
            }
        }

        int exitCode = process.waitFor();
        currentProcess.compareAndSet(process, null);
        if (cancellationRequested.get()) {
            throw new UpscaleCancelledException("Upscale cancelled by user.");
        }
        if (exitCode != 0) {
            throw new IOException("Real-ESRGAN exited with code " + exitCode + ". Check diagnostics for details.");
        }
        if (Files.isDirectory(output)) {
            try (var stream = Files.list(output)) {
                if (stream.findAny().isEmpty()) {
                    throw new IOException("Real-ESRGAN finished without generating any upscaled frames.");
                }
            }
        } else if (!Files.exists(output)) {
            throw new IOException("Real-ESRGAN finished without creating the output file.");
        }
    }

    private List<String> buildCommand(
            PreparedRuntime runtime,
            Path input,
            Path output,
            String modelName,
            Path modelDirectory,
            int scaleFactor,
            String engineGpuId,
            int tileSize,
            String threadProfile,
            boolean ttaEnabled
    ) {
        List<String> command = new ArrayList<>();
        command.add(runtime.executable().toString());
        command.add("-i");
        command.add(input.toString());
        command.add("-o");
        command.add(output.toString());
        command.add("-n");
        command.add(modelName);
        command.add("-s");
        command.add(Integer.toString(scaleFactor));
        command.add("-m");
        command.add(modelDirectory.toString());
        command.add("-t");
        command.add(Integer.toString(tileSize));
        command.add("-j");
        command.add(threadProfile);
        command.add("-f");
        command.add("png");
        if (engineGpuId != null && !engineGpuId.isBlank()) {
            command.add("-g");
            command.add(engineGpuId);
        }
        if (ttaEnabled) {
            command.add("-x");
        }
        command.add("-v");
        return command;
    }

    private void ensureModelFiles(ModelDefinition model, Path modelDirectory) throws IOException {
        if (!Files.exists(modelDirectory.resolve(model.name() + ".bin")) || !Files.exists(modelDirectory.resolve(model.name() + ".param"))) {
            throw new IOException("Model files are missing for " + model.displayName() + " in " + modelDirectory);
        }
    }

    private void checkCancelled() throws UpscaleCancelledException {
        if (cancellationRequested.get()) {
            throw new UpscaleCancelledException("Upscale cancelled by user.");
        }
    }
}
