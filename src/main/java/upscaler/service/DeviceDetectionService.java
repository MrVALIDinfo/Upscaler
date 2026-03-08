package upscaler.service;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import upscaler.model.ComputeDevice;
import upscaler.runtime.BundledRuntimeManager;
import upscaler.runtime.PreparedRuntime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DeviceDetectionService {
    private static final ComputeDevice AUTO_DEVICE = new ComputeDevice(
            "AUTO",
            "Auto",
            "Let Real-ESRGAN choose the best Vulkan device automatically.",
            null,
            true,
            false
    );

    private final BundledRuntimeManager runtimeManager;

    public DeviceDetectionService(BundledRuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    public DeviceScanResult detectDevices() {
        List<ComputeDevice> devices = new ArrayList<>();
        devices.add(AUTO_DEVICE);

        List<VulkanPhysicalDevice> vulkanDevices = detectViaVulkanInfo();
        if (!vulkanDevices.isEmpty()) {
            for (VulkanPhysicalDevice device : vulkanDevices) {
                devices.add(new ComputeDevice(
                        "GPU:" + device.gpuId(),
                        device.deviceName(),
                        beautifyDeviceType(device.deviceType()) + " • " + device.driverInfo(),
                        Integer.toString(device.gpuId()),
                        true,
                        device.deviceType().toLowerCase(Locale.ROOT).contains("cpu")
                ));
            }
            return new DeviceScanResult(devices, "Devices detected through Vulkan runtime.");
        }

        List<ComputeDevice> probed = probeBundledRuntime();
        if (!probed.isEmpty()) {
            devices.addAll(probed);
            return new DeviceScanResult(devices, "Devices probed via bundled Real-ESRGAN runtime. Names are matched against host adapters.");
        }

        return new DeviceScanResult(devices, "No explicit Vulkan device list detected. Auto mode remains available.");
    }

    private List<VulkanPhysicalDevice> detectViaVulkanInfo() {
        try {
            Process process = new ProcessBuilder("vulkaninfo", "--summary")
                    .redirectErrorStream(true)
                    .start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().reduce("", (left, right) -> left + right + System.lineSeparator());
            }
            process.waitFor(15, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                return List.of();
            }
            return VulkanInfoParser.parseSummary(output);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<ComputeDevice> probeBundledRuntime() {
        try {
            PreparedRuntime runtime = runtimeManager.prepareRuntime();
            List<String> adapterNames = hostGraphicsCardNames();
            List<ComputeDevice> devices = new ArrayList<>();
            int missesAfterFirstHit = 0;
            boolean foundAny = false;

            for (int gpuId = 0; gpuId < 8; gpuId++) {
                if (probeGpuId(runtime, gpuId)) {
                    foundAny = true;
                    missesAfterFirstHit = 0;
                    String displayName = gpuId < adapterNames.size() ? adapterNames.get(gpuId) : "GPU " + gpuId;
                    devices.add(new ComputeDevice(
                            "GPU:" + gpuId,
                            displayName,
                            "Detected by probing the bundled runtime.",
                            Integer.toString(gpuId),
                            true,
                            false
                    ));
                } else if (foundAny) {
                    missesAfterFirstHit++;
                    if (missesAfterFirstHit >= 2) {
                        break;
                    }
                }
            }
            return devices;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private boolean probeGpuId(PreparedRuntime runtime, int gpuId) {
        Path workspace = null;
        Process process = null;
        try {
            workspace = Files.createTempDirectory("upscaler-device-probe-");
            Path input = workspace.resolve("probe-in.png");
            Path output = workspace.resolve("probe-out.png");

            BufferedImage sample = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            ImageIO.write(sample, "png", input.toFile());

            List<String> command = new ArrayList<>();
            command.add(runtime.executable().toString());
            command.add("-i");
            command.add(input.toString());
            command.add("-o");
            command.add(output.toString());
            command.add("-n");
            command.add("realesrgan-x4plus");
            command.add("-s");
            command.add("2");
            command.add("-g");
            command.add(Integer.toString(gpuId));
            command.add("-m");
            command.add(runtime.modelsDirectory().toString());

            process = new ProcessBuilder(command)
                    .directory(runtime.rootDirectory().toFile())
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (workspace != null) {
                try {
                    Files.walk(workspace)
                            .sorted((left, right) -> right.compareTo(left))
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
    }

    private List<String> hostGraphicsCardNames() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            List<GraphicsCard> cards = systemInfo.getHardware().getGraphicsCards();
            List<String> names = new ArrayList<>();
            for (GraphicsCard card : cards) {
                String name = card.getName();
                if (name != null && !name.isBlank() && names.stream().noneMatch(name::equalsIgnoreCase)) {
                    names.add(name);
                }
            }
            return names;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String beautifyDeviceType(String rawType) {
        String normalized = rawType.replace("PHYSICAL_DEVICE_TYPE_", "").replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
