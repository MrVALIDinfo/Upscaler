package upscaler.service;

import upscaler.model.ComputeDevice;

import java.util.List;

public record DeviceScanResult(List<ComputeDevice> devices, String summary) {
}
