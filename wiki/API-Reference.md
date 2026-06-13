# API 参考文档

本文档提供无驱动录音录像系统的**完整 API 接口文档**。

---

## 📚 核心接口

### 1. ICameraDevice - 摄像头设备接口

**包路径**: `com.recording.api.ICameraDevice`

#### 方法列表

| 方法 | 说明 |
|------|------|
| `String getDeviceId()` | 获取设备唯一 ID |
| `String getDeviceName()` | 获取设备名称 |
| `String getDescription()` | 获取设备描述 |
| `List<VideoFormat> getSupportedFormats()` | 获取支持的视频格式 |
| `boolean open(VideoFormat format)` | 打开设备并设置格式 |
| `void close()` | 关闭设备 |
| `boolean isOpen()` | 检查设备是否打开 |
| `boolean startPreview(PreviewCallback callback)` | 开始预览 |
| `void stopPreview()` | 停止预览 |
| `VideoFrame capturePhoto()` | 拍照 |
| `boolean startRecording(String outputPath)` | 开始录像 |
| `void stopRecording()` | 停止录像 |
| `boolean isRecording()` | 检查是否正在录像 |
| `void setVideoParameters(int brightness, int contrast, int saturation)` | 设置视频参数 |
| `double getCurrentFPS()` | 获取当前帧率 |

#### 使用示例

```java
ICameraDevice camera = new JavaCVCamera("camera_0", "Logitech C920", "USB Camera");
camera.open(VideoFormat.Presets.HD720());

// 拍照
VideoFrame frame = camera.capturePhoto();
System.out.println("照片大小: " + frame.getDataSize() + " bytes");

// 录像
camera.startRecording("output/video.mp4");
Thread.sleep(5000);
camera.stopRecording();

camera.close();
```

---

### 2. IAudioDevice - 音频设备接口

**包路径**: `com.recording.api.IAudioDevice`

#### 方法列表

| 方法 | 说明 |
|------|------|
| `String getDeviceId()` | 获取设备唯一 ID |
| `String getDeviceName()` | 获取设备名称 |
| `String getDescription()` | 获取设备描述 |
| `List<AudioFormat> getSupportedFormats()` | 获取支持的音频格式 |
| `boolean open(AudioFormat format)` | 打开设备并设置格式 |
| `void close()` | 关闭设备 |
| `boolean isOpen()` | 检查设备是否打开 |
| `boolean startCapture(CaptureCallback callback)` | 开始捕获 |
| `void stopCapture()` | 停止捕获 |
| `boolean startRecording(String outputPath)` | 开始录音 |
| `void stopRecording()` | 停止录音 |
| `boolean isRecording()` | 检查是否正在录音 |
| `void setAudioParameters(int volume, int boost, int noiseReduction)` | 设置音频参数 |
| `int getAudioLevel()` | 获取当前音量电平 |

#### 使用示例

```java
IAudioDevice microphone = new JavaSoundAudio("audio_1", "Blue Yeti", "USB Microphone");
microphone.open(AudioFormat.Presets.CD_QUALITY());

// 录音
microphone.startRecording("output/audio.wav");
Thread.sleep(5000);
microphone.stopRecording();

// 获取音量电平
int level = microphone.getAudioLevel();
System.out.println("音量电平: " + level);

microphone.close();
```

---

### 3. IMediaCapture - 媒体捕获管理接口

**包路径**: `com.recording.api.IMediaCapture`

#### 方法列表

| 方法 | 说明 |
|------|------|
| `boolean initialize()` | 初始化系统 |
| `void destroy()` | 销毁系统，释放资源 |
| `IDeviceDiscovery<ICameraDevice> getCameraDiscovery()` | 获取摄像头发现器 |
| `IDeviceDiscovery<IAudioDevice> getAudioDiscovery()` | 获取音频设备发现器 |
| `boolean setVideoSource(ICameraDevice camera)` | 设置视频源 |
| `boolean setAudioSource(IAudioDevice audioDevice)` | 设置音频源 |
| `boolean startRecording(RecordingConfig config)` | 开始录制 |
| `void stopRecording()` | 停止录制 |
| `void pauseRecording()` | 暂停录制 |
| `void resumeRecording()` | 恢复录制 |
| `boolean isRecording()` | 检查是否正在录制 |
| `boolean isPaused()` | 检查是否暂停 |
| `long getRecordingDuration()` | 获取录制时长（毫秒） |
| `long getRecordingFileSize()` | 获取录制文件大小（字节） |
| `boolean capturePhoto(File outputFile)` | 拍照 |
| `boolean startAudioRecording(File outputFile)` | 开始仅音频录制 |
| `boolean startVideoRecording(File outputFile)` | 开始仅视频录制 |
| `void setRecordingCallback(RecordingCallback callback)` | 设置录制回调 |

#### 回调接口

**RecordingCallback** - 录制状态回调

```java
interface RecordingCallback {
    void onRecordingStarted(File outputFile);
    void onRecordingStopped(File outputFile, long duration);
    void onRecordingPaused();
    void onRecordingResumed();
    void onRecordingError(int errorCode, String errorMessage);
    void onRecordingProgress(long duration, long fileSize);
}
```

