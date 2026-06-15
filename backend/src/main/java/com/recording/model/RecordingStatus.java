package com.recording.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 录制状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStatus {
    /** 是否录制中 */
    private boolean recording;
    /** 是否暂停 */
    private boolean paused;
    /** 录制时长(毫秒) */
    private long duration;
    /** 当前文件大小(字节) */
    private long fileSize;
    /** 输出文件路径 */
    private String outputPath;
}
