# 无驱动录音录像系统

## 概述

本系统使用 Java 实现免驱动录音录像功能，适用于高拍仪、USB 摄像头、麦克风等设备。基于标准 UVC（USB Video Class）和 UAC（USB Audio Class）协议，无需安装厂商专用驱动。

## 技术栈

- **视频捕获**：JavaCV（OpenCV 3.x+ 封装）
- **音频捕获**：Java Sound API / TarsosDSP
- **视频编码**：FFmpeg（通过 JavaCV 集成）
- **音频编码**：Java Sound / LAME MP3 Encoder
- **设备发现**：jUSB / libusb Java 封装

## 系统要求

- JDK 8+
- OpenCV 3.x+ 本地库（通过 JavaCV 自动管理）
- FFmpeg 库（可选，用于高级编码）
- 支持 UVC/UAC 的标准 USB 设备

## 快速开始

### 1. 添加依赖（Maven）

```xml
<dependencies>
    <!-- JavaCV - OpenCV 封装 -->
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
    
    <!-- TarsosDSP - 音频处理 -->
    <dependency>
        <groupId>be.tarsos.dsp</groupId>
        <artifactId>core</artifactId>
        <version>2.4</version>
    </dependency>
    
    <!-- USB 设备发现（可选） -->
    <dependency>
        <groupId>org.usb4java</groupId>
        <artifactId>usb4java</artifactId>
        <version>1.3.0</version>
    </dependency>
</dependencies>
```

### 2. 基础使用示例

```java
// 初始化录音录像管理器
MediaCaptureManager manager = new MediaCaptureManager();

// 列出所有可用设备
List<CameraDevice> cameras = manager.listCameras();
List<AudioDevice> microphones = manager.listMicrophones();

// 开始录像
manager.startRecording(
    cameras.get(0),  // 选择摄像头
    microphones.get(0),  // 选择麦克风
    new File("output.mp4")
);

// 5秒后停止
Thread.sleep(5000);
manager.stopRecording();
```

## 项目结构

```
RecordingSystem/
├── src/main/java/com/recording/
│   ├── api/                    # 公共接口
│   │   ├── ICameraDevice.java
│   │   ├── IAudioDevice.java
│   │   ├── IMediaCapture.java
│   │   └── IDeviceDiscovery.java
│   ├── impl/                   # 接口实现
│   │   ├── JavaCVCamera.java
│   │   ├── JavaSoundAudio.java
│   │   ├── MediaCaptureManager.java
│   │   └── USBCameraDiscovery.java
│   ├── model/                  # 数据模型
│   │   ├── CameraDevice.java
│   │   ├── AudioDevice.java
│   │   ├── VideoFormat.java
│   │   ├── AudioFormat.java
│   │   └── RecordingConfig.java
│   ├── exception/              # 自定义异常
│   │   ├── DeviceNotFoundException.java
│   │   ├── CaptureException.java
│   │   └── EncodingException.java
│   └── util/                  # 工具类
│       ├── VideoEncoder.java
│       ├── AudioEncoder.java
│       └── DeviceUtils.java
├── src/test/java/              # 单元测试
├── docs/                       # 文档
└── examples/                   # 示例代码
```

## 核心功能

### 1. 设备发现
- 自动检测 USB 摄像头（UVC 标准）
- 自动检测麦克风（UAC 标准）
- 获取设备能力（分辨率、帧率、采样率等）

### 2. 视频捕获
- 实时预览
- 拍照（JPEG/PNG）
- 录像（MP4/AVI/MOV）
- 支持多种分辨率和帧率

### 3. 音频捕获
- 实时监听
- 录音（WAV/MP3/AAC）
- 降噪和增益控制

### 4. 编码和输出
- H.264/H.265 视频编码
- AAC/MP3 音频编码
- MP4/AVI/MKV 封装

## 文档

详细文档请查看：
- [API 文档](docs/API.md)
- [接口设计](docs/INTERFACES.md)
- [实现指南](docs/IMPLEMENTATION.md)
- [设备兼容性](docs/COMPATIBILITY.md)

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或联系开发团队。
