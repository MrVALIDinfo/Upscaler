package upscaler.service;

public interface UpscaleProgressListener {
    void onStatus(String message, int currentPass, int totalPasses);

    void onLog(String line);
}
