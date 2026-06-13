# 实现指南

## 1. 概述

本文档提供无驱动录音录像系统的详细实现指南，包括环境搭建、核心类实现、关键技术和最佳实践。

---

## 2. 环境搭建

### 2.1 系统要求

- **操作系统**: Windows 7+, macOS 10.12+, Linux (Kernel 3.2+)
- **JDK**: 8 或更高版本
- **内存**: 至少 2GB RAM（建议 4GB+）
- **硬盘**: 至少 100MB 可用空间（不含录制文件）

### 2.2 依赖安装

#### Maven 依赖配置

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.recording</groupId>
    <artifactId>recording-system</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <!-- JavaCV - OpenCV 封装 -->
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>javacv</artifactId>
            <version>1.5.9</version>
        </dependency>
        
        <!-- OpenCV 平台库 -->
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>opencv-platform</artifactId>
            <version>4.7.0-1.5.9</version>
        </dependency>
        
        <!-- FFmpeg 平台库 -->
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>ffmpeg-platform</artifactId>
            <version>6.0-1.5.9</version>
        </dependency>
        
        <!-- TarsosDSP - 音频处理（可选） -->
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
</project>
```

#### 手动下载 JAR

如果使用非 Maven 项目，可以手动下载以下 JAR 文件：

1. `javacv-1.5.9.jar`
2. `opencv-4.7.0-1.5.9.jar`
3. `ffmpeg-6.0-1.5.9.jar`
4. 对应平台的本地库（如 `opencv-4.7.0-windows-x86_64.jar`）

### 2.3 OpenCV 本地库配置

JavaCV 会自动下载和管理本地库，但也可以手动配置：

```java
// 设置 OpenCV 本地库路径
System.setProperty("java.library.path", "path/to/opencv/dll");

