package com.recording.controller;

import com.recording.model.FileInfo;
import com.recording.service.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private RecordingService recordingService;

    /** 获取文件列表 */
    @GetMapping
    public ResponseEntity<List<FileInfo>> getFileList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type) {
        List<FileInfo> files = recordingService.getFileList();
        return ResponseEntity.ok(files);
    }

    /** 下载文件 */
    @GetMapping("/{fileName}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        String filePath = recordingService.getFilePath(fileName);
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /** 删除文件 */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileName) {
        Map<String, Object> result = new HashMap<>();
        boolean success = recordingService.deleteFile(fileName);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "文件不存在");
        return ResponseEntity.ok(result);
    }
}
