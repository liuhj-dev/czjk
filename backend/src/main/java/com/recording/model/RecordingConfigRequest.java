package com.recording.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 录制配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingConfigRequest {
    /** 输出文件路径 */
    private String outputPath;
    /** 视频宽度 */
    private Integer videoWidth;
    /** 视频高度 */
    private Integer videoHeight;
    /** 帧率 */
    private Double frameRate;
    /** 视频比特率 */
    private Long videoBitrate;
    /** 音频采样率 */
    private Integer audioSampleRate;
    /** 音频声道数 */
    private Integer audioChannels;
    /** 音频比特率 */
    private Long audioBitrate;
    /** 录制模式: both, video, audio */
    private String mode;
}