// 或者使用环境变量
// OPENCV_LIBRARY_PATH=path/to/opencv/dll
```

---

## 3. 核心类实现

### 3.1 JavaCVCamera - 摄像头实现

**关键步骤：**

#### 3.1.1 打开摄像头

```java
@Override
public boolean open(VideoFormat format) {
    try {
        // 解析设备 ID（如 "camera_0" → 索引 0）
        int cameraIndex = Integer.parseInt(deviceId.replace("camera_", ""));
        
        // 创建 FrameGrabber
        grabber = new OpenCVFrameGrabber(cameraIndex);
        
        // 设置视频格式
        if (format != null) {
            grabber.setImageWidth(format.getWidth());
            grabber.setImageHeight(format.getHeight());
            grabber.setFrameRate(format.getFrameRate());
        }
        
        // 启动抓帧器
        grabber.start();
        
        currentFormat = format != null ? format : VideoFormat.Presets.HD720();
        isOpen = true;
        return true;
    } catch (Exception e) {
        System.err.println("打开摄像头失败: " + e.getMessage());
        return false;
    }
}
```

**关键点：**
- `OpenCVFrameGrabber` 支持多种后端（DirectShow, V4L2, AVFoundation）
- 设备索引通常从 0 开始

#### 3.1.2 视频预览

```java
@Override
public boolean startPreview(PreviewCallback callback) {
    if (!isOpen) return false;
    
    this.previewCallback = callback;
    this.isPreviewing = true;
    
    previewThread = new Thread(() -> {
        try {
            Frame frame;
            while (isPreviewing && (frame = grabber.grab()) != null) {
                // 转换 Frame 为 VideoFrame
                VideoFrame videoFrame = convertFrameToVideoFrame(frame);
                
                // 回调
                if (previewCallback != null) {
                    previewCallback.onPreviewFrame(videoFrame);
                }
            }
        } catch (Exception e) {
            if (previewCallback != null) {
                previewCallback.onError(500, e.getMessage());
            }
        }
    });
    previewThread.setDaemon(true);
    previewThread.start();
    return true;
}
```

**关键点：**
- 在独立线程中抓取帧，避免阻塞
- 使用回调推送帧，支持实时处理

#### 3.1.3 录像

```java
@Override
public boolean startRecording(String outputPath) {
    if (!isOpen) return false;
    
    try {
        // 创建 FFmpegFrameRecorder
        recorder = new FFmpegFrameRecorder(
            outputPath,
            currentFormat.getWidth(),
            currentFormat.getHeight()
        );
        
        // 配置编码器
        recorder.setFrameRate(currentFormat.getFrameRate());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);  // H.264 编码
        recorder.setVideoBitrate(currentFormat.getBitrate());
        recorder.setFormat("mp4");  // 输出格式
        
        // 启动录制器
        recorder.start();
        isRecording = true;
        return true;
    } catch (Exception e) {
        System.err.println("开始录像失败: " + e.getMessage());
        return false;
    }
}
```

**关键点：**
- 使用 `FFmpegFrameRecorder` 进行编码和封装
- 支持多种编码格式（H.264, H.265, MPEG4 等）

#### 3.1.4 帧转换

```java
private byte[] frameToByteArray(Frame frame) {
    if (frame == null || frame.image == null) {
        return new byte[0];
    }
    
    try {
        // 转换为 IplImage
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        IplImage img = converter.convert(frame);
        
        // 读取像素数据
        ByteBuffer buffer = img.getByteBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    } catch (Exception e) {
        e.printStackTrace();
        return new byte[0];
    }
}
```

---

### 3.2 JavaSoundAudio - 音频实现

**关键步骤：**

#### 3.2.1 打开音频设备

```java
@Override
public boolean open(AudioFormat format) {
    try {
        // 查找 Mixer
        Mixer.Info mixerInfo = findMixerByName(deviceName);
        if (mixerInfo == null) return false;
        
        // 转换 AudioFormat 为 Java Sound 格式
        javax.sound.sampled.AudioFormat audioFormat = 
            new javax.sound.sampled.AudioFormat(
                format.getSampleRate(),
                format.getBitDepth(),
                format.getChannels(),
                true,  // signed
                false   // little-endian
            );
        
        // 获取 TargetDataLine
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        microphone = (TargetDataLine) mixer.getLine(info);
        
        // 打开线路
        microphone.open(audioFormat);
        
        currentFormat = format;
        isOpen = true;
        return true;
    } catch (Exception e) {
        System.err.println("打开音频设备失败: " + e.getMessage());
        return false;
    }
}
```

**关键点：**
- 使用 `TargetDataLine` 捕获音频数据
- 需要正确配置音频格式（采样率、位深度、声道数）

#### 3.2.2 音频捕获

```java
@Override
public boolean startCapture(CaptureCallback callback) {
    if (!isOpen) return false;
    
    this.captureCallback = callback;
    this.isCapturing = true;
    
    microphone.start();
    
    captureThread = new Thread(() -> {
        byte[] buffer = new byte[microphone.getBufferSize() / 2];
        try {
            while (isCapturing) {
                int count = microphone.read(buffer, 0, buffer.length);
                if (count > 0 && captureCallback != null) {
                    captureCallback.onAudioData(buffer, count);
                }
            }
        } catch (Exception e) {
            if (captureCallback != null) {
                captureCallback.onError(500, e.getMessage());
            }
        }
    });
    captureThread.setDaemon(true);
    captureThread.start();
    return true;
}
```

**关键点：**
- 在独立线程中读取音频数据
- 使用回调推送 PCM 数据

#### 3.2.3 保存 WAV 文件

```java
private void saveAudioToFile(byte[] audioData, String filePath) {
    try {
        FileOutputStream fos = new FileOutputStream(filePath);
        
        // 写入 WAV 文件头
        writeWavHeader(fos, audioData.length, currentFormat);
        
        // 写入音频数据
        fos.write(audioData);
        fos.close();
    } catch (Exception e) {
        System.err.println("保存音频文件失败: " + e.getMessage());
    }
}

