package com.recording.impl;

import com.recording.api.ICameraDevice;
import com.recording.api.IDeviceDiscovery;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头设备发现实现
 */
public class CameraDiscovery implements IDeviceDiscovery<ICameraDevice> {
    
    private DeviceStatusCallback statusCallback;
    
    @Override
    public List<ICameraDevice> discoverDevices() {
        List<ICameraDevice> devices = new ArrayList<>();
        
        try {
            // 尝试查找可用的摄像头（通常索引 0-10）
            for (int i = 0; i < 10; i++) {
                try {
                    OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(i);
                    grabber.start();
                    
                    // 成功打开，添加到列表
                    String deviceId = "camera_" + i;
                    String deviceName = "Camera " + (i + 1);
                    String description = "USB Camera (Index: " + i + ")";
                    
                    ICameraDevice camera = new JavaCVCamera(deviceId, deviceName, description);
                    devices.add(camera);
                    
                    grabber.stop();
                    grabber.close();
                } catch (Exception e) {
                    // 该索引不可用，继续尝试下一个
                    break; // 通常摄像头是连续的，如果遇到错误可以停止
                }
            }
            
            System.out.println("发现 " + devices.size() + " 个摄像头设备");
        } catch (Exception e) {
            System.err.println("扫描摄像头设备失败: " + e.getMessage());
        }
        
        return devices;
    }
    
    @Override
    public ICameraDevice getDeviceById(String deviceId) {
        List<ICameraDevice> devices = discoverDevices();
        for (ICameraDevice device : devices) {
            if (device.getDeviceId().equals(deviceId)) {
                return device;
            }
        }
        return null;
    }
    
    @Override
    public ICameraDevice getDeviceByName(String deviceName) {
        List<ICameraDevice> devices = discoverDevices();
        for (ICameraDevice device : devices) {
            if (device.getDeviceName().equals(deviceName)) {
                return device;
            }
        }
        return null;
    }
    
    @Override
    public ICameraDevice getDefaultDevice() {
        List<ICameraDevice> devices = discoverDevices();
        return devices.isEmpty() ? null : devices.get(0);
    }
    
    @Override
    public boolean isDeviceAvailable(String deviceId) {
        ICameraDevice device = getDeviceById(deviceId);
        return device != null;
    }
    
    @Override
    public List<ICameraDevice> refreshDevices() {
        // 重新扫描设备
        return discoverDevices();
    }
    
    @Override
    public void setDeviceStatusCallback(DeviceStatusCallback callback) {
        this.statusCallback = callback;
    }
    
    /**
     * 测试方法：列出所有可用摄像头
     */
    public static void main(String[] args) {
        CameraDiscovery discovery = new CameraDiscovery();
        List<ICameraDevice> cameras = discovery.discoverDevices();
        
        System.out.println("可用摄像头:");
        for (ICameraDevice camera : cameras) {
            System.out.println("  ID: " + camera.getDeviceId());
            System.out.println("  Name: " + camera.getDeviceName());
            System.out.println("  Description: " + camera.getDescription());
            System.out.println("  Supported Formats: " + camera.getSupportedFormats());
            System.out.println();
        }
    }
}
