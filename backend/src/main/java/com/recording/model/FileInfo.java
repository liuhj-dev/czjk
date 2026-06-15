package com.recording.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    /** 文件ID */
    private String id;
    /** 文件名 */
    private String name;
    /** 文件路径 */
    private String path;
    /** 文件大小(字节) */
    private long size;
    /** 文件类型: video, audio, image */
    private String type;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 文件时长(毫秒, 可选) */
    private Long duration;
}
