package com.recording.api;

import com.recording.model.AudioFormat;
import java.util.List;

/**
 * 音频设备接口
 * 定义麦克风等音频输入设备的通用操作
 */
public interface IAudioDevice {
    
    /**
     * 获取设备 ID
     * @return 设备唯一标识符
     */
    String getDeviceId();
    
    /**
     * 获取设备名称
     * @return 设备名称
     */
    String getDeviceName();
    
    /**
     * 获取设备描述
     * @return 设备描述信息
     */
    String getDescription();
    
    /**
     * 获取设备支持的音频格式列表
     * @return 支持的音频格式列表
     */
    List<AudioFormat> getSupportedFormats();
    
    /**
     * 打开音频设备
     * @param format 音频格式
     * @return 是否成功打开
     */
    boolean open(AudioFormat format);
    
    /**
     * 关闭音频设备
     */
    void close();
    
    /**
     * 检查设备是否已打开
     * @return 设备是否已打开
     */
    boolean isOpen();
    
    /**
     * 开始音频捕获
     * @param captureCallback 捕获回调接口
     * @return 是否成功开始捕获
     */
    boolean startCapture(CaptureCallback captureCallback);
    
    /**
     * 停止音频捕获
     */
    void stopCapture();
    
    /**
     * 检查是否正在捕获
     * @return 是否正在捕获
     */
    boolean isCapturing();
    
    /**
     * 开始录音
     * @param outputPath 输出文件路径
     * @return 是否成功开始录音
     */
    boolean startRecording(String outputPath);
    
    /**
     * 停止录音
     */
    void stopRecording();
    
    /**
     * 检查是否正在录音
     * @return 是否正在录音
     */
    boolean isRecording();
    
    /**
     * 设置音频参数
     * @param volume 音量 (0-100)
     * @param boost 增益 (0-100)
     * @param noiseReduction 降噪级别 (0-100)
     */
    void setAudioParameters(int volume, int boost, int noiseReduction);
    
    /**
     * 获取当前音频电平
     * @return 音频电平 (0-100)
     */
    int getAudioLevel();
    
    /**
     * 音频捕获回调接口
     */
    interface CaptureCallback {
        /**
         * 音频数据回调
         * @param audioData 音频数据（PCM）
         * @param length 数据长度
         */
        void onAudioData(byte[] audioData, int length);
        
        /**
         * 捕获错误回调
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(int errorCode, String errorMessage);
    }
}
