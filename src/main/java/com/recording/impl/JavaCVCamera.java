package com.recording.impl;

import com.recording.api.ICameraDevice;
import com.recording.model.VideoFormat;
import com.recording.model.VideoFrame;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 基于 JavaCV (OpenCV) 的摄像头实现
 */
public class JavaCVCamera implements ICameraDevice {
    
    private String deviceId;
    private String deviceName;
    private String description;
    private OpenCVFrameGrabber grabber;
    private OpenCVFrameRecorder recorder;
    private VideoFormat currentFormat;
    private boolean isOpen = false;
    private boolean isRecording = false;
    private boolean isPreviewing = false;
    private PreviewCallback previewCallback;
    private Thread previewThread;
    
    public JavaCVCamera(int cameraIndex) {
        this.deviceId = "camera_" + cameraIndex;
        this.deviceName = "Camera " + cameraIndex;
        this.description = "USB Camera (Index: " + cameraIndex + ")";
    }
    
    public JavaCVCamera(String deviceId, String deviceName, String description) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.description = description;
    }
    
    @Override
    public String getDeviceId() {
        return deviceId;
    }
    
    @Override
    public String getDeviceName() {
        return deviceName;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public List<VideoFormat> getSupportedFormats() {
        List<VideoFormat> formats = new ArrayList<>();
        // 添加常用分辨率格式
        formats.add(VideoFormat.Presets.VGA());
        formats.add(VideoFormat.Presets.HD720());
        formats.add(VideoFormat.Presets.FULL_HD());
        // 4K 可能需要检查设备能力
        return formats;
    }
    
    @Override
    public boolean open(VideoFormat format) {
        try {
            int cameraIndex = Integer.parseInt(deviceId.replace("camera_", ""));
            grabber = new OpenCVFrameGrabber(cameraIndex);
            
            if (format != null) {
                grabber.setImageWidth(format.getWidth());
                grabber.setImageHeight(format.getHeight());
                grabber.setFrameRate(format.getFrameRate());
            }
            
            grabber.start();
            currentFormat = format != null ? format : VideoFormat.Presets.HD720();
            isOpen = true;
            System.out.println("摄像头已打开: " + deviceName);
            return true;
        } catch (Exception e) {
            System.err.println("打开摄像头失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void close() {
        try {
            if (isRecording) {
                stopRecording();
            }
            if (isPreviewing) {
                stopPreview();
            }
            if (grabber != null) {
                grabber.stop();
                grabber.close();
            }
            isOpen = false;
            System.out.println("摄像头已关闭: " + deviceName);
        } catch (Exception e) {
            System.err.println("关闭摄像头失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isOpen() {
        return isOpen;
    }
    
    @Override
    public boolean startPreview(PreviewCallback callback) {
        if (!isOpen) {
            System.err.println("摄像头未打开");
            return false;
        }
        
        this.previewCallback = callback;
        this.isPreviewing = true;
        
        previewThread = new Thread(() -> {
            try {
                Frame frame;
                VideoFrame videoFrame = new VideoFrame();
                
                while (isPreviewing && (frame = grabber.grab()) != null) {
                    // 转换 Frame 为 VideoFrame
                    videoFrame.setData(frameToByteArray(frame));
                    videoFrame.setWidth(frame.imageWidth);
                    videoFrame.setHeight(frame.imageHeight);
                    videoFrame.setTimestamp(System.currentTimeMillis());
                    
                    if (previewCallback != null) {
                        previewCallback.onPreviewFrame(videoFrame);
                    }
                }
            } catch (Exception e) {
                if (previewCallback != null) {
                    previewCallback.onError(500, e.getMessage());
                }
            }
        });
        previewThread.setDaemon(true);
        previewThread.start();
        
        return true;
    }
    
    @Override
    public void stopPreview() {
        isPreviewing = false;
        if (previewThread != null) {
            previewThread.interrupt();
            previewThread = null;
        }
    }
    
    @Override
    public VideoFrame capturePhoto() {
        if (!isOpen) {
            System.err.println("摄像头未打开");
            return null;
        }
        
        try {
            Frame frame = grabber.grab();
            if (frame != null) {
                VideoFrame videoFrame = new VideoFrame();
                videoFrame.setData(frameToByteArray(frame));
                videoFrame.setWidth(frame.imageWidth);
                videoFrame.setHeight(frame.imageHeight);
                videoFrame.setTimestamp(System.currentTimeMillis());
                return videoFrame;
            }
        } catch (Exception e) {
            System.err.println("拍照失败: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean startRecording(String outputPath) {
        if (!isOpen) {
            System.err.println("摄像头未打开");
            return false;
        }
        
        try {
            recorder = new FFmpegFrameRecorder(
                outputPath,
                currentFormat.getWidth(),
                currentFormat.getHeight()
            );
            
            recorder.setFrameRate(currentFormat.getFrameRate());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(currentFormat.getBitrate());
            recorder.setFormat("mp4");
            
            recorder.start();
            isRecording = true;
            System.out.println("开始录像: " + outputPath);
            return true;
        } catch (Exception e) {
            System.err.println("开始录像失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void stopRecording() {
        if (!isRecording) {
            return;
        }
        
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.close();
            }
            isRecording = false;
            System.out.println("停止录像");
        } catch (Exception e) {
            System.err.println("停止录像失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isRecording() {
        return isRecording;
    }
    
    @Override
    public void setVideoParameters(int brightness, int contrast, int saturation) {
        // OpenCV 不直接支持这些参数调整
        // 需要通过相机属性或后期处理实现
        System.out.println("视频参数调整: brightness=" + brightness + 
            ", contrast=" + contrast + ", saturation=" + saturation);
    }
    
    @Override
    public double getCurrentFPS() {
        // 实际实现需要计算帧率
        return currentFormat != null ? currentFormat.getFrameRate() : 30.0;
    }
    
    /**
     * 将 Frame 转换为字节数组
     */
    private byte[] frameToByteArray(Frame frame) {
        if (frame == null || frame.image == null) {
            return new byte[0];
        }
        
        try {
            IplImage img = frameToIplImage(frame);
            ByteBuffer buffer = img.getByteBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    /**
     * 将 Frame 转换为 IplImage
     */
    private IplImage frameToIplImage(Frame frame) {
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        return converter.convert(frame);
    }
    
    /**
     * 保存 VideoFrame 为图片文件
     */
    public static boolean saveFrameToImage(VideoFrame frame, String outputPath) {
        try {
            BufferedImage img = frameToBufferedImage(frame);
            File output = new File(outputPath);
            ImageIO.write(img, "jpg", output);
            return true;
        } catch (Exception e) {
            System.err.println("保存图片失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 将 VideoFrame 转换为 BufferedImage
     */
    private static BufferedImage frameToBufferedImage(VideoFrame frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        img.getRaster().setDataElements(0, 0, width, height, frame.getData());
        return img;
    }
}
