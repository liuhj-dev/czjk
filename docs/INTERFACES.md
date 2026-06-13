# 接口设计文档

## 1. 设计概述

本文档详细描述无驱动录音录像系统的接口设计理念、架构分层和各层接口定义。

---

## 2. 架构分层

系统采用经典的三层架构：

```
┌─────────────────────────────────────────┐
│   应用层 (Application Layer)              │
│   - 示例使用代码                          │
│   - 业务逻辑封装                          │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│   接口层 (API Layer)                      │
│   - ICameraDevice                         │
│   - IAudioDevice                         │
│   - IMediaCapture                        │
│   - IDeviceDiscovery                     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│   实现层 (Implementation Layer)            │
│   - JavaCVCamera (OpenCV)                │
│   - JavaSoundAudio (Java Sound)          │
│   - MediaCaptureManager                   │
│   - CameraDiscovery / AudioDiscovery     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│   底层层 (Native Layer)                   │
│   - OpenCV 本地库                        │
│   - FFmpeg 编码器                        │
│   - Java Sound (DirectSound/ALSA)        │
│   - UVC/UAC 标准驱动（操作系统提供）      │
└─────────────────────────────────────────┘
```

---

## 3. 核心接口设计

### 3.1 ICameraDevice - 摄像头设备接口

**设计目标：** 抽象摄像头设备，屏蔽底层实现差异。

**接口方法分类：**

#### 3.1.1 设备信息
```java
String getDeviceId()           // 唯一标识符
String getDeviceName()         // 用户友好名称
String getDescription()        // 设备描述
List<VideoFormat> getSupportedFormats()  // 支持的视频格式
```

#### 3.1.2 设备生命周期
```java
boolean open(VideoFormat format)  // 打开设备
void close()                      // 关闭设备
boolean isOpen()                  // 检查状态
```

#### 3.1.3 视频预览
```java
boolean startPreview(PreviewCallback callback)  // 开始预览
void stopPreview()                                 // 停止预览
```

**设计说明：**
- 采用回调模式推送视频帧，避免轮询
- `PreviewCallback` 在独立线程中调用，保证实时性

#### 3.1.4 拍照和录像
```java
VideoFrame capturePhoto()                // 拍照
boolean startRecording(String outputPath)  // 开始录像
void stopRecording()                      // 停止录像
boolean isRecording()                    // 检查状态
```

**设计说明：**
- `capturePhoto()` 返回 `VideoFrame` 对象，支持后续处理
- 录像功能内置编码和封装，输出标准 MP4/AVI 文件

#### 3.1.5 参数调整
```java
void setVideoParameters(int brightness, int contrast, int saturation)
double getCurrentFPS()
```

---

### 3.2 IAudioDevice - 音频设备接口

**设计目标：** 抽象音频输入设备，提供统一的音频捕获接口。

**接口方法分类：**

#### 3.2.1 设备信息
```java
String getDeviceId()
String getDeviceName()
String getDescription()
List<AudioFormat> getSupportedFormats()
```

#### 3.2.2 设备生命周期
```java
boolean open(AudioFormat format)
void close()
boolean isOpen()
```

#### 3.2.3 音频捕获
```java
boolean startCapture(CaptureCallback callback)  // 开始捕获
void stopCapture()                             // 停止捕获
boolean isCapturing()                         // 检查状态
```

**设计说明：**
- `CaptureCallback` 实时推送 PCM 音频数据
- 支持音频流处理（如实时降噪、增益）

#### 3.2.4 录音
```java
boolean startRecording(String outputPath)
void stopRecording()
boolean isRecording()
```

#### 3.2.5 音频控制
```java
void setAudioParameters(int volume, int boost, int noiseReduction)
int getAudioLevel()
```

**设计说明：**
- `getAudioLevel()` 用于实现音量可视化（VU 表）

---

### 3.3 IMediaCapture - 媒体捕获管理接口

**设计目标：** 协调音视频设备，提供完整的录音录像功能。

**设计模式：**
- **门面模式（Facade）**：封装底层复杂性
- **观察者模式（Observer）**：通过回调通知状态变化

**核心方法：**

#### 3.3.1 系统生命周期
```java
boolean initialize()   // 初始化系统，扫描设备
void destroy()         // 销毁系统，释放资源
```

