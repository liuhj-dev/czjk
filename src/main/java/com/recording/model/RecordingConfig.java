package com.recording.model;

/**
 * 录制配置模型
 * 定义录音录像的参数
 */
public class RecordingConfig {
    private String outputPath;         // 输出文件路径
    private VideoFormat videoFormat;    // 视频格式
    private AudioFormat audioFormat;    // 音频格式
    private boolean recordVideo = true; // 是否录制视频
    private boolean recordAudio = true; // 是否录制音频
    private boolean captureAudioOnly = false; // 仅录音模式
    private boolean captureVideoOnly = false; // 仅录像模式
    private long maxDuration = 0;      // 最大录制时长（毫秒，0=无限制）
    private long maxFileSize = 0;       // 最大文件大小（字节，0=无限制）
    private boolean autoSplit = false;   // 是否自动分割文件
    private String fileNamePattern = "yyyyMMdd_HHmmss"; // 文件名模式
    
    public RecordingConfig() {
    }
    
    public RecordingConfig(String outputPath) {
        this.outputPath = outputPath;
        this.videoFormat = VideoFormat.Presets.HD720();
        this.audioFormat = AudioFormat.Presets.AAC_HIGH();
    }
    
    public RecordingConfig(String outputPath, VideoFormat videoFormat, AudioFormat audioFormat) {
        this.outputPath = outputPath;
        this.videoFormat = videoFormat;
        this.audioFormat = audioFormat;
    }
    
    // Getters and Setters
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public VideoFormat getVideoFormat() {
        return videoFormat;
    }
    
    public void setVideoFormat(VideoFormat videoFormat) {
        this.videoFormat = videoFormat;
    }
    
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
    
    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }
    
    public boolean isRecordVideo() {
        return recordVideo;
    }
    
    public void setRecordVideo(boolean recordVideo) {
        this.recordVideo = recordVideo;
    }
    
    public boolean isRecordAudio() {
        return recordAudio;
    }
    
    public void setRecordAudio(boolean recordAudio) {
        this.recordAudio = recordAudio;
    }
    
    public boolean isCaptureAudioOnly() {
        return captureAudioOnly;
    }
    
    public void setCaptureAudioOnly(boolean captureAudioOnly) {
        this.captureAudioOnly = captureAudioOnly;
        if (captureAudioOnly) {
            this.recordVideo = false;
        }
    }
    
    public boolean isCaptureVideoOnly() {
        return captureVideoOnly;
    }
    
    public void setCaptureVideoOnly(boolean captureVideoOnly) {
        this.captureVideoOnly = captureVideoOnly;
        if (captureVideoOnly) {
            this.recordAudio = false;
        }
    }
    
    public long getMaxDuration() {
        return maxDuration;
    }
    
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public boolean isAutoSplit() {
        return autoSplit;
    }
    
    public void setAutoSplit(boolean autoSplit) {
        this.autoSplit = autoSplit;
    }
    
    public String getFileNamePattern() {
        return fileNamePattern;
    }
    
    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }
    
    @Override
    public String toString() {
        return String.format("RecordingConfig{output='%s', video=%s, audio=%s}", 
            outputPath, 
            recordVideo ? videoFormat : "disabled",
            recordAudio ? audioFormat : "disabled");
    }
}
