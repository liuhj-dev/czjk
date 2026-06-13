package com.recording.api;

import com.recording.model.VideoFrame;
import com.recording.model.VideoFormat;
import java.util.List;

/**
 * 摄像头设备接口
 * 定义摄像头设备的通用操作
 */
public interface ICameraDevice {
    
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
     * 获取设备支持的视频格式列表
     * @return 支持的视频格式列表
     */
    List<VideoFormat> getSupportedFormats();
    
    /**
     * 打开摄像头设备
     * @param format 视频格式
     * @return 是否成功打开
     */
    boolean open(VideoFormat format);
    
    /**
     * 关闭摄像头设备
     */
    void close();
    
    /**
     * 检查设备是否已打开
     * @return 设备是否已打开
     */
    boolean isOpen();
    
    /**
     * 开始视频预览
     * @param previewCallback 预览回调接口
     * @return 是否成功开始预览
     */
    boolean startPreview(PreviewCallback previewCallback);
    
    /**
     * 停止视频预览
     */
    void stopPreview();
    
    /**
     * 捕获单张图片
     * @return 视频帧对象
     */
    VideoFrame capturePhoto();
    
    /**
     * 开始录像
     * @param outputPath 输出文件路径
     * @return 是否成功开始录像
     */
    boolean startRecording(String outputPath);
    
    /**
     * 停止录像
     */
    void stopRecording();
    
    /**
     * 检查是否正在录像
     * @return 是否正在录像
     */
    boolean isRecording();
    
    /**
     * 设置视频参数
     * @param brightness 亮度 (0-100)
     * @param contrast 对比度 (0-100)
     * @param saturation 饱和度 (0-100)
     */
    void setVideoParameters(int brightness, int contrast, int saturation);
    
    /**
     * 获取当前帧率
     * @return 帧率（FPS）
     */
    double getCurrentFPS();
    
    /**
     * 预览回调接口
     */
    interface PreviewCallback {
        /**
         * 预览帧回调
         * @param frame 视频帧
         */
        void onPreviewFrame(VideoFrame frame);
        
        /**
         * 预览错误回调
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(int errorCode, String errorMessage);
    }
}