#### 3.3.2 设备管理
```java
IDeviceDiscovery<ICameraDevice> getCameraDiscovery()
IDeviceDiscovery<IAudioDevice> getAudioDiscovery()
boolean setVideoSource(ICameraDevice camera)
boolean setAudioSource(IAudioDevice audioDevice)
```

#### 3.3.3 录制控制
```java
boolean startRecording(RecordingConfig config)
void stopRecording()
void pauseRecording()
void resumeRecording()
boolean isRecording()
boolean isPaused()
```

**设计说明：**
- `RecordingConfig` 封装所有录制参数
- 支持暂停/恢复（通过独立的录制线程实现）

#### 3.3.4 状态和进度
```java
long getRecordingDuration()  // 当前录制时长（毫秒）
long getRecordingFileSize() // 当前文件大小（字节）
```

#### 3.3.5 回调机制
```java
void setRecordingCallback(RecordingCallback callback)
```

**RecordingCallback 回调方法：**
- `onRecordingStarted(File outputFile)`: 录制开始
- `onRecordingStopped(File outputFile, long duration)`: 录制停止
- `onRecordingPaused()`: 录制暂停
- `onRecordingResumed()`: 录制恢复
- `onRecordingError(int errorCode, String errorMessage)`: 录制错误
- `onRecordingProgress(long duration, long fileSize)`: 录制进度更新

---

### 3.4 IDeviceDiscovery<T> - 设备发现接口

**设计目标：** 提供通用的设备发现和枚举机制。

**设计模式：**
- **泛型接口**：支持不同类型的设备
- **观察者模式**：监听设备连接/断开事件

**核心方法：**

```java
List<T> discoverDevices()              // 扫描并列出所有设备
T getDeviceById(String deviceId)      // 根据 ID 获取设备
T getDeviceByName(String deviceName)   // 根据名称获取设备
T getDefaultDevice()                  // 获取默认设备
boolean isDeviceAvailable(String deviceId)  // 检查设备是否可用
List<T> refreshDevices()              // 刷新设备列表
void setDeviceStatusCallback(DeviceStatusCallback callback)  // 设置状态回调
```

**DeviceStatusCallback 回调方法：**
- `onDeviceConnected(T device)`: 设备连接
- `onDeviceDisconnected(String deviceId)`: 设备断开
- `onDeviceStatusChanged(T device, DeviceStatus status)`: 设备状态变化

**DeviceStatus 枚举：**
- `CONNECTED`: 已连接
- `DISCONNECTED`: 已断开
- `BUSY`: 忙碌（被占用）
- `ERROR`: 错误

---

## 4. 数据模型设计

### 4.1 VideoFormat - 视频格式

**属性：**
- `width` (int): 宽度（像素）
- `height` (int): 高度（像素）
- `frameRate` (double): 帧率（FPS）
- `codec` (String): 编码格式（"H.264", "MJPEG" 等）
- `bitrate` (int): 比特率（bps）

**设计说明：**
- 提供常用预设（Presets），简化配置
- 支持自定义格式，适应特殊需求

### 4.2 AudioFormat - 音频格式

**属性：**
- `sampleRate` (int): 采样率（Hz）
- `bitDepth` (int): 位深度（bit）
- `channels` (int): 声道数（1=单声道，2=立体声）
- `codec` (String): 编码格式（"PCM", "AAC", "MP3" 等）
- `bitrate` (int): 比特率（bps）

### 4.3 RecordingConfig - 录制配置

**属性：**
- `outputPath` (String): 输出文件路径
- `videoFormat` (VideoFormat): 视频格式
- `audioFormat` (AudioFormat): 音频格式
- `recordVideo` (boolean): 是否录制视频
- `recordAudio` (boolean): 是否录制音频
- `captureAudioOnly` (boolean): 仅录音模式
- `captureVideoOnly` (boolean): 仅录像模式
- `maxDuration` (long): 最大录制时长（毫秒，0=无限制）
- `maxFileSize` (long): 最大文件大小（字节，0=无限制）
- `autoSplit` (boolean): 是否自动分割文件
- `fileNamePattern` (String): 文件名模式（日期格式）

**设计说明：**
- 支持音视频组合、纯视频、纯音频三种模式
- 支持自动停止和文件分割，适应长时间录制

### 4.4 VideoFrame - 视频帧

