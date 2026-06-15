package com.recording.controller;

import com.recording.model.DeviceInfo;
import com.recording.model.RecordingStatus;
import com.recording.service.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Autowired
    private RecordingService recordingService;

    /** 获取摄像头列表 */
    @GetMapping("/cameras")
    public ResponseEntity<List<DeviceInfo>> getCameras() {
        return ResponseEntity.ok(recordingService.getCameraDevices());
    }

    /** 获取音频设备列表 */
    @GetMapping("/audio")
    public ResponseEntity<List<DeviceInfo>> getAudioDevices() {
        return ResponseEntity.ok(recordingService.getAudioDevices());
    }

    /** 扫描所有设备 */
    @GetMapping("/discover")
    public ResponseEntity<Map<String, List<DeviceInfo>>> discover() {
        Map<String, List<DeviceInfo>> result = new HashMap<>();
        result.put("cameras", recordingService.getCameraDevices());
        result.put("audioDevices", recordingService.getAudioDevices());
        return ResponseEntity.ok(result);
    }

    /** 打开设备 */
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openDevice(@RequestBody Map<String, String> body) {
        String deviceId = body.get("deviceId");
        String type = body.get("type");
        boolean success = recordingService.openDevice(deviceId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "设备已打开" : "打开设备失败");
        return ResponseEntity.ok(result);
    }

    /** 关闭设备 */
    @PostMapping("/close")
    public ResponseEntity<Map<String, Object>> closeDevice(@RequestBody Map<String, String> body) {
        String deviceId = body.get("deviceId");
        String type = body.get("type");
        boolean success = recordingService.closeDevice(deviceId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "设备已关闭" : "关闭设备失败");
        return ResponseEntity.ok(result);
    }
}
