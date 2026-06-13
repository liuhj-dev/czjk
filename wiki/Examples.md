# 使用示例

本文档提供无驱动录音录像系统的**完整使用示例**和**常见使用场景**。

---

## 📚 目录

1. [基础示例](#基础示例)
2. [高级示例](#高级示例)
3. [使用场景](#使用场景)
4. [最佳实践](#最佳实践)

---

## 📝 基础示例

### 示例 1: 拍照

**功能**: 使用默认摄像头拍一张照片。

```java
import com.recording.api.ICameraDevice;
import com.recording.impl.MediaCaptureManager;
import java.io.File;

public class Example1_Photo {
    public static void main(String[] args) {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            // 初始化
            manager.initialize();
            
            // 获取默认摄像头
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            if (camera == null) {
                System.err.println("未找到摄像头");
                return;
            }
            
            // 设置视频源
            manager.setVideoSource(camera);
            
            // 拍照
            File photo = new File("output/photo_" + System.currentTimeMillis() + ".jpg");
            if (manager.capturePhoto(photo)) {
                System.out.println("✅ 拍照成功: " + photo.getAbsolutePath());
            } else {
                System.err.println("❌ 拍照失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在 `output/` 目录生成 JPG 文件。

---

### 示例 2: 录制视频（5秒）

**功能**: 录制 5 秒视频（不带音频）。

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;
import java.io.File;

public class Example2_VideoRecording {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            manager.setVideoSource(camera);
            
            // 配置：仅录像（不带音频）
            RecordingConfig config = new RecordingConfig("output/video_" + System.currentTimeMillis() + ".mp4");
            config.setCaptureVideoOnly(true);
            config.setVideoFormat(VideoFormat.Presets.HD720());  // 720p
            
            // 开始录制
            manager.startRecording(config);
            System.out.println("⏺ 开始录制视频...");
            
            // 录制 5 秒
            Thread.sleep(5000);
            
            // 停止录制
            manager.stopRecording();
            System.out.println("⏹ 停止录制");
            
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在 `output/` 目录生成 MP4 文件。

---

### 示例 3: 录制音频（5秒）

**功能**: 录制 5 秒音频（不带视频）。

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;
import java.io.File;

public class Example3_AudioRecording {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();
            manager.setAudioSource(microphone);
            
            // 配置：仅录音（不带视频）
            RecordingConfig config = new RecordingConfig("output/audio_" + System.currentTimeMillis() + ".wav");
            config.setCaptureAudioOnly(true);
            config.setAudioFormat(AudioFormat.Presets.CD_QUALITY());  // CD 音质
            
            // 开始录音
            manager.startRecording(config);
            System.out.println("⏺ 开始录音...");
            
            // 录音 5 秒
            Thread.sleep(5000);
            
            // 停止录音
            manager.stopRecording();
            System.out.println("⏹ 停止录音");
            
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在 `output/` 目录生成 WAV 文件。

---

### 示例 4: 同时录音录像（10秒）

**功能**: 同时录制视频和音频。

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.*;
import java.io.File;

public class Example4_AVRecording {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            // 设置视频源和音频源
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();
            
            manager.setVideoSource(camera);
            manager.setAudioSource(microphone);
            
            // 配置：同时录制音视频
            RecordingConfig config = new RecordingConfig("output/av_" + System.currentTimeMillis() + ".mp4");
            config.setVideoFormat(VideoFormat.Presets.HD720());
            config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());
            config.setRecordVideo(true);
            config.setRecordAudio(true);
            
            // 设置回调
            manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
                @Override
                public void onRecordingStarted(File outputFile) {
                    System.out.println("⏺ 开始录制: " + outputFile.getName());
                }
                
                @Override
                public void onRecordingProgress(long duration, long fileSize) {
                    System.out.printf("📊 进度: %.1f秒, %.2f MB%n",
                        duration / 1000.0, fileSize / (1024.0 * 1024.0));
                }
                
                @Override
                public void onRecordingStopped(File outputFile, long duration) {
                    System.out.println("⏹ 停止录制: " + outputFile.getName() + 
                        " (时长: " + (duration / 1000) + "秒)");
                }
                
                @Override
                public void onRecordingError(int errorCode, String errorMessage) {
                    System.err.println("❌ 录制错误: " + errorCode + " - " + errorMessage);
                }
            });
            
            // 开始录制
            manager.startRecording(config);
            
            // 录制 10 秒
            Thread.sleep(10000);
            
            // 停止录制
            manager.stopRecording();
            
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在 `output/` 目录生成包含音视频的 MP4 文件。

---

## 🚀 高级示例

### 示例 5: 视频预览（实时显示）

**功能**: 实时预览摄像头画面（用于处理或显示）。

```java
import com.recording.api.ICameraDevice;
import com.recording.api.ICameraDevice.PreviewCallback;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.VideoFrame;

public class Example5_Preview {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            manager.setVideoSource(camera);
            
            // 开始预览
            camera.startPreview(new PreviewCallback() {
                @Override
                public void onPreviewFrame(VideoFrame frame) {
                    // 这里是每一帧的回调
                    System.out.println("帧 #" + frame.getFrameNumber() + 
                        ", 大小: " + frame.getDataSize() + " bytes, " +
                        "时间戳: " + frame.getTimestamp() + "ms");
                    
                    // 在这里可以添加图像处理逻辑
                    // 例如：人脸识别、二维码扫描等
                }
                
                @Override
                public void onError(int errorCode, String errorMessage) {
                    System.err.println("预览错误: " + errorMessage);
                }
            });
            
            System.out.println("预览中...按 Enter 停止");
            System.in.read();  // 等待用户按 Enter
            
            camera.stopPreview();
            
        } finally {
            manager.destroy();
        }
    }
}
```

---

### 示例 6: 选择指定设备

**功能**: 列出所有设备并选择指定设备进行录制。

```java
import com.recording.api.ICameraDevice;
import com.recording.api.IAudioDevice;
import com.recording.impl.MediaCaptureManager;
import java.util.List;

public class Example6_SelectDevice {
    public static void main(String[] args) {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            // 列出所有摄像头
            System.out.println("=== 可用摄像头 ===");
            List<ICameraDevice> cameras = manager.getCameraDiscovery().discoverDevices();
            for (int i = 0; i < cameras.size(); i++) {
                ICameraDevice camera = cameras.get(i);
                System.out.println(i + ". " + camera.getDeviceName() + 
                    " (" + camera.getDeviceId() + ")");
            }
            
            // 列出所有音频设备
            System.out.println("\n=== 可用音频设备 ===");
            List<IAudioDevice> audios = manager.getAudioDiscovery().discoverDevices();
            for (int i = 0; i < audios.size(); i++) {
                IAudioDevice audio = audios.get(i);
                System.out.println(i + ". " + audio.getDeviceName() + 
                    " (" + audio.getDeviceId() + ")");
            }
            
            // 选择第一个摄像头和第一个音频设备
            if (!cameras.isEmpty() && !audios.isEmpty()) {
                manager.setVideoSource(cameras.get(0));
                manager.setAudioSource(audios.get(0));
                System.out.println("\n✅ 已选择设备");
            } else {
                System.err.println("\n❌ 设备不足");
            }
            
        } finally {
            manager.destroy();
        }
    }
}
```

---

### 示例 7: 自动停止（达到最大时长）

**功能**: 设置最大录制时长，自动停止。

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;

public class Example7_AutoStop {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            manager.setVideoSource(manager.getCameraDiscovery().getDefaultDevice());
            
            // 配置：最大录制 30 秒
            RecordingConfig config = new RecordingConfig("output/auto_stop.mp4");
            config.setMaxDuration(30000);  // 30 秒（毫秒）
            
            // 开始录制
            manager.startRecording(config);
            System.out.println("⏺ 开始录制（最长 30 秒）...");
            
            // 等待录制完成（或自动停止）
            while (manager.isRecording()) {
                Thread.sleep(1000);
                System.out.println("⏱ 已录制: " + (manager.getRecordingDuration() / 1000) + " 秒");
            }
            
            System.out.println("✅ 录制已停止（达到最大时长）");
            
        } finally {
            manager.destroy();
        }
    }
}
```

---

## 🎯 使用场景

### 场景 1: 高拍仪拍照

**需求**: 使用高拍仪拍摄文档并保存为 JPG。

```java
public class Scene1_DocumentScanner {
    public static void main(String[] args) {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            // 选择高拍仪（通常设备名包含 "Camera" 或 "Document"）
            List<ICameraDevice> cameras = manager.getCameraDiscovery().discoverDevices();
            ICameraDevice documentCamera = null;
            
            for (ICameraDevice camera : cameras) {
                if (camera.getDeviceName().contains("Camera") || 
                    camera.getDeviceName().contains("Document")) {
                    documentCamera = camera;
                    break;
                }
            }
            
            if (documentCamera == null && !cameras.isEmpty()) {
                documentCamera = cameras.get(0);  // 使用第一个摄像头
            }
            
            manager.setVideoSource(documentCamera);
            
            // 拍照（循环拍 5 张）
            for (int i = 1; i <= 5; i++) {
                File photo = new File("output/document_" + i + ".jpg");
                if (manager.capturePhoto(photo)) {
                    System.out.println("✅ 第 " + i + " 张拍照成功");
                }
                Thread.sleep(1000);  // 间隔 1 秒
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.destroy();
        }
    }
}
```

---

### 场景 2: 视频会议录制

**需求**: 录制视频会议的音视频（自动选择最佳格式）。

```java
public class Scene2_MeetingRecorder {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            // 设置音视频源
            manager.setVideoSource(manager.getCameraDiscovery().getDefaultDevice());
            manager.setAudioSource(manager.getAudioDiscovery().getDefaultDevice());
            
            // 配置：适合视频会议的参数
            RecordingConfig config = new RecordingConfig("output/meeting_" + 
                System.currentTimeMillis() + ".mp4");
            config.setVideoFormat(VideoFormat.Presets.HD720());  // 720p（平衡清晰度和文件大小）
            config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());  // 高质量音频
            config.setMaxDuration(3600000);  // 最长 1 小时
            
            manager.startRecording(config);
            System.out.println("⏺ 开始录制会议...");
            
            // 模拟会议进行中（实际应用中这里可能是消息循环）
            Thread.sleep(300000);  // 5 分钟
            
            manager.stopRecording();
            System.out.println("⏹ 会议录制完成");
            
        } finally {
            manager.destroy();
        }
    }
}
```

---

## 💡 最佳实践

### 1. 始终在 finally 块中调用 destroy()

```java
MediaCaptureManager manager = new MediaCaptureManager();
try {
    manager.initialize();
    // 使用管理器
} finally {
    manager.destroy();  // 确保资源释放
}
```

### 2. 使用回调监听状态变化

```java
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingError(int errorCode, String errorMessage) {
        // 处理错误
    }
    
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        // 更新 UI 或日志
    }
});
```

### 3. 检查设备可用性

```java
ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
if (camera == null) {
    System.err.println("未找到摄像头");
    return;
}

if (!camera.isOpen()) {
    if (!camera.open(VideoFormat.Presets.HD720())) {
        System.err.println("打开摄像头失败");
        return;
    }
}
```

### 4. 处理异常

```java
try {
    manager.startRecording(config);
} catch (Exception e) {
    System.err.println("开始录制失败: " + e.getMessage());
    e.printStackTrace();
    // 执行错误处理或恢复逻辑
}
```

---

## 🔙 返回首页

[返回 Wiki 首页](Home)

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
