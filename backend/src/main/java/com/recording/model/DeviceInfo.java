package com.recording.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 设备信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    /** 设备ID */
    private String id;
    /** 设备名称 */
    private String name;
    /** 设备描述 */
    private String description;
    /** 设备类型: camera, audio */
    private String type;
    /** 是否已打开 */
    private boolean isOpen;
    /** 设备索引 */
    private int index;
}
