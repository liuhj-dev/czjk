package com.recording.model;

/**
 * 视频帧模型
 * 包含一帧视频数据
 */
public class VideoFrame {
    private byte[] data;           // 帧数据（RGB 或 YUV 格式）
    private int width;              // 帧宽度
    private int height;             // 帧高度
    private long timestamp;         // 时间戳（毫秒）
    private int frameNumber;        // 帧序号
    private String format;          // 数据格式（"RGB", "BGR", "YUV"）
    
    public VideoFrame() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public VideoFrame(byte[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.timestamp = System.currentTimeMillis();
        this.format = "BGR"; // OpenCV 默认格式
    }
    
    public VideoFrame(byte[] data, int width, int height, long timestamp, int frameNumber) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.timestamp = timestamp;
        this.frameNumber = frameNumber;
        this.format = "BGR";
    }
    
    // Getters and Setters
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getFrameNumber() {
        return frameNumber;
    }
    
    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    /**
     * 获取帧大小（字节数）
     */
    public int getDataSize() {
        return data != null ? data.length : 0;
    }
    
    /**
     * 获取帧率（基于时间戳计算）
     */
    public double calculateFPS(VideoFrame previousFrame) {
        if (previousFrame == null || timestamp == previousFrame.timestamp) {
            return 0.0;
        }
        return 1000.0 / (timestamp - previousFrame.timestamp);
    }
}
