# 设备兼容性文档

## 1. 概述

本文档列出无驱动录音录像系统支持的设备类型、操作系统和硬件要求。

---

## 2. 支持的摄像头设备

### 2.1 UVC 兼容摄像头

**标准：** USB Video Class (UVC) 1.0, 1.1, 1.5

**支持的设备类型：**
- ✅ USB 网络摄像头（Logitech, Microsoft, HP 等）
- ✅ 高拍仪（大部分现代型号）
- ✅ USB 文档相机
- ✅ 部分 USB 采集卡
- ✅ 部分 USB 显微镜摄像头

**不支持的设备：**
- ❌ 仅提供厂商私有驱动的设备
- ❌ 需要特殊 SDK 的设备（如部分工业相机）

### 2.2 常见品牌和型号

#### 罗技（Logitech）
| 型号 | 分辨率 | 帧率 | 兼容性 |
|------|--------|------|--------|
| C270 | 720p | 30 FPS | ✅ 完全支持 |
| C920 | 1080p | 30 FPS | ✅ 完全支持 |
| C922 | 1080p | 60 FPS | ✅ 完全支持 |
| Brio | 4K | 30 FPS | ✅ 完全支持 |

#### 微软（Microsoft）
| 型号 | 分辨率 | 帧率 | 兼容性 |
|------|--------|------|--------|
| LifeCam HD-3000 | 720p | 30 FPS | ✅ 完全支持 |
| LifeCam Studio | 1080p | 30 FPS | ✅ 完全支持 |

#### 高拍仪（文档相机）
| 品牌 | 型号 | 分辨率 | 兼容性 |
|------|------|--------|--------|
| 南天 | NT-1000 | 1080p | ✅ 完全支持（UVC 模式） |
| 哲林 | ZL-2000 | 1080p | ✅ 完全支持（UVC 模式） |
| 良田 | LS-500 | 720p | ✅ 完全支持 |
| 汉王 | HW-300 | 1080p | ✅ 完全支持 |

**注意：** 南天和哲林的部分型号可能需要切换到 UVC 模式（通过设备按钮或软件切换）。

---

## 3. 支持的音频设备

### 3.1 UAC 兼容麦克风

**标准：** USB Audio Class (UAC) 1.0, 2.0

**支持的设备类型：**
- ✅ USB 麦克风
- ✅ USB 耳机（带麦克风）
- ✅ 部分 USB 音频接口
- ✅ 部分 USB 话放

**不支持的设备：**
- ❌ 仅提供厂商私有驱动的设备
- ❌ 专业音频接口（如部分 Focusrite, PreSonus 型号可能需要专有驱动）

### 3.2 常见品牌和型号

#### USB 麦克风
| 品牌 | 型号 | 采样率 | 兼容性 |
|------|------|--------|--------|
| Blue | Yeti | 48 kHz | ✅ 完全支持 |
| Audio-Technica | AT2020USB+ | 48 kHz | ✅ 完全支持 |
| Razer | Seiren X | 48 kHz | ✅ 完全支持 |

#### USB 耳机
| 品牌 | 型号 | 采样率 | 兼容性 |
|------|------|--------|--------|
| Logitech | H390 | 44.1 kHz | ✅ 完全支持 |
| Microsoft | LifeChat LX-3000 | 44.1 kHz | ✅ 完全支持 |
| Jabra | Evolve 40 | 48 kHz | ✅ 完全支持 |

---

## 4. 操作系统兼容性

### 4.1 Windows

| 版本 | 架构 | 兼容性 | 备注 |
|------|------|--------|------|
| Windows 7 | x86, x64 | ✅ 支持 | 需要安装 KB2670838 更新 |
| Windows 8.1 | x86, x64 | ✅ 支持 | - |
| Windows 10 | x64 | ✅ 完全支持 | 推荐 |
| Windows 11 | x64 | ✅ 完全支持 | 推荐 |
| Windows Server 2016+ | x64 | ✅ 支持 | 需要启用桌面体验 |

