package com.recording.impl;

import com.recording.api.IAudioDevice;
import com.recording.api.IDeviceDiscovery;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频设备发现实现
 */
public class AudioDiscovery implements IDeviceDiscovery<IAudioDevice> {
    
    private DeviceStatusCallback statusCallback;
    
    @Override
    public List<IAudioDevice> discoverDevices() {
        List<IAudioDevice> devices = new ArrayList<>();
        
        try {
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            
            for (Mixer.Info info : mixerInfos) {
                String name = info.getName();
                String description = info.getDescription();
                
                // 过滤出输入设备（麦克风）
                if (isInputDevice(info)) {
                    String deviceId = "audio_" + name.hashCode();
                    IAudioDevice audioDevice = new JavaSoundAudio(deviceId, name, description);
                    devices.add(audioDevice);
                }
            }
            
            System.out.println("发现 " + devices.size() + " 个音频输入设备");
        } catch (Exception e) {
            System.err.println("扫描音频设备失败: " + e.getMessage());
        }
        
        return devices;
    }
    
    @Override
    public IAudioDevice getDeviceById(String deviceId) {
        List<IAudioDevice> devices = discoverDevices();
        for (IAudioDevice device : devices) {
            if (device.getDeviceId().equals(deviceId)) {
                return device;
            }
        }
        return null;
    }
    
    @Override
    public IAudioDevice getDeviceByName(String deviceName) {
        List<IAudioDevice> devices = discoverDevices();
        for (IAudioDevice device : devices) {
            if (device.getDeviceName().equals(deviceName)) {
                return device;
            }
        }
        return null;
    }
    
    @Override
    public IAudioDevice getDefaultDevice() {
        List<IAudioDevice> devices = discoverDevices();
        return devices.isEmpty() ? null : devices.get(0);
    }
    
    @Override
    public boolean isDeviceAvailable(String deviceId) {
        IAudioDevice device = getDeviceById(deviceId);
        return device != null;
    }
    
    @Override
    public List<IAudioDevice> refreshDevices() {
        return discoverDevices();
    }
    
    @Override
    public void setDeviceStatusCallback(DeviceStatusCallback callback) {
        this.statusCallback = callback;
    }
    
    /**
     * 判断是否为输入设备
     */
    private boolean isInputDevice(Mixer.Info info) {
        try {
            Mixer mixer = AudioSystem.getMixer(info);
            mixer.open();
            
            // 检查是否支持 TargetDataLine（输入）
            javax.sound.sampled.DataLine.Info dataLineInfo = 
                new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, 
                    new javax.sound.sampled.AudioFormat(44100, 16, 2, true, false));
            
            boolean supportsInput = mixer.isLineSupported(dataLineInfo);
            mixer.close();
            
            return supportsInput;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 测试方法：列出所有可用音频输入设备
     */
    public static void main(String[] args) {
        AudioDiscovery discovery = new AudioDiscovery();
        List<IAudioDevice> audios = discovery.discoverDevices();
        
        System.out.println("可用音频输入设备:");
        for (IAudioDevice audio : audios) {
            System.out.println("  ID: " + audio.getDeviceId());
            System.out.println("  Name: " + audio.getDeviceName());
            System.out.println("  Description: " + audio.getDescription());
            System.out.println("  Supported Formats: " + audio.getSupportedFormats());
            System.out.println();
        }
    }
}
