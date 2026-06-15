package com.recording.service;

import com.recording.model.DeviceInfo;
import com.recording.model.FileInfo;
import com.recording.model.RecordingConfigRequest;
import com.recording.model.RecordingStatus;

import java.util.List;

/**
 * 录音录像核心服务接口
 */
public interface RecordingService {

    // ============ 设备管理 ============

    /** 获取所有摄像头设备 */
    List<DeviceInfo> getCameraDevices();

    /** 获取所有音频设备 */
    List<DeviceInfo> getAudioDevices();

    /** 打开设备 */
    boolean openDevice(String deviceId, String type);

    /** 关闭设备 */
    boolean closeDevice(String deviceId, String type);

    // ============ 录像控制 ============

    /** 开始录制 */
    void startRecording(RecordingConfigRequest config);

    /** 停止录制 */
    void stopRecording();

    /** 暂停录制 */
    void pauseRecording();

    /** 恢复录制 */
    void resumeRecording();

    /** 拍照 */
    String capturePhoto(String deviceId);

    /** 获取录制状态 */
    RecordingStatus getStatus();

    // ============ 文件管理 ============

    /** 获取文件列表 */
    List<FileInfo> getFileList();

    /** 获取文件下载路径 */
    String getFilePath(String fileId);

    /** 删除文件 */
    boolean deleteFile(String fileId);
}
