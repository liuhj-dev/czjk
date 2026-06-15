package com.recording.controller;

import com.recording.model.RecordingConfigRequest;
import com.recording.model.RecordingStatus;
import com.recording.service.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 录像控制控制器
 */
@RestController
@RequestMapping("/api/recording")
@CrossOrigin(origins = "*")
public class RecordingController {

    @Autowired
    private RecordingService recordingService;

    /** 开始录制 */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startRecording(@RequestBody RecordingConfigRequest config) {
        Map<String, Object> result = new HashMap<>();
        try {
            recordingService.startRecording(config);
            result.put("success", true);
            result.put("message", "录制已开始");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /** 停止录制 */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopRecording() {
        Map<String, Object> result = new HashMap<>();
        recordingService.stopRecording();
        result.put("success", true);
        result.put("message", "录制已停止");
        return ResponseEntity.ok(result);
    }

    /** 暂停录制 */
    @PostMapping("/pause")
    public ResponseEntity<Map<String, Object>> pauseRecording() {
        Map<String, Object> result = new HashMap<>();
        recordingService.pauseRecording();
        result.put("success", true);
        result.put("message", "录制已暂停");
        return ResponseEntity.ok(result);
    }

    /** 恢复录制 */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resumeRecording() {
        Map<String, Object> result = new HashMap<>();
        recordingService.resumeRecording();
        result.put("success", true);
        result.put("message", "录制已恢复");
        return ResponseEntity.ok(result);
    }

    /** 拍照 */
    @PostMapping("/photo")
    public ResponseEntity<Map<String, Object>> capturePhoto(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String deviceId = body.get("deviceId");
            String photoPath = recordingService.capturePhoto(deviceId);
            result.put("success", true);
            result.put("path", photoPath);
            result.put("message", "拍照成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /** 获取录制状态 */
    @GetMapping("/status")
    public ResponseEntity<RecordingStatus> getStatus() {
        return ResponseEntity.ok(recordingService.getStatus());
    }
}
