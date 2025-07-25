package VideoScaler;

import java.io.*;
import java.util.function.Consumer;

public class VideoUpscaler {

    public static void upscaleVideo(String inputVideoPath, String outputVideoPath, String modelName, Consumer<Integer> progressCallback)
            throws IOException, InterruptedException {

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

        // 1. 🎬 Extract frames
        ProcessBuilder extractTask = new ProcessBuilder(
                "ffmpeg", "-i", inputVideoPath,
                new File(framesDir, "frame_%05d.png").getAbsolutePath()
        );
        extractTask.inheritIO();
        Process extract = extractTask.start();
        extract.waitFor();

        // 2. 🧠 Upscale each frame
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
            upscaleTask.directory(new File(workingDir));
            upscaleTask.redirectErrorStream(true);
            Process upscale = upscaleTask.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(upscale.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Upscale] " + line);
                }
            }

            int exit = upscale.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Failed upscale: " + frame.getName());
            }

            processed++;
            int percent = (int) ((processed / (double) total) * 100);
            progressCallback.accept(percent);
        }

        // 3. 🎞️ Combine back
        ProcessBuilder combine = new ProcessBuilder(
                "ffmpeg", "-framerate", "24", "-i",
                new File(upscaledDir, "frame_%05d.png").getAbsolutePath(),
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-y",
                outputVideoPath
        );
        combine.inheritIO();
        Process p3 = combine.start();
        p3.waitFor();

        // 4. 🧹 Optional: Clean up frames (если хочешь)
        // for (File f : framesDir.listFiles()) f.delete();
        // for (File f : upscaledDir.listFiles()) f.delete();
    }
}