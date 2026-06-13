package com.recording.impl;

import com.recording.api.IAudioDevice;
import com.recording.model.AudioFormat;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Java Sound API 的音频设备实现
 */
public class JavaSoundAudio implements IAudioDevice {
    
    private String deviceId;
    private String deviceName;
    private String description;
    private TargetDataLine microphone;
    private AudioFormat currentFormat;
    private boolean isOpen = false;
    private boolean isCapturing = false;
    private boolean isRecording = false;
    private CaptureCallback captureCallback;
    private Thread captureThread;
    private ByteArrayOutputStream audioBuffer;
    private String outputFilePath;
    
    public JavaSoundAudio(String mixerName) {
        this.deviceId = "audio_" + mixerName.hashCode();
        this.deviceName = mixerName;
        this.description = "Audio Device: " + mixerName;
    }
    
    public JavaSoundAudio(String deviceId, String deviceName, String description) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.description = description;
    }
    
    @Override
    public String getDeviceId() {
        return deviceId;
    }
    
    @Override
    public String getDeviceName() {
        return deviceName;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public List<AudioFormat> getSupportedFormats() {
        List<AudioFormat> formats = new ArrayList<>();
        // 添加常用音频格式
        formats.add(AudioFormat.Presets.CD_QUALITY());
        formats.add(AudioFormat.Presets.DVD_QUALITY());
        formats.add(AudioFormat.Presets.AAC_HIGH());
        return formats;
    }
    
    @Override
    public boolean open(AudioFormat format) {
        try {
            // 查找匹配的 Mixer
            Mixer.Info mixerInfo = findMixerByName(deviceName);
            if (mixerInfo == null) {
                System.err.println("未找到音频设备: " + deviceName);
                return false;
            }
            
            // 转换 AudioFormat 为 Java Sound 格式
            javax.sound.sampled.AudioFormat audioFormat = 
                new javax.sound.sampled.AudioFormat(
                    format.getSampleRate(),
                    format.getBitDepth(),
                    format.getChannels(),
                    true,  // signed
                    false   // little-endian
                );
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            microphone = (TargetDataLine) mixer.getLine(info);
            microphone.open(audioFormat);
            
            currentFormat = format;
            isOpen = true;
            System.out.println("音频设备已打开: " + deviceName);
            return true;
        } catch (Exception e) {
            System.err.println("打开音频设备失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void close() {
        try {
            if (isRecording) {
                stopRecording();
            }
            if (isCapturing) {
                stopCapture();
            }
            if (microphone != null) {
                microphone.close();
            }
            isOpen = false;
            System.out.println("音频设备已关闭: " + deviceName);
        } catch (Exception e) {
            System.err.println("关闭音频设备失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isOpen() {
        return isOpen;
    }
    
    @Override
    public boolean startCapture(CaptureCallback callback) {
        if (!isOpen) {
            System.err.println("音频设备未打开");
            return false;
        }
        
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
    
    @Override
    public void stopCapture() {
        isCapturing = false;
        if (microphone != null) {
            microphone.stop();
        }
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
    }
    
    @Override
    public boolean isCapturing() {
        return isCapturing;
    }
    
    @Override
    public boolean startRecording(String outputPath) {
        if (!isOpen) {
            System.err.println("音频设备未打开");
            return false;
        }
        
        try {
            this.outputFilePath = outputPath;
            this.audioBuffer = new ByteArrayOutputStream();
            isRecording = true;
            
            // 开始捕获并保存到 buffer
            startCapture(new CaptureCallback() {
                @Override
                public void onAudioData(byte[] audioData, int length) {
                    if (isRecording && audioBuffer != null) {
                        audioBuffer.write(audioData, 0, length);
                    }
                }
                
                @Override
                public void onError(int errorCode, String errorMessage) {
                    System.err.println("录音错误: " + errorMessage);
                }
            });
            
            System.out.println("开始录音: " + outputPath);
            return true;
        } catch (Exception e) {
            System.err.println("开始录音失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void stopRecording() {
        if (!isRecording) {
            return;
        }
        
        try {
            stopCapture();
            isRecording = false;
            
            // 保存音频数据到文件
            if (audioBuffer != null && outputFilePath != null) {
                saveAudioToFile(audioBuffer.toByteArray(), outputFilePath);
                audioBuffer = null;
            }
            
            System.out.println("停止录音: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("停止录音失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isRecording() {
        return isRecording;
    }
    
    @Override
    public void setAudioParameters(int volume, int boost, int noiseReduction) {
        // Java Sound API 不直接支持这些参数
        // 需要通过 GainControl 或其他方式实现
        System.out.println("音频参数调整: volume=" + volume + 
            ", boost=" + boost + ", noiseReduction=" + noiseReduction);
    }
    
    @Override
    public int getAudioLevel() {
        if (!isOpen || microphone == null) {
            return 0;
        }
        
        // 获取当前音频电平（需要读取实时数据计算）
        byte[] buffer = new byte[1024];
        int count = microphone.available();
        if (count > 0) {
            int bytesRead = microphone.read(buffer, 0, Math.min(count, buffer.length));
            // 简单计算音量级别
            long sum = 0;
            for (int i = 0; i < bytesRead; i += 2) {
                short sample = (short) ((buffer[i + 1] << 8) | buffer[i]);
                sum += Math.abs(sample);
            }
            int avgLevel = (int) (sum / (bytesRead / 2));
            return Math.min(100, avgLevel / 100); // 归一化到 0-100
        }
        return 0;
    }
    
    /**
     * 根据名称查找 Mixer
     */
    private Mixer.Info findMixerByName(String name) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixers) {
            if (info.getName().equals(name) || info.getDescription().contains(name)) {
                return info;
            }
        }
        return null;
    }
    
    /**
     * 保存音频数据到文件
     */
    private void saveAudioToFile(byte[] audioData, String filePath) {
        try {
            File outputFile = new File(filePath);
            FileOutputStream fos = new FileOutputStream(outputFile);
            
            // 写入 WAV 文件头
            writeWavHeader(fos, audioData.length, currentFormat);
            
            // 写入音频数据
            fos.write(audioData);
            fos.close();
            
            System.out.println("音频已保存: " + filePath + " (" + audioData.length + " bytes)");
        } catch (Exception e) {
            System.err.println("保存音频文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 写入 WAV 文件头
     */
    private void writeWavHeader(FileOutputStream fos, int dataSize, AudioFormat format) throws Exception {
        int sampleRate = (int) format.getSampleRate();
        int bitDepth = format.getBitDepth();
        int channels = format.getChannels();
        int byteRate = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;
        
        // RIFF header
        fos.write("RIFF".getBytes());
        fos.write(intToBytes(36 + dataSize)); // ChunkSize
        fos.write("WAVE".getBytes());
        
        // fmt subchunk
        fos.write("fmt ".getBytes());
        fos.write(intToBytes(16)); // Subchunk1Size (PCM)
        fos.write(shortToBytes((short) 1)); // AudioFormat (PCM)
        fos.write(shortToBytes((short) channels));
        fos.write(intToBytes(sampleRate));
        fos.write(intToBytes(byteRate));
        fos.write(shortToBytes((short) blockAlign));
        fos.write(shortToBytes((short) bitDepth));
        
        // data subchunk
        fos.write("data".getBytes());
        fos.write(intToBytes(dataSize));
    }
    
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }
    
    private byte[] shortToBytes(short value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }
}
