package com.jlau.libraryseat.controller;

import com.jlau.libraryseat.common.Result;
import com.jlau.libraryseat.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/detection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController {

    private final DetectionService detectionService;

    /**
     * 手动触发检测（测试用）
     */
    @PostMapping("/check")
    public Result<?> checkPresence(
            @RequestParam String studentNo,
            @RequestParam String seatNo,
            @RequestParam(defaultValue = "MULTI") String mode) {

        boolean result;
        switch (mode.toUpperCase()) {
            case "WIFI":
                result = detectionService.detectByWifi(studentNo, seatNo);
                break;
            case "BLUETOOTH":
                result = detectionService.detectByBluetooth(studentNo, seatNo);
                break;
            case "RFID":
                result = detectionService.detectByRfid(studentNo, seatNo, null);
                break;
            case "CAMERA":
                result = detectionService.detectByCamera(studentNo, seatNo);
                break;
            default:
                result = detectionService.multiModalDetection(studentNo, seatNo);
        }

        // Java 8 兼容写法
        Map<String, Object> data = new HashMap<>();
        data.put("studentNo", studentNo);
        data.put("seatNo", seatNo);
        data.put("mode", mode);
        data.put("present", result);
        data.put("timestamp", LocalDateTime.now());

        return Result.success(data);
    }

    /**
     * 获取检测配置状态
     */
    @GetMapping("/status")
    public Result<?> getDetectionStatus() {
        // Java 8 兼容写法
        Map<String, Object> status = new HashMap<>();

        Map<String, Object> wifi = new HashMap<>();
        wifi.put("enabled", false);
        wifi.put("status", "未接入");
        status.put("wifi", wifi);

        Map<String, Object> bluetooth = new HashMap<>();
        bluetooth.put("enabled", false);
        bluetooth.put("status", "未接入");
        status.put("bluetooth", bluetooth);

        Map<String, Object> rfid = new HashMap<>();
        rfid.put("enabled", false);
        rfid.put("status", "未接入");
        status.put("rfid", rfid);

        Map<String, Object> camera = new HashMap<>();
        camera.put("enabled", false);
        camera.put("status", "未接入");
        status.put("camera", camera);

        status.put("fusionStrategy", "VOTE");

        return Result.success(status);
    }
}
