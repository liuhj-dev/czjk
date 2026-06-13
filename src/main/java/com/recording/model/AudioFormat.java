package com.recording.model;

/**
 * 音频格式模型
 * 定义音频捕获的参数
 */
public class AudioFormat {
    private int sampleRate;    // 采样率（Hz）
    private int bitDepth;      // 位深度（bit）
    private int channels;      // 声道数（1=单声道，2=立体声）
    private String codec;      // 编码格式（如 "PCM", "AAC", "MP3"）
    private int bitrate;      // 比特率（bps）
    
    public AudioFormat() {
    }
    
    public AudioFormat(int sampleRate, int bitDepth, int channels) {
        this.sampleRate = sampleRate;
        this.bitDepth = bitDepth;
        this.channels = channels;
        this.codec = "PCM";
        this.bitrate = sampleRate * bitDepth * channels;
    }
    
    public AudioFormat(int sampleRate, int bitDepth, int channels, String codec, int bitrate) {
        this.sampleRate = sampleRate;
        this.bitDepth = bitDepth;
        this.channels = channels;
        this.codec = codec;
        this.bitrate = bitrate;
    }
    
    // Getters and Setters
    public int getSampleRate() {
        return sampleRate;
    }
    
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    
    public int getBitDepth() {
        return bitDepth;
    }
    
    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }
    
    public int getChannels() {
        return channels;
    }
    
    public void setChannels(int channels) {
        this.channels = channels;
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
        return String.format("%dHz %dbit %s [%s, %d kbps]", 
            sampleRate, bitDepth, 
            channels == 1 ? "Mono" : "Stereo",
            codec, bitrate / 1000);
    }
    
    /**
     * 获取常用音频格式预设
     */
    public static class Presets {
        public static AudioFormat CD_QUALITY() {
            return new AudioFormat(44100, 16, 2, "PCM", 1411200);
        }
        
        public static AudioFormat DVD_QUALITY() {
            return new AudioFormat(48000, 16, 2, "PCM", 1536000);
        }
        
        public static AudioFormat AAC_HIGH() {
            return new AudioFormat(44100, 16, 2, "AAC", 192000);
        }
        
        public static AudioFormat MP3_HIGH() {
            return new AudioFormat(44100, 16, 2, "MP3", 320000);
        }
    }
}