**依赖：**
- Visual C++ Redistributable 2015+ (x64)
- DirectX 9.0c+ (可选，用于硬件加速）

### 4.2 macOS

| 版本 | 架构 | 兼容性 | 备注 |
|------|------|--------|------|
| macOS 10.12 (Sierra) | x64 | ✅ 支持 | - |
| macOS 10.13 (High Sierra) | x64 | ✅ 支持 | - |
| macOS 10.14 (Mojave) | x64 | ✅ 支持 | - |
| macOS 10.15 (Catalina) | x64 | ✅ 完全支持 | 推荐 |
| macOS 11 (Big Sur) | ARM64, x64 | ✅ 完全支持 | 推荐 |
| macOS 12+ (Monterey, Ventura) | ARM64, x64 | ✅ 完全支持 | 推荐 |

**依赖：**
- Xcode Command Line Tools
- OpenCV 4.0+ (通过 Homebrew 安装：`brew install opencv`)

**权限要求：**
- 需要在"系统偏好设置 → 安全性与隐私 → 隐私 → 摄像头"中授权
- 需要在"系统偏好设置 → 安全性与隐私 → 隐私 → 麦克风"中授权

### 4.3 Linux

| 发行版 | 版本 | 架构 | 兼容性 | 备注 |
|--------|------|--------|--------|------|
| Ubuntu | 18.04+ | x64, ARM64 | ✅ 完全支持 | 推荐 |
| Debian | 9+ | x64, ARM64 | ✅ 支持 | - |
| Fedora | 30+ | x64 | ✅ 支持 | - |
| CentOS | 7+ | x64 | ✅ 支持 | - |
| Raspberry Pi OS | 10+ | ARM64 | ✅ 支持 | 需要 V4L2 驱动 |

**依赖（Ubuntu/Debian）：**
```bash
sudo apt-get update
sudo apt-get install -y \
    libopencv-dev \
    ffmpeg \
    v4l-utils \
    pulseaudio \
    libpulse-dev
```

**用户权限：**
```bash
sudo usermod -a -G video $USER
sudo usermod -a -G audio $USER
```

---

## 5. 硬件要求

### 5.1 最低配置

| 组件 | 要求 |
|------|------|
| CPU | Intel Core i3 或 AMD 等效（2 核 2.0 GHz+） |
| 内存 | 2 GB RAM |
| 硬盘 | 100 MB（不含录制文件） |
| USB | USB 2.0+ |
| 显卡 | 集成显卡（Intel HD 4000+） |

### 5.2 推荐配置

| 组件 | 要求 |
|------|------|
| CPU | Intel Core i5/i7 或 AMD Ryzen 5/7（4 核 3.0 GHz+） |
| 内存 | 4-8 GB RAM |
| 硬盘 | 1 GB（不含录制文件） |
| USB | USB 3.0+ |
| 显卡 | 独立显卡（NVIDIA GTX 1050+ 或 AMD RX 560+） |

### 5.3 4K 录制配置

| 组件 | 要求 |
|------|------|
| CPU | Intel Core i7/i9 或 AMD Ryzen 7/9（8 核 3.5 GHz+） |
| 内存 | 16 GB RAM |
| 硬盘 | SSD（读写速度 500 MB/s+） |
| USB | USB 3.0+ |
| 显卡 | NVIDIA RTX 3060+ 或 AMD RX 6700XT+ |

---

## 6. Java 版本兼容性

| JDK 版本 | 兼容性 | 备注 |
|----------|--------|------|
| JDK 8 | ✅ 完全支持 | 推荐（稳定性好） |
| JDK 11 (LTS) | ✅ 完全支持 | 推荐（长期支持） |
| JDK 17 (LTS) | ✅ 完全支持 | 推荐（最新 LTS） |
| JDK 21 (LTS) | ✅ 完全支持 | 最新 LTS |
| JDK 22+ | ⚠️ 部分支持 | 可能需要更新 JavaCV |

**注意：** 需要使用 64 位 JDK（x64 或 ARM64）。

---

## 7. 视频格式兼容性

### 7.1 输入格式（摄像头）

| 分辨率 | 帧率 | 编码格式 | 兼容性 |
|--------|------|----------|--------|
| 640x480 (VGA) | 15, 30 FPS | MJPEG, YUV | ✅ 完全支持 |
| 1280x720 (HD) | 15, 30, 60 FPS | MJPEG, H.264 | ✅ 完全支持 |
| 1920x1080 (FHD) | 15, 30, 60 FPS | MJPEG, H.264 | ✅ 完全支持 |
| 3840x2160 (4K) | 15, 30 FPS | H.264, H.265 | ⚠️ 需要硬件加速 |

### 7.2 输出格式

| 格式 | 编码器 | 兼容性 | 备注 |
|------|--------|--------|------|
| MP4 | H.264/AAC | ✅ 完全支持 | 推荐 |
| AVI | MJPEG/PCM | ✅ 支持 | - |
| MKV | H.264/AAC | ✅ 支持 | - |
| MOV | H.264/AAC | ✅ 支持 | macOS 友好 |
| FLV | H.264/AAC | ⚠️ 部分支持 | 不推荐 |

---

## 8. 音频格式兼容性

### 8.1 输入格式（麦克风）

| 采样率 | 位深度 | 声道 | 格式 | 兼容性 |
|--------|--------|------|------|--------|
| 44100 Hz | 16 bit | Mono/Stereo | PCM | ✅ 完全支持 |
| 48000 Hz | 16 bit | Mono/Stereo | PCM | ✅ 完全支持 |
| 96000 Hz | 24 bit | Stereo | PCM | ⚠️ 需要设备支持 |

### 8.2 输出格式

| 格式 | 编码器 | 兼容性 | 备注 |
|------|--------|--------|------|
| WAV | PCM | ✅ 完全支持 | 推荐（无损） |
| MP3 | LAME | ✅ 支持 | 需要 LAME 库 |
| AAC | FFmpeg AAC | ✅ 完全支持 | 推荐（高效） |
| OGG | Vorbis | ⚠️ 部分支持 | 需要 Vorbis 库 |

---

## 9. 已知问题和限制

### 9.1 摄像头问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 设备无法打开 | 设备被占用 | 关闭其他使用摄像头的程序 |
| 画面卡顿 | USB 带宽不足 | 降低分辨率或帧率 |
| 颜色异常 | 白平衡错误 | 在摄像头属性中调整 |
| 无法识别 | 驱动不兼容 | 确保设备支持 UVC 标准 |

### 9.2 音频问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 无音频输入 | 麦克风未选中 | 在系统设置中选择正确的输入设备 |
| 音频延迟 | 缓冲区过大 | 减小 `TargetDataLine` 缓冲区大小 |
| 杂音/回声 | 降噪未启用 | 启用软件降噪或使用带降噪的麦克风 |
| 音量过低 | 增益不足 | 调整麦克风增益（系统设置或代码） |

### 9.3 录制问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 音视频不同步 | 编码延迟 | 使用相同的 timebase |
| 文件过大 | 比特率过高 | 降低视频或音频比特率 |
| 无法播放 | 编码参数错误 | 使用标准编码参数（H.264/AAC） |
| 录制中断 | 磁盘空间不足 | 确保足够的磁盘空间 |

---

## 10. 测试设备清单

以下设备在开发和测试中被验证：

### 10.1 摄像头
- ✅ Logitech C920 (Windows 10, Ubuntu 20.04)
- ✅ Logitech C270 (Windows 10, macOS 11)
- ✅ Microsoft LifeCam HD-3000 (Windows 10)
- ✅ 南天 NT-1000 高拍仪 (Windows 10)
- ✅ 哲林 ZL-2000 高拍仪 (Windows 10)

### 10.2 音频设备
- ✅ Blue Yeti (Windows 10, macOS 11)
- ✅ Logitech H390 (Windows 10)
- ✅ 内置麦克风 (各种设备)

---

## 11. 兼容性测试建议

### 11.1 测试步骤

1. **设备识别测试**
   ```java
   MediaCaptureManager manager = new MediaCaptureManager();
   manager.initialize();
   manager.printAllDevices();
   ```

2. **视频预览测试**
   ```java
   ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
   manager.setVideoSource(camera);
   camera.startPreview((frame) -> {
       System.out.println("帧大小: " + frame.getDataSize());
   });
   Thread.sleep(5000);
   camera.stopPreview();
   ```

3. **音频捕获测试**
   ```java
   IAudioDevice audio = manager.getAudioDiscovery().getDefaultDevice();
   manager.setAudioSource(audio);
   audio.startCapture((data, length) -> {
       System.out.println("音频数据: " + length + " bytes");
   });
   Thread.sleep(5000);
   audio.stopCapture();
   ```

4. **录制测试**
   ```java
   RecordingConfig config = new RecordingConfig("test.mp4");
   manager.startRecording(config);
   Thread.sleep(5000);
   manager.stopRecording();
   
   // 检查文件是否生成
   File file = new File("test.mp4");
   System.out.println("文件大小: " + file.length() + " bytes");
   ```

### 11.2 自动化测试

建议使用 JUnit 编写单元测试：

```java
@Test
public void testCameraDiscovery() {
    CameraDiscovery discovery = new CameraDiscovery();
    List<ICameraDevice> cameras = discovery.discoverDevices();
    assertNotNull(cameras);
    // 至少找到一个摄像头
    assertTrue(cameras.size() > 0);
}
```

---

## 12. 总结

本系统的设备兼容性总结：

✅ **完全支持：**
- UVC 兼容摄像头
- UAC 兼容麦克风
- Windows 10/11, macOS 10.15+, Ubuntu 18.04+
- JDK 8, 11, 17, 21
- MP4 (H.264/AAC) 输出

⚠️ **部分支持：**
- 4K 录制（需要硬件加速）
- MP3/OGG 编码（需要额外库）
- 专业音频接口

❌ **不支持：**
- 厂商私有驱动设备
- 需要特殊 SDK 的设备

---

## 13. 获取支持

如果遇到设备兼容性问题，请：

1. 查看本文档的"已知问题"部分
2. 在 GitHub 提交 Issue（附上设备型号和操作系统信息）
3. 联系开发团队（提供详细错误日志）

---

**文档版本：** 1.0
**最后更新：** 2024-01-01
