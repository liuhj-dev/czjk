package com.recording.api;

import com.recording.model.RecordingConfig;
import java.io.File;

/**
 * 媒体捕获管理接口
 * 统一管理音频和视频的捕获、录制和编码
 */
public interface IMediaCapture {
    
    /**
     * 初始化捕获系统
     * @return 是否初始化成功
     */
    boolean initialize();
    
    /**
     * 销毁捕获系统，释放资源
     */
    void destroy();
    
    /**
     * 获取摄像头设备管理接口
     * @return 摄像头设备管理接口
     */
    IDeviceDiscovery<ICameraDevice> getCameraDiscovery();
    
    /**
     * 获取音频设备管理接口
     * @return 音频设备管理接口
     */
    IDeviceDiscovery<IAudioDevice> getAudioDiscovery();
    
    /**
     * 设置视频源
     * @param camera 摄像头设备
     * @return 是否设置成功
     */
    boolean setVideoSource(ICameraDevice camera);
    
    /**
     * 设置音频源
     * @param audioDevice 音频设备
     * @return 是否设置成功
     */
    boolean setAudioSource(IAudioDevice audioDevice);
    
    /**
     * 开始同时录音录像
     * @param config 录制配置
     * @return 是否成功开始
     */
    boolean startRecording(RecordingConfig config);
    
    /**
     * 停止录音录像
     */
    void stopRecording();
    
    /**
     * 暂停录音录像
     */
    void pauseRecording();
    
    /**
     * 恢复录音录像
     */
    void resumeRecording();
    
    /**
     * 检查是否正在录制
     * @return 是否正在录制
     */
    boolean isRecording();
    
    /**
     * 检查是否暂停
     * @return 是否暂停
     */
    boolean isPaused();
    
    /**
     * 获取录制时长（毫秒）
     * @return 录制时长
     */
    long getRecordingDuration();
    
    /**
     * 获取录制文件大小（字节）
     * @return 文件大小
     */
    long getRecordingFileSize();
    
    /**
     * 拍照（仅视频）
     * @param outputFile 输出文件
     * @return 是否成功
     */
    boolean capturePhoto(File outputFile);
    
    /**
     * 仅录音（不带视频）
     * @param outputFile 输出文件
     * @return 是否成功开始
     */
    boolean startAudioRecording(File outputFile);
    
    /**
     * 仅录像（不带音频）
     * @param outputFile 输出文件
     * @return 是否成功开始
     */
    boolean startVideoRecording(File outputFile);
    
    /**
     * 录制状态回调接口
     */
    interface RecordingCallback {
        /**
         * 录制开始
         * @param outputFile 输出文件
         */
        void onRecordingStarted(File outputFile);
        
        /**
         * 录制停止
         * @param outputFile 输出文件
         * @param duration 录制时长（毫秒）
         */
        void onRecordingStopped(File outputFile, long duration);
        
        /**
         * 录制暂停
         */
        void onRecordingPaused();
        
        /**
         * 录制恢复
         */
        void onRecordingResumed();
        
        /**
         * 录制错误
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onRecordingError(int errorCode, String errorMessage);
        
        /**
         * 录制进度更新
         * @param duration 当前时长（毫秒）
         * @param fileSize 当前文件大小（字节）
         */
        void onRecordingProgress(long duration, long fileSize);
    }
    
    /**
     * 设置录制回调
     * @param callback 回调接口
     */
    void setRecordingCallback(RecordingCallback callback);
}
