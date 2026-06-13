# API 文档

## 概述

本文档详细描述无驱动录音录像系统的 API 接口和使用方法。

## 核心接口

### 1. ICameraDevice - 摄像头设备接口

定义摄像头设备的通用操作。

**主要方法：**

#### getDeviceId()
```java
String getDeviceId()
```
获取设备唯一标识符。

#### getDeviceName()
```java
String getDeviceName()
```
获取设备名称。

#### open(VideoFormat format)
```java
boolean open(VideoFormat format)
```
打开摄像头设备并设置视频格式。

**参数：**
- `format`: 视频格式（分辨率、帧率等）

**返回：**
- `true`: 成功打开
- `false`: 打开失败

#### startPreview(PreviewCallback callback)
```java
boolean startPreview(PreviewCallback callback)
```
开始视频预览。

**参数：**
- `callback`: 预览回调接口，用于接收实时视频帧

#### capturePhoto()
```java
VideoFrame capturePhoto()
```
捕获单张图片。

**返回：**
- `VideoFrame`: 包含图像数据的帧对象
- `null`: 捕获失败

#### startRecording(String outputPath)
```java
boolean startRecording(String outputPath)
```
开始录像。

**参数：**
- `outputPath`: 输出文件路径（支持 .mp4, .avi 等）

**返回：**
- `true`: 成功开始录像
- `false`: 启动失败

#### stopRecording()
```java
void stopRecording()
```
停止录像。

---

### 2. IAudioDevice - 音频设备接口

定义音频输入设备（麦克风）的通用操作。

**主要方法：**

#### open(AudioFormat format)
```java
boolean open(AudioFormat format)
```
打开音频设备并设置音频格式。

#### startCapture(CaptureCallback callback)
```java
boolean startCapture(CaptureCallback callback)
```
开始音频捕获。

#### startRecording(String outputPath)
```java
boolean startRecording(String outputPath)
```
开始录音。

**参数：**
- `outputPath`: 输出文件路径（支持 .wav, .mp3 等）

#### getAudioLevel()
```java
int getAudioLevel()
```
获取当前音频电平（音量）。

**返回：**
- `0-100`: 音频电平值

---

### 3. IMediaCapture - 媒体捕获管理接口

统一管理音频和视频的捕获、录制和编码。

**主要方法：**

#### initialize()
```java
boolean initialize()
```
初始化捕获系统，扫描并枚举所有可用设备。

#### setVideoSource(ICameraDevice camera)
```java
boolean setVideoSource(ICameraDevice camera)
```
设置视频源（摄像头）。

#### setAudioSource(IAudioDevice audioDevice)
```java
boolean setAudioSource(IAudioDevice audioDevice)
```
设置音频源（麦克风）。

#### startRecording(RecordingConfig config)
```java
boolean startRecording(RecordingConfig config)
```
开始同时录音录像。

**参数：**
- `config`: 录制配置对象

#### stopRecording()
```java
void stopRecording()
```
停止录音录像。

#### capturePhoto(File outputFile)
```java
boolean capturePhoto(File outputFile)
```
拍照（仅视频）。

#### setRecordingCallback(RecordingCallback callback)
```java
void setRecordingCallback(RecordingCallback callback)
```
设置录制状态回调接口。

---

### 4. IDeviceDiscovery<T> - 设备发现接口

用于发现和枚举系统中的音视频设备。

**主要方法：**

#### discoverDevices()
```java
List<T> discoverDevices()
```
扫描并列出所有可用设备。

**返回：**
- 设备列表

#### getDeviceById(String deviceId)
```java
T getDeviceById(String deviceId)
```
根据设备 ID 获取设备。

#### getDefaultDevice()
```java
T getDefaultDevice()
```
获取默认设备。

---

## 数据模型

### VideoFormat - 视频格式

```java
public class VideoFormat {
    private int width;          // 宽度（像素）
    private int height;         // 高度（像素）
    private double frameRate;  // 帧率（FPS）
    private String codec;       // 编码格式
    private int bitrate;       // 比特率（bps）
}
```

**预设格式：**
- `VideoFormat.Presets.VGA()`: 640x480@30FPS
- `VideoFormat.Presets.HD720()`: 1280x720@30FPS
- `VideoFormat.Presets.FULL_HD()`: 1920x1080@30FPS
- `VideoFormat.Presets.UHD_4K()`: 3840x2160@30FPS

### AudioFormat - 音频格式

```java
public class AudioFormat {
    private int sampleRate;    // 采样率（Hz）
    private int bitDepth;      // 位深度（bit）
    private int channels;      // 声道数
    private String codec;      // 编码格式
    private int bitrate;      // 比特率（bps）
}
```

**预设格式：**
- `AudioFormat.Presets.CD_QUALITY()`: 44100Hz, 16bit, Stereo
- `AudioFormat.Presets.DVD_QUALITY()`: 48000Hz, 16bit, Stereo
- `AudioFormat.Presets.AAC_HIGH()`: 44100Hz, AAC, 192kbps
- `AudioFormat.Presets.MP3_HIGH()`: 44100Hz, MP3, 320kbps

### RecordingConfig - 录制配置

```java
public class RecordingConfig {
    private String outputPath;         // 输出文件路径
    private VideoFormat videoFormat;    // 视频格式
    private AudioFormat audioFormat;    // 音频格式
    private boolean recordVideo;        // 是否录制视频
    private boolean recordAudio;        // 是否录制音频
    private boolean captureAudioOnly;  // 仅录音模式
    private boolean captureVideoOnly;  // 仅录像模式
    private long maxDuration;          // 最大录制时长（毫秒）
    private long maxFileSize;          // 最大文件大小（字节）
}
```

---

## 回调接口

### ICameraDevice.PreviewCallback

```java
interface PreviewCallback {
    void onPreviewFrame(VideoFrame frame);
    void onError(int errorCode, String errorMessage);
}
```

### IAudioDevice.CaptureCallback

```java
interface CaptureCallback {
    void onAudioData(byte[] audioData, int length);
    void onError(int errorCode, String errorMessage);
}
```

### IMediaCapture.RecordingCallback

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

---

## 使用示例

### 基础示例

```java
// 1. 创建管理器
MediaCaptureManager manager = new MediaCaptureManager();

// 2. 初始化
manager.initialize();

// 3. 选择设备
ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();

// 4. 设置音视频源
manager.setVideoSource(camera);
manager.setAudioSource(microphone);

// 5. 配置并开始录制
RecordingConfig config = new RecordingConfig("output/video.mp4");
config.setVideoFormat(VideoFormat.Presets.HD720());
config.setAudioFormat(AudioFormat.Presets.AAC_HIGH());

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

manager.startRecording(config);

// 6. 等待一段时间后停止
Thread.sleep(5000);
manager.stopRecording();

// 7. 清理资源
manager.destroy();
```

---

## 错误码

| 错误码 | 描述 |
|--------|------|
| 100    | 设备未找到 |
| 101    | 设备已被占用 |
| 102    | 设备打开失败 |
| 200    | 录制配置无效 |
| 201    | 输出文件路径无效 |
| 202    | 开始录制失败 |
| 300    | 编码错误 |
| 301    | 文件写入失败 |
| 500    | 系统错误 |

---

## 更多信息

- 完整示例代码：见 `src/main/java/com/recording/examples/`
- 实现细节：见 `docs/IMPLEMENTATION.md`
- 设备兼容性：见 `docs/COMPATIBILITY.md`
