# 常见问题（FAQ）

本文档列出无驱动录音录像系统的**常见问题**和**解答**。

---

## 📚 目录

1. [设备相关问题](#设备相关问题)
2. [安装和配置问题](#安装和配置问题)
3. [录制问题](#录制问题)
4. [性能和质量问题](#性能和质量问题)
5. [开发和扩展问题](#开发和扩展问题)

---

## 📷 设备相关问题

### Q1: 如何判断我的摄像头是否支持 UVC？

**A**: 可以通过以下方法判断：

**方法 1: 操作系统识别**
- **Windows**: 打开"相机"应用，能正常使用即支持 UVC
- **macOS**: 打开"Photo Booth"，能正常使用即支持 UVC
- **Linux**: 运行 `lsusb | grep -i video`，有输出即支持

**方法 2: 查看设备说明书**
- 查找 "UVC"、"USB Video Class" 或 "免驱" 关键字

**方法 3: 尝试使用**
- 如果能直接使用（无需安装驱动），通常支持 UVC

---

### Q2: 南天/哲林高拍仪如何切换到 UVC 模式？

**A**: 部分高拍仪默认使用厂商私有驱动，需要切换到 UVC 模式：

**方法 1: 设备按钮切换**
1. 查看高拍仪是否有 "模式切换" 按钮
2. 按下按钮切换到 "UVC" 或 "标准" 模式

**方法 2: 软件工具切换**
1. 安装厂商提供的切换工具（如南天的 "UVC 切换工具"）
2. 运行工具并切换到 UVC 模式

**方法 3: 联系厂商**
- 如果以上方法无效，联系厂商技术支持

---

### Q3: 为什么我的设备无法被识别？

**A**: 可能的原因和解决方案：

1. **设备未连接**: 检查 USB 线是否插好，尝试其他 USB 接口
2. **驱动不兼容**: 确保设备支持 UVC/UAC 标准
3. **设备被占用**: 关闭其他使用设备的程序（如 Skype、Zoom）
4. **权限不足**: 
   - Windows: 以管理员身份运行
   - macOS: 在 "系统偏好设置 → 安全性与隐私" 中授权
   - Linux: 将用户添加到 `video` 和 `audio` 组

---

### Q4: 可以同时使用多个摄像头吗？

**A**: 可以，但有以下限制：

1. **USB 带宽**: 多个摄像头会占用大量 USB 带宽，建议使用 USB 3.0+ 并分散到不同的 USB 控制器
2. **性能限制**: 多个摄像头同时录制会占用大量 CPU 和内存
3. **代码示例**:

```java
// 打开多个摄像头
ICameraDevice camera1 = new JavaCVCamera("camera_0", "Camera 1", "...");
ICameraDevice camera2 = new JavaCVCamera("camera_1", "Camera 2", "...");

camera1.open(VideoFormat.Presets.HD720());
camera2.open(VideoFormat.Presets.HD720());

// 分别录制
camera1.startRecording("output1.mp4");
camera2.startRecording("output2.mp4");
```

---

## 🔧 安装和配置问题

### Q5: Maven 依赖下载很慢怎么办？

**A**: 可以配置 Maven 镜像加速下载：

**方法 1: 使用阿里云镜像**

编辑 `~/.m2/settings.xml`（如果没有则创建）：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

**方法 2: 使用国内 Maven 仓库**

在 `pom.xml` 中添加：

```xml
<repositories>
  <repository>
    <id>aliyun</id>
    <url>https://maven.aliyun.com/repository/public</url>
  </repository>
</repositories>
```

---

### Q6: OpenCV 本地库加载失败怎么办？

**A**: JavaCV 会自动下载和管理本地库，但有时会失败。可以手动配置：

**方法 1: 设置本地库路径**

```java
// 设置 OpenCV 本地库路径
System.setProperty("java.library.path", "path/to/opencv/dll");
```

**方法 2: 手动下载本地库**

1. 访问 https://github.com/bytedeco/javacv/releases
2. 下载对应平台的本地库（如 `opencv-4.7.0-windows-x86_64.jar`）
3. 放入项目的 `lib/` 目录
4. 添加到 classpath

---

### Q7: 如何在不同操作系统上运行？

**A**: 项目是跨平台的，但需要注意：

**Windows**:
- 确保安装 Visual C++ Redistributable
- 以管理员身份运行（避免权限问题）

**macOS**:
- 安装 Homebrew: `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`
- 安装 OpenCV: `brew install opencv`
- 授予摄像头和麦克风权限

**Linux**:
- 安装依赖: `sudo apt-get install libopencv-dev ffmpeg v4l-utils`
- 配置用户权限: `sudo usermod -a -G video,audio $USER`

---

## 🎥 录制问题

### Q8: 录制的视频无法播放怎么办？

**A**: 可能的原因和解决方案：

1. **编码参数错误**: 使用标准编码参数（H.264 + AAC）
   ```java
   config.setVideoFormat(VideoFormat.Presets.HD720());
   config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());
   ```

2. **文件未正确关闭**: 确保调用 `stopRecording()`
   ```java
   try {
       manager.startRecording(config);
       Thread.sleep(5000);
   } finally {
       manager.stopRecording();  // 重要：必须调用
   }
   ```

3. **播放器不支持**: 使用 VLC 或 PotPlayer 等支持多格式的播放器

---

### Q9: 如何录制更长的时间（如 1 小时）？

**A**: 可以设置最大录制时长，或禁用自动停止：

**方法 1: 设置最大时长**

```java
RecordingConfig config = new RecordingConfig("output/long_recording.mp4");
config.setMaxDuration(3600000);  // 60 分钟（毫秒）
```

**方法 2: 自动分割文件**

```java
// 每小时自动分割文件
config.setMaxDuration(3600000);
config.setAutoSplit(true);
config.setFileNamePattern("output/recording_YYYYMMDD_HHmmss.mp4");
```

**方法 3: 监听录制状态并重新启动**

```java
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingStopped(File outputFile, long duration) {
        // 录制停止后，自动重新开始
        if (duration >= 3600000) {
            RecordingConfig newConfig = new RecordingConfig("output/recording_" + System.currentTimeMillis() + ".mp4");
            manager.startRecording(newConfig);
        }
    }
});
```

---

### Q10: 录制的音视频不同步怎么办？

**A**: 音视频不同步通常是因为编码延迟。解决方案：

1. **使用相同的 timebase**

```java
recorder.setVideoTimebase(1.0 / 30.0);  // 30 FPS
recorder.setAudioTimebase(1.0 / 30.0);
```

2. **降低分辨率或帧率**

```java
config.setVideoFormat(VideoFormat.Presets.HD720());  // 降低从 1080p 到 720p
```

3. **使用硬件加速编码**

```java
// 使用 NVIDIA NVENC（需要 NVIDIA 显卡）
recorder.setVideoCodecName("h264_nvenc");
```

---

## ⚡ 性能和质量问题

### Q11: 录制时 CPU 占用很高怎么办？

**A**: 可以通过以下方法降低 CPU 占用：

1. **降低分辨率或帧率**

```java
// 从 1080p 降到 720p
config.setVideoFormat(VideoFormat.Presets.HD720());

// 从 60 FPS 降到 30 FPS
VideoFormat format = new VideoFormat(1280, 720, 30.0, "H.264", 2000000);
```

2. **使用硬件加速编码**

```java
// NVIDIA GPU
recorder.setVideoCodecName("h264_nvenc");

// Intel GPU
recorder.setVideoCodecName("h264_qsv");

// AMD GPU
recorder.setVideoCodecName("h264_amf");
```

3. **降低比特率**

```java
VideoFormat format = new VideoFormat(1280, 720, 30.0, "H.264", 1000000);  // 1 Mbps
```

---

### Q12: 如何提高录制画质？

**A**: 可以通过以下方法提高画质：

1. **提高分辨率和比特率**

```java
// 使用 1080p 和高比特率
VideoFormat format = new VideoFormat(1920, 1080, 30.0, "H.264", 5000000);  // 5 Mbps
config.setVideoFormat(format);
```

2. **使用更高质量的编码**

```java
// H.265 比 H.264 更高效
recorder.setVideoCodec(avcodec.AV_CODEC_ID_H265);
```

3. **调整摄像头参数**

```java
ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
manager.setVideoSource(camera);
camera.setVideoParameters(50, 50, 50);  // 亮度、对比度、饱和度（0-100）
```

---

### Q13: 录制的音频有杂音怎么办？

**A**: 音频杂音可能来自多个原因：

1. **启用软件降噪**

```java
IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();
manager.setAudioSource(microphone);
microphone.setAudioParameters(80, 50, 80);  // 音量、增益、降噪（0-100）
```

2. **使用高质量麦克风**

- 推荐使用 USB 麦克风（如 Blue Yeti）
- 避免使用笔记本内置麦克风（通常质量较差）

3. **调整麦克风位置**

- 避免靠近噪音源（如风扇、空调）
- 使用防喷罩（Pop Filter）

---

## 💻 开发和扩展问题

### Q14: 如何添加新的设备类型支持？

**A**: 通过实现 `ICameraDevice` 或 `IAudioDevice` 接口：

**示例：添加 IP 摄像头支持**

```java
public class IPCamera implements ICameraDevice {
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    
    public IPCamera(String deviceId, String deviceName, String ipAddress) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
    }
    
    @Override
    public boolean open(VideoFormat format) {
        // 实现打开 IP 摄像头逻辑
        return true;
    }
    
    @Override
    public VideoFrame capturePhoto() {
        // 实现拍照逻辑（通过 RTSP/HTTP 抓取帧）
        return null;
    }
    
    // 实现其他接口方法...
}
```

---

### Q15: 如何添加新的编码格式支持？

**A**: 通过扩展 `VideoFormat` 和 `AudioFormat`：

**示例：添加 H.265 编码支持**

```java
// 创建 H.265 视频格式
VideoFormat hevcFormat = new VideoFormat(1920, 1080, 30.0, "H.265", 3000000);

// 使用 FFmpegFrameRecorder 录制
recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
recorder.setVideoFormat("hevc");
```

---

### Q16: 如何集成到现有项目？

**A**: 可以通过以下方式集成：

**方法 1: 作为 Maven 依赖**

1. 将项目安装到本地 Maven 仓库：
   ```bash
   mvn clean install
   ```

2. 在其他项目中添加依赖：
   ```xml
   <dependency>
       <groupId>com.recording</groupId>
       <artifactId>recording-system</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

**方法 2: 作为 JAR 文件**

1. 打包项目：
   ```bash
   mvn clean package
   ```

2. 将生成的 `recording-system-1.0.0.jar` 添加到项目的 classpath

**方法 3: 直接复制源码**

- 将 `src/main/java/com/recording/` 目录复制到你的项目
- 确保所有依赖（JavaCV, OpenCV, FFmpeg）已添加

---

## 📞 获取更多帮助

如果以上 FAQ 无法解决你的问题，请：

1. 查看 [故障排除](Troubleshooting) 文档
2. 在 GitHub 提交 Issue：https://github.com/liuhj-dev/czjk/issues
3. 联系开发团队

---

## 🔙 返回首页

[返回 Wiki 首页](Home)

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