private void writeWavHeader(FileOutputStream fos, int dataSize, AudioFormat format) throws Exception {
    int sampleRate = (int) format.getSampleRate();
    int bitDepth = format.getBitDepth();
    int channels = format.getChannels();
    int byteRate = sampleRate * channels * bitDepth / 8;
    int blockAlign = channels * bitDepth / 8;
    
    // RIFF header
    fos.write("RIFF".getBytes());
    fos.write(intToBytes(36 + dataSize));
    fos.write("WAVE".getBytes());
    
    // fmt subchunk
    fos.write("fmt ".getBytes());
    fos.write(intToBytes(16));  // PCM
    fos.write(shortToBytes((short) 1));
    fos.write(shortToBytes((short) channels));
    fos.write(intToBytes(sampleRate));
    fos.write(intToBytes(byteRate));
    fos.write(shortToBytes((short) blockAlign));
    fos.write(shortToBytes((short) bitDepth));
    
    // data subchunk
    fos.write("data".getBytes());
    fos.write(intToBytes(dataSize));
}
```

**关键点：**
- WAV 文件由文件头 + PCM 数据组成
- 文件头包含音频格式信息

---

### 3.3 MediaCaptureManager - 核心管理器

**关键步骤：**

#### 3.3.1 初始化

```java
@Override
public boolean initialize() {
    try {
        System.out.println("正在初始化媒体捕获系统...");
        
        // 扫描设备
        List<ICameraDevice> cameras = cameraDiscovery.discoverDevices();
        List<IAudioDevice> audios = audioDiscovery.discoverDevices();
        
        System.out.println("发现 " + cameras.size() + " 个摄像头设备");
        System.out.println("发现 " + audios.size() + " 个音频设备");
        return true;
    } catch (Exception e) {
        System.err.println("初始化失败: " + e.getMessage());
        return false;
    }
}
```

#### 3.3.2 录制监控

```java
private void startRecordingMonitor() {
    recordingThread = new Thread(() -> {
        try {
            while (isRecording) {
                Thread.sleep(1000);  // 每秒更新一次
                
                if (recordingCallback != null && !isPaused) {
                    recordingCallback.onRecordingProgress(
                        getRecordingDuration(),
                        getRecordingFileSize()
                    );
                }
                
                // 检查最大时长限制
                if (currentConfig != null && currentConfig.getMaxDuration() > 0) {
                    if (getRecordingDuration() >= currentConfig.getMaxDuration()) {
                        stopRecording();
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
    recordingThread.setDaemon(true);
    recordingThread.start();
}
```

---

## 4. 关键技术

### 4.1 UVC 标准

**概述：**
UVC（USB Video Class）是 USB 设备的标准协议，支持免驱动使用摄像头。

**实现：**
- Windows: DirectShow / Media Foundation
- Linux: V4L2 (Video4Linux2)
- macOS: AVFoundation

**Java 支持：**
- JavaCV 通过 OpenCV 调用操作系统 API
- 无需安装厂商驱动

### 4.2 UAC 标准

**概述：**
UAC（USB Audio Class）是 USB 音频设备的标准协议。

**实现：**
- Windows: DirectSound / WASAPI
- Linux: ALSA / PulseAudio
- macOS: Core Audio

**Java 支持：**
- Java Sound API 调用操作系统 API
- 支持捕获和播放

### 4.3 视频编码

**H.264 编码：**

```java
recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
```

**关键参数：**
- `bitrate`: 比特率（影响文件大小和画质）
- `framerate`: 帧率（影响流畅度）
- `preset`: 编码速度（ultrafast, fast, medium, slow）

### 4.4 音频编码

**AAC 编码：**

```java
// 需要使用 FFmpegFrameRecorder
recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
recorder.setAudioBitrate(128000);
recorder.setSampleRate(44100);
recorder.setAudioChannels(2);
```

---

## 5. 最佳实践

### 5.1 资源管理

**问题：** 设备占用、内存泄漏

**解决方案：**
```java
try {
    manager.initialize();
    // 使用管理器
} finally {
    manager.destroy();  // 确保资源释放
}
```

### 5.2 错误处理

**问题：** 设备断开、编码失败

**解决方案：**
```java
manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
    @Override
    public void onRecordingError(int errorCode, String errorMessage) {
        System.err.println("录制错误: " + errorMessage);
        // 尝试恢复或停止
        manager.stopRecording();
    }
});
```

### 5.3 性能优化

**问题：** 高 CPU 占用、延迟大

**解决方案：**
1. 使用硬件加速编码（如 NVIDIA NVENC）
2. 降低分辨率或帧率
3. 使用合适的缓冲区大小

```java
// 设置硬件加速（需要 GPU 支持）
recorder.setVideoCodecName("h264_nvenc");
```

### 5.4 多线程安全

**问题：** 多线程访问设备导致冲突

**解决方案：**
```java
public synchronized boolean open(VideoFormat format) {
    // 同步方法，避免并发访问
}
```

---

## 6. 调试技巧

### 6.1 日志输出

```java
// 启用 JavaCV 日志
System.setProperty("org.bytedeco.javacpp.logger", "slf4j");
```

### 6.2 设备测试

```java
// 测试摄像头
ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
if (camera != null) {
    camera.open(VideoFormat.Presets.HD720());
    VideoFrame frame = camera.capturePhoto();
    System.out.println("拍照成功，帧大小: " + frame.getDataSize());
    camera.close();
}
```

### 6.3 性能分析

```java
// 监控帧率
long startTime = System.currentTimeMillis();
int frameCount = 0;

while (recording) {
    Frame frame = grabber.grab();
    frameCount++;
    
    long elapsed = System.currentTimeMillis() - startTime;
    if (elapsed >= 1000) {
        System.out.println("FPS: " + frameCount);
        frameCount = 0;
        startTime = System.currentTimeMillis();
    }
}
```

---

## 7. 常见问题

### Q1: 找不到摄像头设备

**原因：**
- 设备未连接
- 驱动不兼容
- 设备被其他程序占用

**解决：**
```java
// 列出所有摄像头
CameraDiscovery discovery = new CameraDiscovery();
List<ICameraDevice> cameras = discovery.discoverDevices();
for (ICameraDevice camera : cameras) {
    System.out.println(camera.getDeviceName());
}
```

### Q2: 录像文件无法播放

**原因：**
- 编码参数错误
- 文件未正确关闭

**解决：**
```java
// 确保正确停止录制
try {
    manager.startRecording(config);
    Thread.sleep(5000);
} finally {
    manager.stopRecording();  // 重要：必须调用
}
```

### Q3: 音频不同步

**原因：**
- 音视频时间戳不一致
- 编码延迟

**解决：**
```java
// 使用同一个 timebase
recorder.setVideoTimebase(1.0 / 30.0);  // 30 FPS
recorder.setAudioTimebase(1.0 / 30.0);
```

---

## 8. 部署建议

### 8.1 Windows 部署

1. 安装 Visual C++ Redistributable（OpenCV 依赖）
2. 确保摄像头驱动是 UVC 兼容的
3. 以管理员权限运行（避免设备访问权限问题）

### 8.2 Linux 部署

1. 安装 V4L2 工具：`sudo apt-get install v4l-utils`
2. 配置用户权限：`sudo usermod -a -G video $USER`
3. 安装 FFmpeg：`sudo apt-get install ffmpeg`

### 8.3 macOS 部署

1. 安装 Homebrew：`/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`
2. 安装 OpenCV：`brew install opencv`
3. 授予摄像头权限（系统偏好设置 → 安全性与隐私）

---

## 9. 总结

本实现指南涵盖了：

1. **环境搭建** - 依赖配置和本地库管理
2. **核心类实现** - 摄像头、音频、管理器
3. **关键技术** - UVC/UAC、编码、多线程
4. **最佳实践** - 资源管理、错误处理、性能优化
5. **调试技巧** - 日志、测试、性能分析
6. **常见问题** - 解决方案和预防措施

通过遵循本指南，可以成功实现和部署无驱动录音录像系统。
