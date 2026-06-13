# 性能优化

本文档提供无驱动录音录像系统的**性能优化建议**和**最佳实践**。

---

## 📚 目录

1. [CPU 优化](#cpu-优化)
2. [内存优化](#内存优化)
3. [磁盘 I/O 优化](#磁盘-io-优化)
4. [视频编码优化](#视频编码优化)
5. [音频处理优化](#音频处理优化)
6. [多线程优化](#多线程优化)
7. [硬件加速](#硬件加速)
8. [最佳实践](#最佳实践)

---

## 💻 CPU 优化

### 问题: 录制时 CPU 占用过高

**原因**:
- 软件编码（如 x264）占用大量 CPU
- 高分辨率/高帧率
- 同时录制多个流

### 解决方案

#### 1. 使用硬件加速编码（最有效）

**NVIDIA GPU (NVENC)**:
```java
// 使用 NVIDIA GPU 编码（需要 NVIDIA 显卡）
recorder.setVideoCodecName("h264_nvenc");  // H.264
// 或
recorder.setVideoCodecName("hevc_nvenc");  // H.265
```

**Intel GPU (QuickSync)**:
```java
// 使用 Intel 集成显卡编码（需要 Intel 第 2 代 CPU+）
recorder.setVideoCodecName("h264_qsv");  // H.264
// 或
recorder.setVideoCodecName("hevc_qsv");  // H.265
```

**AMD GPU (VCE/AMF)**:
```java
// 使用 AMD GPU 编码（需要 AMD 显卡）
recorder.setVideoCodecName("h264_amf");  // H.264
// 或
recorder.setVideoCodecName("hevc_amf");  // H.265
```

**检查硬件加速是否可用**:
```bash
# Windows: 使用 FFmpeg 检查
ffmpeg -hide_banner -encoders | findstr nvenc  # NVIDIA
ffmpeg -hide_banner -encoders | findstr qsv    # Intel
ffmpeg -hide_banner -encoders | findstr amf    # AMD

# macOS/Linux:
ffmpeg -hide_banner -encoders | grep nvenc
ffmpeg -hide_banner -encoders | grep qsv
ffmpeg -hide_banner -encoders | grep amf
```

---

#### 2. 降低分辨率或帧率

```java
// 从 1080p 降到 720p
VideoFormat format720p = new VideoFormat(1280, 720, 30.0, "H.264", 2000000);
config.setVideoFormat(format720p);

// 从 60 FPS 降到 30 FPS
VideoFormat format30fps = new VideoFormat(1920, 1080, 30.0, "H.264", 3000000);
config.setVideoFormat(format30fps);
```

**性能对比**:

| 分辨率 | 帧率 | CPU 占用（软件编码） | CPU 占用（硬件加速） |
|--------|------|---------------------|---------------------|
| 640x480 | 30 FPS | ~10% | ~2% |
| 1280x720 | 30 FPS | ~30% | ~5% |
| 1920x1080 | 30 FPS | ~60% | ~10% |
| 1920x1080 | 60 FPS | ~90% | ~15% |
| 3840x2160 | 30 FPS | ~100% | ~20% |

---

#### 3. 降低比特率

```java
// 降低比特率（会减少文件大小，但可能降低画质）
VideoFormat lowBitrate = new VideoFormat(1920, 1080, 30.0, "H.264", 1000000);  // 1 Mbps
config.setVideoFormat(lowBitrate);
```

**比特率建议**:

| 分辨率 | 推荐比特率 | 最低比特率 |
|--------|------------|------------|
| 640x480 | 500-1000 Kbps | 300 Kbps |
| 1280x720 | 1500-2500 Kbps | 800 Kbps |
| 1920x1080 | 3000-5000 Kbps | 1500 Kbps |
| 3840x2160 | 20000-40000 Kbps | 10000 Kbps |

---

## 💾 内存优化

### 问题: 内存占用过高或内存溢出

**原因**:
- 帧缓冲区过大
- 同时打开太多设备
- 内存泄漏（未调用 `close()` 或 `destroy()`）

### 解决方案

#### 1. 增加 Java 堆内存

```bash
# 运行程序时指定堆内存最小值和最大值
java -Xms512m -Xmx2048m -jar your-app.jar
```

**推荐配置**:

| 录制参数 | 推荐堆内存 |
|----------|------------|
| 720p@30FPS | 512 MB - 1 GB |
| 1080p@30FPS | 1 GB - 2 GB |
| 1080p@60FPS | 2 GB - 4 GB |
| 4K@30FPS | 4 GB - 8 GB |

---

#### 2. 及时释放资源

```java
MediaCaptureManager manager = new MediaCaptureManager();

try {
    manager.initialize();
    
    ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
    manager.setVideoSource(camera);
    
    // 使用设备...
    
} finally {
    // 重要：始终在 finally 块中调用 destroy()
    manager.destroy();  // 会关闭所有设备并释放内存
}
```

---

#### 3. 避免同时打开太多设备

```java
// ❌ 不推荐：同时打开多个摄像头
ICameraDevice camera1 = new JavaCVCamera("camera_0", "...", "...");
ICameraDevice camera2 = new JavaCVCamera("camera_1", "...", "...");
camera1.open(VideoFormat.Presets.HD720());
camera2.open(VideoFormat.Presets.HD720());
// 这会占用大量内存

// ✅ 推荐：使用完一个设备后，关闭再打开下一个
ICameraDevice camera = new JavaCVCamera("camera_0", "...", "...");
camera.open(VideoFormat.Presets.HD720());
// 使用...
camera.close();  // 释放资源

// 需要时使用另一个设备
ICameraDevice camera2 = new JavaCVCamera("camera_1", "...", "...");
camera2.open(VideoFormat.Presets.HD720());
```

---

## 💿 磁盘 I/O 优化

### 问题: 磁盘写入慢或磁盘空间不足

**原因**:
- 机械硬盘速度慢
- 录制高码率视频
- 磁盘空间不足

### 解决方案

#### 1. 使用 SSD（强烈推荐）

| 硬盘类型 | 写入速度 | 适用场景 |
|----------|----------|----------|
| 机械硬盘 (HDD) | 80-160 MB/s | 仅适合低码率录制（< 5 Mbps） |
| SATA SSD | 200-600 MB/s | 适合大部分场景 |
| NVMe SSD | 1000-3500 MB/s | 适合 4K 录制、多路录制 |

---

#### 2. 降低比特率或分辨率

```java
// 如果磁盘写入速度慢，降低比特率
VideoFormat format = new VideoFormat(1920, 1080, 30.0, "H.264", 2000000);  // 2 Mbps
config.setVideoFormat(format);
```

---

#### 3. 设置最大文件大小并自动分割

```java
// 设置单个文件最大 1 GB，超过后自动分割
RecordingConfig config = new RecordingConfig("output/recording.mp4");
config.setMaxFileSize(1L * 1024 * 1024 * 1024);  // 1 GB
config.setAutoSplit(true);
config.setFileNamePattern("output/recording_%d.mp4");  // %d 会被替换为序号
```

---

#### 4. 定期检查磁盘空间

```java
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        // 检查磁盘剩余空间
        File outputDir = new File("output");
        long freeSpace = outputDir.getFreeSpace();
        
        if (freeSpace < 1024L * 1024 * 1024) {  // 小于 1 GB
            System.err.println("警告: 磁盘空间不足！");
            manager.stopRecording();
        }
    }
});
```

---

## 🎥 视频编码优化

### 优化 1: 选择合适的编码预设（preset）

```java
// 对于 x264 软件编码，可以选择更快的预设（但文件会更大）
recorder.setVideoOption("preset", "ultrafast");  // 最快编码，文件最大
// 或
recorder.setVideoOption("preset", "fast");  // 平衡速度和文件大小
// 或
recorder.setVideoOption("preset", "medium");  // 默认，较慢但文件较小
```

**预设对比**:

| 预设 | 编码速度 | 文件大小 | CPU 占用 |
|------|----------|----------|----------|
| ultrafast | 最快 | 最大（+50%） | 最低 |
| fast | 快 | 较大（+20%） | 较低 |
| medium | 中 | 标准 | 较高 |
| slow | 慢 | 较小（-20%） | 最高 |

---

### 优化 2: 使用更高效的编码格式

```java
// H.265 (HEVC) 比 H.264 效率高 25-50%
recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
recorder.setVideoFormat("hevc");

// 注意：H.265 编码需要更多 CPU（或使用硬件加速）
```

**编码格式对比**:

| 编码格式 | 同等画质下的比特率 | CPU 占用 | 兼容性 |
|----------|-------------------|----------|--------|
| H.264 | 标准 | 中 | 最好（所有设备） |
| H.265 | 低 25-50% | 高 | 较好（2015+ 设备） |
| VP9 | 低 30-50% | 很高 | 一般（主要用于 Web） |
| AV1 | 低 40-60% | 极高 | 差（新兴格式） |

---

## 🎤 音频处理优化

### 优化 1: 使用更高效的音频编码

```java
// AAC 比 MP3 更高效
config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());  // 推荐

// 如果不需要高质量音频，可以降低比特率
AudioFormat lowBitrateAudio = new AudioFormat(44100, 16, 2, "AAC", 96000);  // 96 Kbps
config.setAudioFormat(lowBitrateAudio);
```

**音频编码对比**:

| 编码格式 | 比特率（推荐） | 音质 | 兼容性 |
|----------|----------------|------|--------|
| PCM | 1411 Kbps (CD) | 无损 | 最好 |
| WAV | 1411 Kbps (CD) | 无损 | 很好 |
| AAC | 96-192 Kbps | 接近无损 | 很好 |
| MP3 | 128-320 Kbps | 有损 | 最好 |
| OGG | 96-160 Kbps | 接近无损 | 一般 |

---

### 优化 2: 减少音频采样率或声道数

```java
// 如果不需要立体声，使用单声道
AudioFormat mono = new AudioFormat(44100, 16, 1, "AAC", 64000);  // 单声道
config.setAudioFormat(mono);

// 如果不需要高采样率，使用 22050 Hz
AudioFormat lowSampleRate = new AudioFormat(22050, 16, 2, "AAC", 96000);
config.setAudioFormat(lowSampleRate);
```

---

## 🔀 多线程优化

### 问题: 预览或录制时 UI 卡顿

**原因**:
- 在主线程中执行耗时操作（如帧处理）
- 回调方法执行时间过长

### 解决方案

#### 1. 在独立线程中执行录制

```java
// ✅ 推荐：在独立线程中录制
new Thread(() -> {
    try {
        manager.startRecording(config);
        Thread.sleep(5000);
        manager.stopRecording();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}).start();
```

---

#### 2. 在回调方法中避免耗时操作

```java
// ❌ 不推荐：在回调中执行耗时操作
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        // 不要在这里执行耗时操作（如写入文件、网络请求）
        // 这会阻塞录制线程
    }
});

// ✅ 推荐：在回调中只做快速操作（如更新 UI）
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        // 快速更新 UI（如使用 SwingUtilities.invokeLater）
        SwingUtilities.invokeLater(() -> {
            label.setText("已录制: " + (duration / 1000) + " 秒");
        });
    }
});
```

---

## 🚀 硬件加速

### NVIDIA GPU 加速

**要求**:
- NVIDIA 显卡（GTX 1050+ 或 RTX 系列）
- 安装最新 NVIDIA 驱动
- 安装 NVIDIA Video Codec SDK（可选，FFmpeg 自带）

**配置**:
```java
// H.264 编码
recorder.setVideoCodecName("h264_nvenc");
recorder.setVideoOption("gpu", "0");  // 使用第一个 GPU

// H.265 编码
recorder.setVideoCodecName("hevc_nvenc");

// 设置编码预设（可选）
recorder.setVideoOption("preset", "fast");  // fast, medium, slow
```

---

### Intel GPU 加速（QuickSync）

**要求**:
- Intel CPU（第 2 代 / Sandy Bridge+）
- 安装 Intel 显卡驱动
- 在 BIOS 中启用 IGPU（如果同时使用独立显卡）

**配置**:
```java
// H.264 编码
recorder.setVideoCodecName("h264_qsv");

// H.265 编码
recorder.setVideoCodecName("hevc_qsv");

// 设置目标使用率（可选）
recorder.setVideoOption("global_quality", "25");  // 数值越低质量越高
```

---

### AMD GPU 加速（VCE/AMF）

**要求**:
- AMD 显卡（RX 460+ 或 Vega 系列）
- 安装最新 AMD 驱动

**配置**:
```java
// H.264 编码
recorder.setVideoCodecName("h264_amf");

// H.265 编码
recorder.setVideoCodecName("hevc_amf");

// 设置编码质量（可选）
recorder.setVideoOption("quality", "speed");  // speed, balanced, quality
```

---

## 💡 最佳实践

### 1. 生产环境配置（推荐）

```java
// 生产环境推荐配置（平衡性能和质量）
RecordingConfig config = new RecordingConfig("output/recording.mp4");

// 视频：720p@30FPS, H.264, 2.5 Mbps
VideoFormat videoFormat = new VideoFormat(1280, 720, 30.0, "H.264", 2500000);
config.setVideoFormat(videoFormat);

// 音频：44100Hz, AAC, 128 Kbps
AudioFormat audioFormat = new AudioFormat(44100, 16, 2, "AAC", 128000);
config.setAudioFormat(audioFormat);

// 设置最大时长和文件大小
config.setMaxDuration(3600000);  // 最长 1 小时
config.setMaxFileSize(4L * 1024 * 1024 * 1024);  // 最大 4 GB

// 使用硬件加速（如果可用）
try {
    recorder.setVideoCodecName("h264_nvenc");  // 或 h264_qsv, h264_amf
} catch (Exception e) {
    System.out.println("硬件加速不可用，使用软件编码");
}
```

---

### 2. 高质量配置（适合专业制作）

```java
// 高质量配置（需要高性能硬件）
RecordingConfig config = new RecordingConfig("output/high_quality.mp4");

// 视频：1080p@30FPS, H.265, 5 Mbps
VideoFormat videoFormat = new VideoFormat(1920, 1080, 30.0, "H.265", 5000000);
config.setVideoFormat(videoFormat);

// 音频：48000Hz, AAC, 192 Kbps
AudioFormat audioFormat = new AudioFormat(48000, 16, 2, "AAC", 192000);
config.setAudioFormat(audioFormat);

// 使用硬件加速（必须）
recorder.setVideoCodecName("hevc_nvenc");  // NVIDIA GPU
```

---

### 3. 低性能设备配置（适合旧电脑）

```java
// 低性能设备配置（减少资源占用）
RecordingConfig config = new RecordingConfig("output/low_performance.mp4");

// 视频：720p@24FPS, H.264, 1 Mbps
VideoFormat videoFormat = new VideoFormat(1280, 720, 24.0, "H.264", 1000000);
config.setVideoFormat(videoFormat);

// 音频：22050Hz, AAC, 64 Kbps, 单声道
AudioFormat audioFormat = new AudioFormat(22050, 16, 1, "AAC", 64000);
config.setAudioFormat(audioFormat);

// 使用最快的编码预设
recorder.setVideoOption("preset", "ultrafast");
```

---

### 4. 监控性能指标

```java
// 监控 CPU 和内存占用
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        // 获取 CPU 占用（需要第三方库，如 OSHI）
        // 获取内存占用
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        System.out.printf("进度: %.1f秒, 文件: %.2f MB, 内存: %.2f MB%n",
            duration / 1000.0,
            fileSize / (1024.0 * 1024.0),
            usedMemory / (1024.0 * 1024.0));
    }
});
```

---

## 📊 性能测试

### 测试脚本

```java
public class PerformanceTest {
    public static void main(String[] args) {
        // 测试不同配置的性能
        testConfiguration("720p@30FPS", VideoFormat.Presets.HD720(), AudioFormat.Presets.AAC_HIGH());
        testConfiguration("1080p@30FPS", VideoFormat.Presets.FULL_HD(), AudioFormat.Presets.AAC_HIGH());
    }
    
    private static void testConfiguration(String name, VideoFormat videoFormat, AudioFormat audioFormat) {
        System.out.println("\n=== 测试配置: " + name + " ===");
        
        MediaCaptureManager manager = new MediaCaptureManager();
        try {
            manager.initialize();
            manager.setVideoSource(manager.getCameraDiscovery().getDefaultDevice());
            
            RecordingConfig config = new RecordingConfig("output/test.mp4");
            config.setVideoFormat(videoFormat);
            config.setAudioFormat(audioFormat);
            
            // 开始录制并监控性能
            long startTime = System.currentTimeMillis();
            manager.startRecording(config);
            
            // 录制 10 秒
            Thread.sleep(10000);
            
            manager.stopRecording();
            long endTime = System.currentTimeMillis();
            
            System.out.println("测试完成，耗时: " + ((endTime - startTime) / 1000) + " 秒");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.destroy();
        }
    }
}
```

---

## 🔙 返回首页

[返回 Wiki 首页](Home)

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