**属性：**
- `data` (byte[]): 帧数据（RGB/BGR/YUV 格式）
- `width` (int): 帧宽度
- `height` (int): 帧高度
- `timestamp` (long): 时间戳（毫秒）
- `frameNumber` (int): 帧序号
- `format` (String): 数据格式

**设计说明：**
- 封装原始帧数据，支持后续处理和编码
- 提供工具方法（如 `calculateFPS()`）

---

## 5. 回调机制设计

### 5.1 设计原则

- **异步回调**：避免阻塞主线程
- **线程安全**：回调在独立线程中执行
- **错误容忍**：单个回调失败不影响整体

### 5.2 PreviewCallback - 预览回调

```java
interface PreviewCallback {
    void onPreviewFrame(VideoFrame frame);
    void onError(int errorCode, String errorMessage);
}
```

**使用场景：**
- 实时预览（GUI 显示）
- 视频帧处理（如人脸识别）

### 5.3 CaptureCallback - 音频捕获回调

```java
interface CaptureCallback {
    void onAudioData(byte[] audioData, int length);
    void onError(int errorCode, String errorMessage);
}
```

**使用场景：**
- 实时音频处理（如降噪、回声消除）
- 音频流传输（如网络直播）

### 5.4 RecordingCallback - 录制回调

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

**使用场景：**
- UI 进度更新
- 自动停止（达到最大时长/文件大小）
- 错误恢复

---

## 6. 扩展性设计

### 6.1 新设备类型支持

通过实现 `ICameraDevice` 或 `IAudioDevice` 接口，可以轻松支持新设备类型：

```java
// 示例：支持 IP 摄像头
public class IPCamera implements ICameraDevice {
    // 实现所有接口方法
}
```

### 6.2 新编码格式支持

通过扩展 `VideoFormat` 和 `AudioFormat`，支持新编码格式：

```java
// 示例：支持 H.265 编码
VideoFormat format = new VideoFormat(1920, 1080, 30.0, "H.265", 2000000);
```

### 6.3 新存储后端支持

通过修改 `IMediaCapture` 实现，支持云存储等后端：

```java
// 示例：录制到云存储
RecordingConfig config = new RecordingConfig("s3://bucket/video.mp4");
```

---

## 7. 线程安全设计

### 7.1 设备访问同步

- 使用 `synchronized` 关键字保护设备状态
- 避免多线程同时访问同一设备

### 7.2 回调线程隔离

- 回调在独立线程中执行，避免阻塞设备线程
- 使用线程池管理回调线程

### 7.3 资源释放

- 在 `destroy()` 方法中等待所有线程结束
- 使用 `Thread.interrupt()` 优雅停止线程

---

## 8. 错误处理设计

### 8.1 异常类型

- `DeviceNotFoundException`: 设备未找到
- `CaptureException`: 捕获失败
- `EncodingException`: 编码失败

### 8.2 错误码规范

| 范围 | 含义 |
|------|------|
| 100-199 | 设备错误 |
| 200-299 | 录制错误 |
| 300-399 | 编码错误 |
| 500-599 | 系统错误 |

---

## 9. 性能优化设计

### 9.1 零拷贝

- 尽量使用直接内存（Direct Memory）
- 避免不必要的数据复制

### 9.2 缓冲机制

- 使用环形缓冲区（Ring Buffer）减少延迟
- 动态调整缓冲区大小

### 9.3 硬件加速

- 利用 GPU 进行视频编码（如 NVENC、QuickSync）
- 利用 DSP 进行音频处理

---

## 10. 测试接口设计

为支持单元测试，提供以下测试接口：

```java
// 模拟摄像头
public class MockCamera implements ICameraDevice {
    // 返回模拟视频帧
}

// 模拟麦克风
public class MockMicrophone implements IAudioDevice {
    // 返回模拟音频数据
}
```

---

## 11. 总结

本系统的接口设计遵循以下原则：

1. **抽象分层**：屏蔽底层复杂性
2. **回调机制**：支持异步事件通知
3. **泛型设计**：提高代码复用性
4. **扩展友好**：方便支持新设备、新格式
5. **线程安全**：保证多线程环境下的稳定性
6. **错误容忍**：优雅处理各种异常情况

通过这些设计，系统具有良好的可维护性、可扩展性和可靠性。
