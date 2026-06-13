package com.recording.model;

/**
 * 视频格式模型
 * 定义视频捕获的参数
 */
public class VideoFormat {
    private int width;          // 宽度（像素）
    private int height;         // 高度（像素）
    private double frameRate;  // 帧率（FPS）
    private String codec;       // 编码格式（如 "H.264", "MJPEG"）
    private int bitrate;        // 比特率（bps）
    
    public VideoFormat() {
    }
    
    public VideoFormat(int width, int height, double frameRate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.codec = "H.264";
        this.bitrate = width * height * 2; // 默认比特率
    }
    
    public VideoFormat(int width, int height, double frameRate, String codec, int bitrate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.codec = codec;
        this.bitrate = bitrate;
    }
    
    // Getters and Setters
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
    
    public double getFrameRate() {
        return frameRate;
    }
    
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }
    
    public String getCodec() {
        return codec;
    }
    
    public void setCodec(String codec) {
        this.codec = codec;
    }
    
    public int getBitrate() {
        return bitrate;
    }
    
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
    
    @Override
    public String toString() {
        return String.format("%dx%d@%.1fFPS [%s, %d kbps]", 
            width, height, frameRate, codec, bitrate / 1000);
    }
    
    /**
     * 获取常用分辨率格式
     */
    public static class Presets {
        public static VideoFormat VGA() {
            return new VideoFormat(640, 480, 30.0, "H.264", 500000);
        }
        
        public static VideoFormat HD720() {
            return new VideoFormat(1280, 720, 30.0, "H.264", 1500000);
        }
        
        public static VideoFormat FULL_HD() {
            return new VideoFormat(1920, 1080, 30.0, "H.264", 3000000);
        }
        
        public static VideoFormat UHD_4K() {
            return new VideoFormat(3840, 2160, 30.0, "H.264", 10000000);
        }
    }
}
