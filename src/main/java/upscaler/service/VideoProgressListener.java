package upscaler.service;

public interface VideoProgressListener {
    void onStage(String stage, int overallPercent);

    void onLog(String line);
}
