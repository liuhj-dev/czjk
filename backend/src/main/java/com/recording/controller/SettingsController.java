package com.recording.controller;

import com.recording.service.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 设置控制器
 */
@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    @Autowired
    private RecordingService recordingService;

    private final Map<String, Object> settings = new HashMap<>();

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("video", Map.of(
                "resolution", "1280x720",
                "frameRate", 30,
                "codec", "h264",
                "bitrate", 2500
        ));
        defaults.put("audio", Map.of(
                "sampleRate", 44100,
                "bitDepth", 16,
                "channels", 2,
                "codec", "aac",
                "bitrate", 128
        ));
        defaults.put("storage", Map.of(
                "outputPath", "./output",
                "format", "mp4",
                "autoSplit", false,
                "splitSize", 1073741824,
                "maxDuration", 0
        ));
        return ResponseEntity.ok(defaults);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> newSettings) {
        settings.clear();
        settings.putAll(newSettings);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设置已保存");
        return ResponseEntity.ok(result);
    }
}