#### 使用示例

```java
MediaCaptureManager manager = new MediaCaptureManager();
manager.initialize();

// 设置回调
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingStarted(File outputFile) {
        System.out.println("开始录制: " + outputFile.getName());
    }
    
    @Override
    public void onRecordingProgress(long duration, long fileSize) {
        System.out.printf("进度: %.1f秒, %.2f MB%n",
            duration / 1000.0, fileSize / (1024.0 * 1024.0));
    }
    
    @Override
    public void onRecordingStopped(File outputFile, long duration) {
        System.out.println("录制完成: " + outputFile.getName());
    }
});

// 开始录制
RecordingConfig config = new RecordingConfig("output/recording.mp4");
manager.startRecording(config);

Thread.sleep(5000);
manager.stopRecording();
manager.destroy();
```

---

### 4. IDeviceDiscovery<T> - 设备发现接口

**包路径**: `com.recording.api.IDeviceDiscovery`

#### 方法列表

| 方法 | 说明 |
|------|------|
| `List<T> discoverDevices()` | 扫描并列出所有设备 |
| `T getDeviceById(String deviceId)` | 根据 ID 获取设备 |
| `T getDeviceByName(String deviceName)` | 根据名称获取设备 |
| `T getDefaultDevice()` | 获取默认设备 |
| `boolean isDeviceAvailable(String deviceId)` | 检查设备是否可用 |
| `List<T> refreshDevices()` | 刷新设备列表 |
| `void setDeviceStatusCallback(DeviceStatusCallback callback)` | 设置状态回调 |

#### 回调接口

**DeviceStatusCallback** - 设备状态回调

```java
interface DeviceStatusCallback {
    void onDeviceConnected(T device);
    void onDeviceDisconnected(String deviceId);
    void onDeviceStatusChanged(T device, DeviceStatus status);
}
```

#### 使用示例

```java
MediaCaptureManager manager = new MediaCaptureManager();
manager.initialize();

// 获取所有摄像头
List<ICameraDevice> cameras = manager.getCameraDiscovery().discoverDevices();
for (ICameraDevice camera : cameras) {
    System.out.println("摄像头: " + camera.getDeviceName());
}

// 获取默认摄像头
ICameraDevice defaultCamera = manager.getCameraDiscovery().getDefaultDevice();
System.out.println("默认摄像头: " + defaultCamera.getDeviceName());
```

---

## 📦 数据模型

### VideoFormat - 视频格式

**包路径**: `com.recording.model.VideoFormat`

#### 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `width` | `int` | 宽度（像素） |
| `height` | `int` | 高度（像素） |
| `frameRate` | `double` | 帧率（FPS） |
| `codec` | `String` | 编码格式 |
| `bitrate` | `int` | 比特率（bps） |

#### 预设格式

```java
VideoFormat.Presets.VGA()          // 640x480@30FPS
VideoFormat.Presets.HD720()       // 1280x720@30FPS
VideoFormat.Presets.FULL_HD()     // 1920x1080@30FPS
VideoFormat.Presets.UHD_4K()      // 3840x2160@30FPS
```

---

### AudioFormat - 音频格式

**包路径**: `com.recording.model.AudioFormat`

#### 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `sampleRate` | `int` | 采样率（Hz） |
| `bitDepth` | `int` | 位深度（bit） |
| `channels` | `int` | 声道数 |
| `codec` | `String` | 编码格式 |
| `bitrate` | `int` | 比特率（bps） |

#### 预设格式

```java
AudioFormat.Presets.CD_QUALITY()   // 44100Hz, 16bit, Stereo
AudioFormat.Presets.DVD_QUALITY()  // 48000Hz, 16bit, Stereo
AudioFormat.Presets.AAC_HIGH()     // 44100Hz, AAC, 192kbps
AudioFormat.Presets.MP3_HIGH()     // 44100Hz, MP3, 320kbps
```

---

### RecordingConfig - 录制配置

**包路径**: `com.recording.model.RecordingConfig`

#### 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `outputPath` | `String` | 输出文件路径 |
| `videoFormat` | `VideoFormat` | 视频格式 |
| `audioFormat` | `AudioFormat` | 音频格式 |
| `recordVideo` | `boolean` | 是否录制视频 |
| `recordAudio` | `boolean` | 是否录制音频 |
| `captureAudioOnly` | `boolean` | 仅录音模式 |
| `captureVideoOnly` | `boolean` | 仅录像模式 |
| `maxDuration` | `long` | 最大录制时长（毫秒） |
| `maxFileSize` | `long` | 最大文件大小（字节） |

#### 使用示例

```java
RecordingConfig config = new RecordingConfig("output/recording.mp4");
config.setVideoFormat(VideoFormat.Presets.HD720());
config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());
config.setMaxDuration(60000);  // 最大 60 秒
config.setMaxFileSize(100 * 1024 * 1024);  // 最大 100 MB
```

---

## 🔙 返回首页

[返回 Wiki 首页](Home)

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
