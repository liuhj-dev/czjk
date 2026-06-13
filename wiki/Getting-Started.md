# 快速开始指南

本指南将帮助你在 **5 分钟**内运行无驱动录音录像系统。

---

## 📋 前置要求

### 系统要求

- **操作系统**: Windows 7+, macOS 10.12+, Linux (Ubuntu 18.04+)
- **JDK**: 8 或更高版本（推荐 JDK 11/17 LTS）
- **内存**: 至少 2GB RAM
- **设备**: UVC 兼容摄像头、UAC 兼容麦克风

### 设备检查

**检查摄像头是否支持 UVC：**
- Windows: 打开"相机"应用，能正常使用即支持
- macOS: 打开"Photo Booth"，能正常使用即支持
- Linux: 运行 `lsusb` 查看设备，应有 "Video" 类别

**检查麦克风是否支持 UAC：**
- Windows: 打开"声音设置"，能正常使用即支持
- macOS: 打开"系统偏好设置 → 声音"，能正常使用即支持
- Linux: 运行 `arecord -l` 列出设备

---

## 🚀 安装步骤

### 步骤 1: 克隆仓库

```bash
git clone https://github.com/liuhj-dev/czjk.git
cd czjk
```

### 步骤 2: 配置 Maven

如果你使用 Maven，添加以下依赖到 `pom.xml`：

```xml
<dependencies>
    <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>javacv</artifactId>
        <version>1.5.9</version>
    </dependency>
    
    <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>opencv-platform</artifactId>
        <version>4.7.0-1.5.9</version>
    </dependency>
    
    <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>ffmpeg-platform</artifactId>
        <version>6.0-1.5.9</version>
    </dependency>
</dependencies>
```

### 步骤 3: 编译项目

```bash
mvn clean compile
```

或使用 IDE（IntelliJ IDEA / Eclipse）导入项目并编译。

---

## 📝 第一个程序

### 示例 1: 拍照

```java
import com.recording.api.ICameraDevice;
import com.recording.impl.MediaCaptureManager;
import com.recording.impl.CameraDiscovery;
import java.io.File;

public class QuickStart {
    public static void main(String[] args) {
        // 1. 创建管理器
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            // 2. 初始化（扫描设备）
            manager.initialize();
            
            // 3. 获取默认摄像头
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            if (camera == null) {
                System.err.println("未找到摄像头设备");
                return;
            }
            
            // 4. 打开摄像头
            manager.setVideoSource(camera);
            
            // 5. 拍照
            File photo = new File("output/photo.jpg");
            if (manager.capturePhoto(photo)) {
                System.out.println("拍照成功: " + photo.getAbsolutePath());
            } else {
                System.err.println("拍照失败");
            }
        } finally {
            // 6. 清理资源
            manager.destroy();
        }
    }
}
```

**运行结果**: 在项目根目录生成 `output/photo.jpg` 文件。

---

### 示例 2: 录制视频（5秒）

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;
import java.io.File;

public class RecordVideo {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            manager.setVideoSource(camera);
            
            // 配置录制参数
            RecordingConfig config = new RecordingConfig("output/video.mp4");
            
            // 开始录制
            manager.startRecording(config);
            System.out.println("开始录制...");
            
            // 录制 5 秒
            Thread.sleep(5000);
            
            // 停止录制
            manager.stopRecording();
            System.out.println("录制完成: output/video.mp4");
            
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在项目根目录生成 `output/video.mp4` 文件。

---

### 示例 3: 录音（5秒）

```java
import com.recording.api.*;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;
import java.io.File;

public class RecordAudio {
    public static void main(String[] args) throws InterruptedException {
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();
            manager.setAudioSource(microphone);
            
            // 配置录音参数
            RecordingConfig config = new RecordingConfig("output/audio.wav");
            config.setCaptureAudioOnly(true);  // 仅录音模式
            
            // 开始录音
            manager.startRecording(config);
            System.out.println("开始录音...");
            
            // 录音 5 秒
            Thread.sleep(5000);
            
            // 停止录音
            manager.stopRecording();
            System.out.println("录音完成: output/audio.wav");
            
        } finally {
            manager.destroy();
        }
    }
}
```

**运行结果**: 在项目根目录生成 `output/audio.wav` 文件。

---

## 🔧 常见问题

### Q1: 找不到摄像头设备

**原因**: 设备未连接或驱动不兼容。

**解决**:
1. 检查设备是否连接
2. 尝试其他 USB 接口
3. 确认设备支持 UVC 标准
4. 运行 `CameraDiscovery.main()` 查看所有可用设备

### Q2: 录制文件无法播放

**原因**: 编码参数错误或文件未正确关闭。

**解决**:
1. 确保调用 `manager.stopRecording()` 停止录制
2. 使用标准编码格式（H.264 + AAC）
3. 检查输出文件路径是否有写入权限

### Q3: 音频不同步

**原因**: 音视频时间戳不一致。

**解决**:
1. 使用相同的 timebase
2. 降低分辨率或帧率
3. 使用硬件加速编码

---

## 📚 下一步

- 阅读 [API 参考](API-Reference) 了解完整的接口文档
- 查看 [使用示例](Examples) 学习更多使用场景
- 阅读 [设备兼容性](Compatibility) 确认你的设备是否支持

---

## 💡 提示

1. **始终调用 `destroy()`**: 在 `finally` 块中调用，确保资源释放
2. **使用回调**: 通过 `RecordingCallback` 监听录制状态
3. **检查设备可用性**: 在 `open()` 之前检查设备是否可用
4. **处理异常**: 捕获并处理 `Exception`，提高程序稳定性

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
