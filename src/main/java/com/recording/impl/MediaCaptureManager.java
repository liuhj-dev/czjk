package com.recording.impl;

import com.recording.api.ICameraDevice;
import com.recording.api.IAudioDevice;
import com.recording.api.IMediaCapture;
import com.recording.api.IDeviceDiscovery;
import com.recording.model.RecordingConfig;
import com.recording.model.VideoFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体捕获管理器
 * 统一管理摄像头和音频设备，协调录音录像
 */
public class MediaCaptureManager implements IMediaCapture {
    
    private ICameraDevice currentCamera;
    private IAudioDevice currentAudioDevice;
    private IDeviceDiscovery<ICameraDevice> cameraDiscovery;
    private IDeviceDiscovery<IAudioDevice> audioDiscovery;
    private RecordingConfig currentConfig;
    private RecordingCallback recordingCallback;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private long recordingStartTime = 0;
    private Thread recordingThread;
    
    public MediaCaptureManager() {
        // 初始化设备发现
        this.cameraDiscovery = new CameraDiscovery();
        this.audioDiscovery = new AudioDiscovery();
    }
    
    @Override
    public boolean initialize() {
        try {
            System.out.println("正在初始化媒体捕获系统...");
            
            // 扫描设备
            List<ICameraDevice> cameras = cameraDiscovery.discoverDevices();
            List<IAudioDevice> audios = audioDiscovery.discoverDevices();
            
            System.out.println("发现 " + cameras.size() + " 个摄像头设备");
            System.out.println("发现 " + audios.size() + " 个音频设备");
            
            System.out.println("媒体捕获系统初始化成功");
            return true;
        } catch (Exception e) {
            System.err.println("初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void destroy() {
        try {
            if (isRecording) {
                stopRecording();
            }
            
            if (currentCamera != null) {
                currentCamera.close();
            }
            
            if (currentAudioDevice != null) {
                currentAudioDevice.close();
            }
            
            System.out.println("媒体捕获系统已销毁");
        } catch (Exception e) {
            System.err.println("销毁失败: " + e.getMessage());
        }
    }
    
    @Override
    public IDeviceDiscovery<ICameraDevice> getCameraDiscovery() {
        return cameraDiscovery;
    }
    
    @Override
    public IDeviceDiscovery<IAudioDevice> getAudioDiscovery() {
        return audioDiscovery;
    }
    
    @Override
    public boolean setVideoSource(ICameraDevice camera) {
        try {
            if (currentCamera != null && currentCamera.isOpen()) {
                currentCamera.close();
            }
            
            this.currentCamera = camera;
            
            if (!camera.isOpen()) {
                VideoFormat format = camera.getSupportedFormats().get(0);
                camera.open(format);
            }
            
            System.out.println("视频源已设置: " + camera.getDeviceName());
            return true;
        } catch (Exception e) {
            System.err.println("设置视频源失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean setAudioSource(IAudioDevice audioDevice) {
        try {
            if (currentAudioDevice != null && currentAudioDevice.isOpen()) {
                currentAudioDevice.close();
            }
            
            this.currentAudioDevice = audioDevice;
            
            if (!audioDevice.isOpen()) {
                AudioFormat format = audioDevice.getSupportedFormats().get(0);
                audioDevice.open(format);
            }
            
            System.out.println("音频源已设置: " + audioDevice.getDeviceName());
            return true;
        } catch (Exception e) {
            System.err.println("设置音频源失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean startRecording(RecordingConfig config) {
        if (isRecording) {
            System.err.println("已经在录制中");
            return false;
        }
        
        if (currentCamera == null && !config.isCaptureAudioOnly()) {
            System.err.println("未设置视频源");
            return false;
        }
        
        if (currentAudioDevice == null && !config.isCaptureVideoOnly()) {
            System.err.println("未设置音频源");
            return false;
        }
        
        try {
            this.currentConfig = config;
            this.isRecording = true;
            this.isPaused = false;
            this.recordingStartTime = System.currentTimeMillis();
            
            // 开始录像
            if (!config.isCaptureAudioOnly() && currentCamera != null) {
                String videoOutput = config.isCaptureVideoOnly() ? 
                    config.getOutputPath() : 
                    config.getOutputPath().replace(".mp4", "_video.mp4");
                currentCamera.startRecording(videoOutput);
            }
            
            // 开始录音
            if (!config.isCaptureVideoOnly() && currentAudioDevice != null) {
                String audioOutput = config.isCaptureAudioOnly() ? 
                    config.getOutputPath() : 
                    config.getOutputPath().replace(".mp4", "_audio.wav");
                currentAudioDevice.startRecording(audioOutput);
            }
            
            // 启动录制监控线程
            startRecordingMonitor();
            
            if (recordingCallback != null) {
                recordingCallback.onRecordingStarted(new File(config.getOutputPath()));
            }
            
            System.out.println("开始录制: " + config.getOutputPath());
            return true;
        } catch (Exception e) {
            System.err.println("开始录制失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void stopRecording() {
        if (!isRecording) {
            return;
        }
        
        try {
            isRecording = false;
            
            // 停止录像
            if (currentCamera != null && currentCamera.isRecording()) {
                currentCamera.stopRecording();
            }
            
            // 停止录音
            if (currentAudioDevice != null && currentAudioDevice.isRecording()) {
                currentAudioDevice.stopRecording();
            }
            
            long duration = System.currentTimeMillis() - recordingStartTime;
            
            if (recordingCallback != null) {
                recordingCallback.onRecordingStopped(
                    new File(currentConfig.getOutputPath()), 
                    duration
                );
            }
            
            System.out.println("停止录制，时长: " + (duration / 1000) + " 秒");
        } catch (Exception e) {
            System.err.println("停止录制失败: " + e.getMessage());
        }
    }
    
    @Override
    public void pauseRecording() {
        if (!isRecording || isPaused) {
            return;
        }
        
        isPaused = true;
        
        // 暂停录像（通过停止帧捕获实现）
        // 实际实现需要更复杂的逻辑来处理暂停
        System.out.println("录制已暂停");
        
        if (recordingCallback != null) {
            recordingCallback.onRecordingPaused();
        }
    }
    
    @Override
    public void resumeRecording() {
        if (!isRecording || !isPaused) {
            return;
        }
        
        isPaused = false;
        System.out.println("录制已恢复");
        
        if (recordingCallback != null) {
            recordingCallback.onRecordingResumed();
        }
    }
    
    @Override
    public boolean isRecording() {
        return isRecording;
    }
    
    @Override
    public boolean isPaused() {
        return isPaused;
    }
    
    @Override
    public long getRecordingDuration() {
        if (!isRecording) {
            return 0;
        }
        return System.currentTimeMillis() - recordingStartTime;
    }
    
    @Override
    public long getRecordingFileSize() {
        if (currentConfig == null || currentConfig.getOutputPath() == null) {
            return 0;
        }
        
        File file = new File(currentConfig.getOutputPath());
        return file.exists() ? file.length() : 0;
    }
    
    @Override
    public boolean capturePhoto(File outputFile) {
        if (currentCamera == null) {
            System.err.println("未设置摄像头");
            return false;
        }
        
        try {
            VideoFrame frame = currentCamera.capturePhoto();
            if (frame != null) {
                boolean success = JavaCVCamera.saveFrameToImage(frame, outputFile.getAbsolutePath());
                if (success) {
                    System.out.println("拍照成功: " + outputFile.getAbsolutePath());
                }
                return success;
            }
        } catch (Exception e) {
            System.err.println("拍照失败: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean startAudioRecording(File outputFile) {
        RecordingConfig config = new RecordingConfig(outputFile.getAbsolutePath());
        config.setCaptureAudioOnly(true);
        return startRecording(config);
    }
    
    @Override
    public boolean startVideoRecording(File outputFile) {
        RecordingConfig config = new RecordingConfig(outputFile.getAbsolutePath());
        config.setCaptureVideoOnly(true);
        return startRecording(config);
    }
    
    @Override
    public void setRecordingCallback(RecordingCallback callback) {
        this.recordingCallback = callback;
    }
    
    /**
     * 启动录制监控线程
     */
    private void startRecordingMonitor() {
        recordingThread = new Thread(() -> {
            try {
                while (isRecording) {
                    Thread.sleep(1000); // 每秒更新一次
                    
                    if (recordingCallback != null && !isPaused) {
                        recordingCallback.onRecordingProgress(
                            getRecordingDuration(),
                            getRecordingFileSize()
                        );
                    }
                    
                    // 检查最大时长限制
                    if (currentConfig != null && currentConfig.getMaxDuration() > 0) {
                        if (getRecordingDuration() >= currentConfig.getMaxDuration()) {
                            System.out.println("达到最大录制时长，自动停止");
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
    
    /**
     * 获取当前摄像头
     */
    public ICameraDevice getCurrentCamera() {
        return currentCamera;
    }
    
    /**
     * 获取当前音频设备
     */
    public IAudioDevice getCurrentAudioDevice() {
        return currentAudioDevice;
    }
    
    /**
     * 打印所有设备信息
     */
    public void printAllDevices() {
        System.out.println("\n===== 摄像头设备 =====");
        List<ICameraDevice> cameras = cameraDiscovery.discoverDevices();
        for (ICameraDevice camera : cameras) {
            System.out.println("  - " + camera.getDeviceName() + " (" + camera.getDeviceId() + ")");
        }
        
        System.out.println("\n===== 音频设备 =====");
        List<IAudioDevice> audios = audioDiscovery.discoverDevices();
        for (IAudioDevice audio : audios) {
            System.out.println("  - " + audio.getDeviceName() + " (" + audio.getDeviceId() + ")");
        }
    }
}
