package com.recording.api;

import java.util.List;

/**
 * 设备发现接口
 * 用于发现和枚举系统中的音视频设备
 * 
 * @param <T> 设备类型，可以是 ICameraDevice 或 IAudioDevice
 */
public interface IDeviceDiscovery<T> {
    
    /**
     * 扫描并列出所有可用设备
     * @return 设备列表
     */
    List<T> discoverDevices();
    
    /**
     * 根据设备 ID 获取设备
     * @param deviceId 设备 ID
     * @return 设备对象，如果未找到返回 null
     */
    T getDeviceById(String deviceId);
    
    /**
     * 根据设备名称获取设备
     * @param deviceName 设备名称
     * @return 设备对象，如果未找到返回 null
     */
    T getDeviceByName(String deviceName);
    
    /**
     * 获取默认设备
     * @return 默认设备，如果没有返回 null
     */
    T getDefaultDevice();
    
    /**
     * 检查设备是否可用
     * @param deviceId 设备 ID
     * @return 设备是否可用
     */
    boolean isDeviceAvailable(String deviceId);
    
    /**
     * 刷新设备列表
     * @return 刷新后的设备列表
     */
    List<T> refreshDevices();
    
    /**
     * 设备状态变化回调接口
     */
    interface DeviceStatusCallback {
        /**
         * 设备连接
         * @param device 连接的设备
         */
        void onDeviceConnected(T device);
        
        /**
         * 设备断开
         * @param deviceId 断开的设备 ID
         */
        void onDeviceDisconnected(String deviceId);
        
        /**
         * 设备状态变化
         * @param device 状态变化的设备
         * @param status 新状态
         */
        void onDeviceStatusChanged(T device, DeviceStatus status);
    }
    
    /**
     * 设备状态枚举
     */
    enum DeviceStatus {
        CONNECTED,    // 已连接
        DISCONNECTED, // 已断开
        BUSY,         // 忙碌（被占用）
        ERROR         // 错误
    }
    
    /**
     * 设置设备状态回调
     * @param callback 回调函数
     */
    void setDeviceStatusCallback(DeviceStatusCallback callback);
}
