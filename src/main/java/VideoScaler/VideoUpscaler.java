package VideoScaler;

import java.io.*;
import java.util.function.Consumer;

public class VideoUpscaler {

    public static void upscaleFromVideoFile(String inputVideoPath, String outputVideoPath, String modelName) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        String executablePath;

        if (os.contains("win")) {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan.exe";
        } else {
            executablePath = "src/main/java/Models/AI/REALESRGAN/realesrgan-ncnn-vulkan";
        }

        File execFile = new File(executablePath);
        if (!execFile.exists()) throw new FileNotFoundException("Binary not found: " + executablePath);
        if (!execFile.canExecute()) execFile.setExecutable(true);

        String workingDir = "src/main/java/Models/AI/REALESRGAN";

        File tempDir = new File(workingDir, "temp_upscale");
        File framesDir = new File(tempDir, "frames");
        File upscaledDir = new File(tempDir, "up_frames");

        framesDir.mkdirs();
        upscaledDir.mkdirs();

        // 1. 🎬 Extract frames using ffmpeg
        ProcessBuilder extractTask = new ProcessBuilder(
                "ffmpeg", "-i", inputVideoPath,
                new File(framesDir, "frame_%05d.png").getAbsolutePath()
        );
        extractTask.inheritIO();
        Process extract = extractTask.start();
        int extractCode = extract.waitFor();
        if (extractCode != 0) throw new RuntimeException("Frame extraction failed!");

        // 2. 🧠 Upscale each frame via .exe
        File[] frames = framesDir.listFiles((dir, name) -> name.endsWith(".png"));
        if (frames == null || frames.length == 0)
            throw new IOException("No frames extracted");

        int total = frames.length;
        int processed = 0;

        for (File frame : frames) {
            File output = new File(upscaledDir, frame.getName());
            ProcessBuilder upscaleTask = new ProcessBuilder(
                    execFile.getAbsolutePath(),
                    "-i", frame.getAbsolutePath(),
                    "-o", output.getAbsolutePath(),
                    "-n", modelName
            );
            upscaleTask.directory(execFile.getParentFile());
            upscaleTask.redirectErrorStream(true);

            Process p = upscaleTask.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[RENDER] " + line);
                }
            }

            int exitCode = p.waitFor();
            if (exitCode != 0) throw new RuntimeException("Upscale failed for: " + frame.getName());

            processed++;
            int percent = (int)((processed / (double)total) * 100);
            System.out.println("Progress: " + percent + "%");
        }

        // 3. 🎞️ Combine into video
        ProcessBuilder combineTask = new ProcessBuilder(
                "ffmpeg", "-framerate", "24", "-i",
                new File(upscaledDir, "frame_%05d.png").getAbsolutePath(),
                "-c:v", "libx264", "-pix_fmt", "yuv420p",
                "-y",
                outputVideoPath
        );
        combineTask.inheritIO();
        Process combine = combineTask.start();
        int combCode = combine.waitFor();
        if (combCode != 0) throw new IOException("Failed to recombine frames.");
    }
}