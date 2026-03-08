package upscaler.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VulkanInfoParserTest {
    @Test
    void parsesSummaryDeviceBlocks() {
        String output = """
                Devices:
                ========
                GPU0:
                \tdeviceType         = PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU
                \tdeviceName         = Intel(R) UHD Graphics (CML GT2)
                \tdriverInfo         = Mesa 26.0.1
                GPU1:
                \tdeviceType         = PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
                \tdeviceName         = NVIDIA GeForce GTX 1660 Ti
                \tdriverInfo         = 580.126.09
                GPU2:
                \tdeviceType         = PHYSICAL_DEVICE_TYPE_CPU
                \tdeviceName         = llvmpipe
                \tdriverInfo         = Mesa CPU
                """;

        List<VulkanPhysicalDevice> devices = VulkanInfoParser.parseSummary(output);

        assertEquals(3, devices.size());
        assertEquals(0, devices.get(0).gpuId());
        assertEquals("Intel(R) UHD Graphics (CML GT2)", devices.get(0).deviceName());
        assertEquals("PHYSICAL_DEVICE_TYPE_DISCRETE_GPU", devices.get(1).deviceType());
        assertEquals("llvmpipe", devices.get(2).deviceName());
    }
}
