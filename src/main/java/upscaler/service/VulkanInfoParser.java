package upscaler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VulkanInfoParser {
    private static final Pattern GPU_HEADER = Pattern.compile("GPU(\\d+):");

    private VulkanInfoParser() {
    }

    public static List<VulkanPhysicalDevice> parseSummary(String output) {
        List<VulkanPhysicalDevice> devices = new ArrayList<>();
        Integer currentId = null;
        String currentName = null;
        String currentType = null;
        String currentDriver = null;

        for (String rawLine : output.split("\\R")) {
            String line = rawLine.strip();
            Matcher matcher = GPU_HEADER.matcher(line);
            if (matcher.matches()) {
                if (currentId != null) {
                    devices.add(new VulkanPhysicalDevice(currentId, fallback(currentName), fallback(currentType), fallback(currentDriver)));
                }
                currentId = Integer.parseInt(matcher.group(1));
                currentName = null;
                currentType = null;
                currentDriver = null;
                continue;
            }

            if (currentId == null || !line.contains("=")) {
                continue;
            }

            String[] parts = line.split("=", 2);
            String key = parts[0].trim();
            String value = parts[1].trim();
            switch (key) {
                case "deviceName" -> currentName = value;
                case "deviceType" -> currentType = value;
                case "driverInfo" -> currentDriver = value;
                default -> {
                }
            }
        }

        if (currentId != null) {
            devices.add(new VulkanPhysicalDevice(currentId, fallback(currentName), fallback(currentType), fallback(currentDriver)));
        }
        return devices;
    }

    private static String fallback(String value) {
        return value == null || value.isBlank() ? "Unknown" : value;
    }
}
