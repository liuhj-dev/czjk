package com.recording.examples;

import com.recording.api.IMediaCapture;
import com.recording.api.ICameraDevice;
import com.recording.api.IAudioDevice;
import com.recording.impl.MediaCaptureManager;
import com.recording.model.RecordingConfig;
import com.recording.model.VideoFormat;
import com.recording.model.AudioFormat;

import java.io.File;

/**
 * 录音录像系统使用示例
 */
public class RecordingExample {
    
    public static void main(String[] args) {
        System.out.println("=== 无驱动录音录像系统示例 ===\n");
        
        // 创建媒体捕获管理器
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            // 1. 初始化系统
            System.out.println("步骤 1: 初始化系统");
            if (!manager.initialize()) {
                System.err.println("初始化失败，程序退出");
                return;
            }
            
            // 2. 打印所有可用设备
            System.out.println("\n步骤 2: 扫描设备");
            manager.printAllDevices();
            
            // 3. 选择设备
            System.out.println("\n步骤 3: 选择设备");
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            IAudioDevice microphone = manager.getAudioDiscovery().getDefaultDevice();
            
            if (camera == null) {
                System.err.println("未找到摄像头设备");
                return;
            }
            
            if (microphone == null) {
                System.err.println("未找到音频设备");
                return;
            }
            
            System.out.println("已选择摄像头: " + camera.getDeviceName());
            System.out.println("已选择麦克风: " + microphone.getDeviceName());
            
            // 4. 设置音视频源
            System.out.println("\n步骤 4: 设置音视频源");
            manager.setVideoSource(camera);
            manager.setAudioSource(microphone);
            
            // 5. 配置录制参数
            System.out.println("\n步骤 5: 配置录制参数");
            RecordingConfig config = new RecordingConfig("output/recording_" + System.currentTimeMillis() + ".mp4");
            config.setVideoFormat(VideoFormat.Presets.HD720());  // 720p
            config.setAudioFormat(AudioFormat.Presets.AAC_HIGH()); // 高质量音频
            config.setRecordVideo(true);
            config.setRecordAudio(true);
            config.setMaxDuration(10000); // 最大 10 秒
            
            System.out.println("录制配置: " + config);
            
            // 6. 设置回调
            System.out.println("\n步骤 6: 设置回调");
            manager.setRecordingCallback(new IMediaCapture.RecordingCallback() {
                @Override
                public void onRecordingStarted(File outputFile) {
                    System.out.println("[回调] 录制开始: " + outputFile.getName());
                }
                
                @Override
                public void onRecordingStopped(File outputFile, long duration) {
                    System.out.println("[回调] 录制停止: " + outputFile.getName() + 
                        " (时长: " + (duration / 1000) + "秒)");
                }
                
                @Override
                public void onRecordingPaused() {
                    System.out.println("[回调] 录制暂停");
                }
                
                @Override
                public void onRecordingResumed() {
                    System.out.println("[回调] 录制恢复");
                }
                
                @Override
                public void onRecordingError(int errorCode, String errorMessage) {
                    System.err.println("[回调] 录制错误: " + errorCode + " - " + errorMessage);
                }
                
                @Override
                public void onRecordingProgress(long duration, long fileSize) {
                    System.out.printf("[回调] 录制进度: %.1f秒, 文件大小: %.2f MB%n",
                        duration / 1000.0, fileSize / (1024.0 * 1024.0));
                }
            });
            
            // 7. 开始录制
            System.out.println("\n步骤 7: 开始录制（10秒）");
            if (!manager.startRecording(config)) {
                System.err.println("开始录制失败");
                return;
            }
            
            // 8. 等待录制完成
            System.out.println("录制中...");
            Thread.sleep(10000); // 等待 10 秒
            
            // 9. 停止录制
            System.out.println("\n步骤 8: 停止录制");
            manager.stopRecording();
            
            // 10. 拍照示例
            System.out.println("\n步骤 9: 拍照");
            File photoFile = new File("output/photo_" + System.currentTimeMillis() + ".jpg");
            if (manager.capturePhoto(photoFile)) {
                System.out.println("拍照成功: " + photoFile.getAbsolutePath());
            } else {
                System.err.println("拍照失败");
            }
            
            // 11. 仅录音示例
            System.out.println("\n步骤 10: 仅录音（5秒）");
            File audioFile = new File("output/audio_" + System.currentTimeMillis() + ".wav");
            manager.startAudioRecording(audioFile);
            Thread.sleep(5000);
            manager.stopRecording();
            System.out.println("录音完成: " + audioFile.getAbsolutePath());
            
            System.out.println("\n=== 示例完成 ===");
            
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 12. 清理资源
            System.out.println("\n步骤 11: 清理资源");
            manager.destroy();
        }
    }
    
    /**
     * 简单的录像示例（不带回调）
     */
    public static void simpleRecordingExample() {
        System.out.println("=== 简单录像示例 ===");
        
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            IAudioDevice audio = manager.getAudioDiscovery().getDefaultDevice();
            
            manager.setVideoSource(camera);
            manager.setAudioSource(audio);
            
            RecordingConfig config = new RecordingConfig("output/simple_recording.mp4");
            manager.startRecording(config);
            
            System.out.println("录制中...按 Enter 停止");
            System.in.read();
            
            manager.stopRecording();
            System.out.println("录制完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.destroy();
        }
    }
    
    /**
     * 仅拍照示例
     */
    public static void photoOnlyExample() {
        System.out.println("=== 仅拍照示例 ===");
        
        MediaCaptureManager manager = new MediaCaptureManager();
        
        try {
            manager.initialize();
            
            ICameraDevice camera = manager.getCameraDiscovery().getDefaultDevice();
            manager.setVideoSource(camera);
            
            for (int i = 0; i < 5; i++) {
                File photo = new File("output/photo_" + i + ".jpg");
                if (manager.capturePhoto(photo)) {
                    System.out.println("拍照 " + (i + 1) + " 成功: " + photo.getName());
                }
                Thread.sleep(1000); // 每秒拍一张
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.destroy();
        }
    }
}
