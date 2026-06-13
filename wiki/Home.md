# 无驱动录音录像系统 - Wiki 首页

欢迎来到**无驱动录音录像系统**的 Wiki 文档！

## 📚 文档导航

| 文档 | 内容 |
|------|------|
| [快速开始](Getting-Started) | 5分钟快速上手指南 |
| [API 参考](API-Reference) | 完整的接口和类文档 |
| [使用示例](Examples) | 代码示例和使用场景 |
| [设备兼容性](Compatibility) | 支持的设备列表 |
| [常见问题](FAQ) | 常见问题解答 |
| [故障排除](Troubleshooting) | 错误诊断和解决方案 |
| [性能优化](Performance) | 优化建议和最佳实践 |

---

## 🎯 项目简介

本项目实现了一个**完全免驱动**的录音录像系统，基于标准 **UVC（USB Video Class）** 和 **UAC（USB Audio Class）** 协议，无需安装任何厂商私有驱动即可使用高拍仪、USB 摄像头和麦克风等设备。

### 核心特性

- ✅ **真正免驱** - 基于 USB 标准协议，操作系统自带驱动
- ✅ **跨平台** - 支持 Windows、macOS、Linux
- ✅ **易扩展** - 接口设计支持新设备、新格式
- ✅ **生产级** - 完整的错误处理、回调机制、线程安全

### 适用场景

- 高拍仪拍照录像功能
- 视频会议录制
- 监控系统
- 在线教育录制
- 替代南天、哲林等厂商私有驱动方案

---

## 🚀 快速链接

- **GitHub 仓库**: https://github.com/liuhj-dev/czjk
- **问题反馈**: https://github.com/liuhj-dev/czjk/issues
- **示例代码**: `src/main/java/com/recording/examples/RecordingExample.java`

---

## 📦 安装

### Maven 依赖

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

### 快速示例

```java
MediaCaptureManager manager = new MediaCaptureManager();
manager.initialize();

ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
manager.setVideoSource(camera);

RecordingConfig config = new RecordingConfig("output.mp4");
manager.startRecording(config);

Thread.sleep(5000);
manager.stopRecording();
manager.destroy();
```

---

## 📖 详细文档

请使用上方的**文档导航**链接查看详细文档。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

MIT License

---

**维护者**: liuhj-dev  
**最后更新**: 2026-06-13
