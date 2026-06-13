# 故障排除

本文档提供无驱动录音录像系统的**错误诊断**和**解决方案**。

---

## 📚 目录

1. [设备无法识别](#设备无法识别)
2. [录制失败](#录制失败)
3. [性能问题](#性能问题)
4. [输出文件问题](#输出文件问题)
5. [运行时错误](#运行时错误)

---

## 📷 设备无法识别

### 问题 1: 摄像头设备未找到

**症状**:
```
未找到摄像头设备
cameraDiscovery.discoverDevices() 返回空列表
```

**诊断步骤**:

1. **检查设备连接**
   ```bash
   # Windows: 打开设备管理器，查看"摄像头"类别
   # macOS: 打开"关于本机 → 系统报告 → 摄像头"
   # Linux: 运行 lsusb | grep -i camera
   ```

2. **检查 UVC 支持**
   - 确保设备支持 UVC 标准
   - 尝试在其他应用（如"相机"应用）中使用设备

3. **检查权限**
   ```bash
   # Linux: 检查用户权限
   groups $USER
   # 应该包含 "video" 组
   
   # 如果没有，添加权限
   sudo usermod -a -G video $USER
   ```

**解决方案**:

1. **重新插拔设备**
   - 拔掉 USB 线，等待 5 秒，重新插入
   - 尝试其他 USB 接口（推荐 USB 3.0+）

2. **重启应用或系统**
   - 关闭所有使用摄像头的程序
   - 重启系统

3. **检查设备是否被占用**
   ```java
   // 列出所有进程，查看是否有其他程序占用摄像头
   // Windows: 任务管理器 → 详细信息
   // macOS/Linux: lsof | grep video
   ```

---

### 问题 2: 音频设备未找到

**症状**:
```
未找到音频设备
audioDiscovery.discoverDevices() 返回空列表
```

**诊断步骤**:

1. **检查设备连接**
   - 确保麦克风或耳机已插入
   - 尝试在其他应用（如"语音录音机"）中使用设备

2. **检查 UAC 支持**
   - 确保设备支持 UAC 标准
   - 查看设备说明书是否标注"免驱"

3. **检查系统设置**
   - **Windows**: 设置 → 系统 → 声音 → 输入设备
   - **macOS**: 系统偏好设置 → 声音 → 输入
   - **Linux**: `pavucontrol` 或 `alsamixer`

**解决方案**:

1. **设置默认音频设备**
   ```java
   // 列出所有音频设备
   List<IAudioDevice> devices = manager.getAudioDiscovery().discoverDevices();
   for (IAudioDevice device : devices) {
       System.out.println(device.getDeviceName());
   }
   
   // 手动选择设备
   IAudioDevice microphone = devices.get(0);
   manager.setAudioSource(microphone);
   ```

2. **检查 Java Sound 混音器**
   ```java
   // 列出所有 Java Sound 混音器
   Mixer.Info[] mixers = AudioSystem.getMixerInfo();
   for (Mixer.Info info : mixers) {
       System.out.println(info.getName() + ": " + info.getDescription());
   }
   ```

---

## 🎥 录制失败

### 问题 3: 开始录制失败

**症状**:
```
manager.startRecording(config) 返回 false
或抛出异常
```

**诊断步骤**:

1. **检查设备是否已打开**
   ```java
   ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
   if (!camera.isOpen()) {
       camera.open(VideoFormat.Presets.HD720());
   }
   ```

2. **检查输出路径权限**
   ```java
   File outputDir = new File("output");
   if (!outputDir.exists()) {
       outputDir.mkdirs();
   }
   
   // 检查是否有写入权限
   if (!outputDir.canWrite()) {
       System.err.println("无写入权限: " + outputDir.getAbsolutePath());
   }
   ```

3. **检查录制配置**
   ```java
   RecordingConfig config = new RecordingConfig("output/recording.mp4");
   
   // 检查视频格式是否有效
   if (config.getVideoFormat() == null) {
       config.setVideoFormat(VideoFormat.Presets.HD720());
   }
   
   // 检查音频格式是否有效
   if (config.getAudioFormat() == null) {
       config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());
   }
   ```

**解决方案**:

1. **使用绝对路径**
   ```java
   // 不要使用相对路径
   RecordingConfig config = new RecordingConfig("C:/Users/Administrator/output/recording.mp4");
   ```

2. **检查磁盘空间**
   ```bash
   # Windows: 在文件资源管理器中查看磁盘空间
   # macOS/Linux: df -h
   ```

3. **查看详细错误日志**
   ```java
   manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
       @Override
       public void onRecordingError(int errorCode, String errorMessage) {
           System.err.println("错误码: " + errorCode);
           System.err.println("错误信息: " + errorMessage);
           // 根据错误码查找解决方案
       }
   });
   ```

---

### 问题 4: 录制中断或停止

**症状**:
```
录制过程中突然停止
或文件损坏
```

**诊断步骤**:

1. **检查是否达到最大时长/文件大小**
   ```java
   RecordingConfig config = new RecordingConfig("output/recording.mp4");
   config.setMaxDuration(60000);  // 60 秒
   config.setMaxFileSize(100 * 1024 * 1024);  // 100 MB
   
   // 如果达到限制，录制会自动停止
   ```

2. **检查磁盘空间**
   - 确保磁盘有足够空间（至少 1 GB 可用）

3. **检查设备连接**
   - 录制过程中设备是否断开？
   - USB 线是否松动？

**解决方案**:

1. **禁用自动停止**
   ```java
   config.setMaxDuration(0);  // 0 = 无限制
   config.setMaxFileSize(0);   // 0 = 无限制
   ```

2. **监听录制状态**
   ```java
   manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
       @Override
       public void onRecordingStopped(File outputFile, long duration) {
           System.out.println("录制停止: " + outputFile.getName());
           // 检查是否是异常停止
           if (duration < 1000) {
               System.err.println("警告: 录制时间过短，可能有错误");
           }
       }
   });
   ```

---

## ⚡ 性能问题

### 问题 5: CPU 占用过高

**症状**:
```
录制时 CPU 占用 80%+ 
系统卡顿
```

**诊断步骤**:

1. **检查录制参数**
   ```java
   // 高分辨率和高帧率会占用更多 CPU
   VideoFormat format = config.getVideoFormat();
   System.out.println("分辨率: " + format.getWidth() + "x" + format.getHeight());
   System.out.println("帧率: " + format.getFrameRate() + " FPS");
   ```

2. **检查是否使用硬件加速**
   ```java
   // 如果没有配置硬件加速，会使用软件编码（占用 CPU）
   // 检查 FFmpegFrameRecorder 的配置
   ```

**解决方案**:

1. **降低分辨率或帧率**
   ```java
   // 从 1080p 降到 720p
   config.setVideoFormat(VideoFormat.Presets.HD720());
   
   // 从 60 FPS 降到 30 FPS
   VideoFormat format = new VideoFormat(1280, 720, 30.0, "H.264", 2000000);
   config.setVideoFormat(format);
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
   config.setVideoFormat(format);
   ```

---

### 问题 6: 内存占用过高

**症状**:
```
Java 堆内存溢出 (OutOfMemoryError)
或系统内存不足
```

**诊断步骤**:

1. **检查 Java 堆内存设置**
   ```bash
   # 查看当前堆内存设置
   java -XX:+PrintFlagsFinal -version | grep HeapSize
   ```

2. **检查是否有内存泄漏**
   - 确保每次 `open()` 后都有对应的 `close()` 调用
   - 使用 `manager.destroy()` 释放所有资源

**解决方案**:

1. **增加 Java 堆内存**
   ```bash
   # 运行程序时指定堆内存
   java -Xms512m -Xmx2048m -jar your-app.jar
   ```

2. **及时释放资源**
   ```java
   try {
       manager.initialize();
       // 使用管理器
   } finally {
       manager.destroy();  // 确保资源释放
   }
   ```

3. **避免同时打开太多设备**
   ```java
   // 不要同时打开多个摄像头（除非必要）
   // 使用完一个设备后，及时 close()
   ```

---

## 📁 输出文件问题

### 问题 7: 录制的视频无法播放

**症状**:
```
生成的 MP4 文件无法在播放器中播放
或播放时只有声音没有画面（或反之）
```

**诊断步骤**:

1. **检查文件是否完整**
   ```bash
   # 检查文件大小
   ls -lh output/recording.mp4
   
   # 如果文件大小为 0 或很小，说明录制失败
   ```

2. **检查编码格式**
   ```java
   // 使用标准编码格式
   config.setVideoFormat(VideoFormat.Presets.HD720());  // H.264
   config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());  // AAC
   ```

3. **使用 FFprobe 分析文件**
   ```bash
   # 安装 FFmpeg（包含 ffprobe）
   # Windows: https://ffmpeg.org/download.html
   # macOS: brew install ffmpeg
   # Ubuntu: sudo apt-get install ffmpeg
   
   # 分析文件
   ffprobe output/recording.mp4
   ```

**解决方案**:

1. **确保正确停止录制**
   ```java
   try {
       manager.startRecording(config);
       Thread.sleep(5000);
   } finally {
       manager.stopRecording();  // 重要：必须调用，否则文件不完整
   }
   ```

2. **使用标准封装格式**
   ```java
   // 使用 MP4 封装（最广泛兼容）
   RecordingConfig config = new RecordingConfig("output/recording.mp4");
   recorder.setFormat("mp4");
   ```

3. **尝试其他播放器**
   - 推荐使用 VLC Media Player（支持几乎所有格式）
   - 下载地址: https://www.videolan.org/vlc/

---

### 问题 8: 音视频不同步

**症状**:
```
播放录制视频时，音频和视频不同步
（如：画面比声音慢）
```

**诊断步骤**:

1. **检查编码延迟**
   - 软件编码（如 x264）会有延迟
   - 硬件加速编码延迟更低

2. **检查时间戳**
   ```java
   // 确保音视频使用相同的时间基准（timebase）
   ```

**解决方案**:

1. **使用硬件加速编码**
   ```java
   // NVIDIA GPU 编码延迟最低
   recorder.setVideoCodecName("h264_nvenc");
   ```

2. **降低分辨率或帧率**
   ```java
   // 高分辨率会导致编码延迟
   config.setVideoFormat(VideoFormat.Presets.HD720());  // 降低从 1080p
   ```

3. **调整时间戳**
   ```java
   // 在 JavaCVCamera 中，确保正确设置帧时间戳
   frame.timestamp = System.currentTimeMillis();
   ```

---

## ⚠️ 运行时错误

### 问题 9: OpenCV 本地库加载失败

**症状**:
```
Exception in thread "main" java.lang.UnsatisfiedLinkError:
no opencv_java470 in java.library.path
```

**诊断步骤**:

1. **检查 JavaCV 依赖是否完整**
   ```bash
   # 如果使用 Maven，确保所有依赖都已下载
   mvn clean compile
   ```

2. **检查操作系统和架构**
   - JavaCV 需要下载对应平台的本地库
   - 确保使用 64 位 JDK（32 位不支持）

**解决方案**:

1. **手动指定本地库路径**
   ```java
   // 下载对应平台的本地库（如 opencv-4.7.0-windows-x86_64.jar）
   // 解压后，指定路径
   System.setProperty("java.library.path", "path/to/opencv/dll");
   ```

2. **重新下载 JavaCV 本地库**
   ```bash
   # 删除 Maven 本地仓库中的 JavaCV 文件
   rm -rf ~/.m2/repository/org/bytedeco/
   
   # 重新下载
   mvn clean compile
   ```

---

### 问题 10: Java Sound 音频捕获失败

**症状**:
```
开启音频捕获失败
或捕获的音频数据全为 0
```

**诊断步骤**:

1. **检查音频设备是否被占用**
   - 关闭其他使用麦克风的程序（如 Skype、Zoom）

2. **检查音频格式是否支持**
   ```java
   // 列出所有支持的音频格式
   IAudioDevice device = ...;
   List<AudioFormat> formats = device.getSupportedFormats();
   for (AudioFormat format : formats) {
       System.out.println(format);
   }
   ```

**解决方案**:

1. **使用默认音频格式**
   ```java
   IAudioDevice device = manager.getAudioDiscovery().getDefaultDevice();
   AudioFormat defaultFormat = device.getSupportedFormats().get(0);
   device.open(defaultFormat);
   ```

2. **手动选择音频设备**
   ```java
   // 不要使用默认设备，手动选择一个
   List<IAudioDevice> devices = manager.getAudioDiscovery().discoverDevices();
   IAudioDevice device = devices.get(0);  // 选择第一个设备
   manager.setAudioSource(device);
   ```

---

## 📞 获取更多帮助

如果以上故障排除步骤无法解决你的问题，请：

1. **收集错误信息**
   - 完整的错误日志
   - 设备型号和操作系统版本
   - 录制配置参数

2. **在 GitHub 提交 Issue**
   - 地址: <ADDRESS_REDACTED>
   - 附上详细错误信息和复现步骤

3. **联系开发团队**

---

## 🔙 返回首页

[返回 Wiki 首页](Home)

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
