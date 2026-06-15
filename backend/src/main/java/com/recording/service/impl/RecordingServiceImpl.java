package com.recording.service.impl;

import com.recording.model.*;
import com.recording.service.RecordingService;
import jakarta.annotation.PreDestroy;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecordingServiceImpl implements RecordingService {

    private static final Logger log = LoggerFactory.getLogger(RecordingServiceImpl.class);

    @Value("${recording.output-dir:./output}")
    private String outputDir;

    private volatile boolean isRecording = false;
    private volatile boolean isPaused = false;
    private volatile long startTime = 0;
    private volatile long pausedDuration = 0;
    private volatile long pauseStart = 0;

    private FFmpegFrameGrabber videoGrabber;
    private FFmpegFrameRecorder videoRecorder;
    private TargetDataLine audioLine;
    private String currentOutputPath;
    private int selectedCameraIndex = -1;
    private int selectedAudioIndex = -1;

    private void ensureOutputDir() {
        new File(outputDir).mkdirs();
    }

    @Override
    public List<DeviceInfo> getCameraDevices() {
        List<DeviceInfo> devices = new ArrayList<>();
        // 尝试 dshow 格式枚举设备
        for (int i = 0; i < 10; i++) {
            try {
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(Integer.toString(i));
                grabber.setFormat("dshow");
                grabber.setImageWidth(640);
                grabber.setImageHeight(480);
                grabber.setFrameRate(25);
                grabber.start();
                devices.add(DeviceInfo.builder()
                        .id("camera_" + i)
                        .name("Camera " + (i + 1))
                        .description("DirectShow device " + i)
                        .type("camera")
                        .index(i)
                        .isOpen(false)
                        .build());
                grabber.stop();
                grabber.close();
            } catch (Exception e) {
                // No more devices
                break;
            }
        }
        if (devices.isEmpty()) {
            devices.add(DeviceInfo.builder()
                    .id("camera_0")
                    .name("Default Camera")
                    .description("Default video device")
                    .type("camera")
                    .index(0)
                    .isOpen(false)
                    .build());
        }
        return devices;
    }

    @Override
    public List<DeviceInfo> getAudioDevices() {
        List<DeviceInfo> devices = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixerInfos.length; i++) {
            Mixer.Info info = mixerInfos[i];
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info targetLineInfo = new Line.Info(TargetDataLine.class);
            if (mixer.isLineSupported(targetLineInfo)) {
                String name = info.getName().toLowerCase();
                if (!name.contains("port") && !name.contains("mute")) {
                    devices.add(DeviceInfo.builder()
                            .id("audio_" + i)
                            .name(info.getName())
                            .description(info.getDescription())
                            .type("audio")
                            .index(i)
                            .isOpen(false)
                            .build());
                }
            }
        }
        return devices;
    }

    @Override
    public boolean openDevice(String deviceId, String type) {
        if ("camera".equals(type)) {
            selectedCameraIndex = extractIndex(deviceId);
            log.info("Selected camera index={}", selectedCameraIndex);
        } else {
            selectedAudioIndex = extractIndex(deviceId);
            log.info("Selected audio index={}", selectedAudioIndex);
        }
        return true;
    }

    @Override
    public boolean closeDevice(String deviceId, String type) {
        if ("camera".equals(type)) { selectedCameraIndex = -1; }
        else { selectedAudioIndex = -1; }
        return true;
    }

    @Override
    public void startRecording(RecordingConfigRequest config) {
        if (isRecording) throw new IllegalStateException("Recording already in progress");
        ensureOutputDir();

        int width = config.getVideoWidth() != null ? config.getVideoWidth() : 1280;
        int height = config.getVideoHeight() != null ? config.getVideoHeight() : 720;
        double frameRate = config.getFrameRate() != null ? config.getFrameRate() : 30.0;
        int videoBitrate = config.getVideoBitrate() != null ? config.getVideoBitrate().intValue() : 2500000;
        int audioSampleRate = config.getAudioSampleRate() != null ? config.getAudioSampleRate() : 44100;
        int audioChannels = config.getAudioChannels() != null ? config.getAudioChannels() : 2;
        int audioBitrate = config.getAudioBitrate() != null ? config.getAudioBitrate().intValue() : 128000;

        String outputPath = config.getOutputPath();
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = outputDir + "/recording_" + System.currentTimeMillis() + ".mp4";
        }
        this.currentOutputPath = outputPath;

        try {
            if (selectedCameraIndex >= 0) {
                videoGrabber = new FFmpegFrameGrabber(Integer.toString(selectedCameraIndex));
                videoGrabber.setFormat("dshow");
                videoGrabber.setImageWidth(width);
                videoGrabber.setImageHeight(height);
                videoGrabber.setFrameRate(frameRate);
                videoGrabber.setVideoOption("framerate", String.valueOf((int) frameRate));
                videoGrabber.start();
                width = videoGrabber.getImageWidth();
                height = videoGrabber.getImageHeight();
            }

            videoRecorder = new FFmpegFrameRecorder(outputPath, width, height, audioChannels);
            videoRecorder.setFormat("mp4");
            videoRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            videoRecorder.setVideoBitrate(videoBitrate);
            videoRecorder.setFrameRate(frameRate);
            videoRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            videoRecorder.setAudioBitrate(audioBitrate);
            videoRecorder.setSampleRate(audioSampleRate);
            videoRecorder.setAudioChannels(audioChannels);
            videoRecorder.start();

            isRecording = true;
            isPaused = false;
            startTime = System.currentTimeMillis();
            pausedDuration = 0;

            new Thread(this::recordLoop, "recording-thread").start();
            log.info("Recording started: {}x{}@{}fps -> {}", width, height, frameRate, outputPath);
        } catch (Exception e) {
            cleanup();
            throw new RuntimeException("Failed to start recording: " + e.getMessage(), e);
        }
    }

    private void recordLoop() {
        try {
            while (isRecording) {
                if (isPaused) { Thread.sleep(50); continue; }
                if (videoGrabber != null) {
                    Frame frame = videoGrabber.grab();
                    if (frame != null) videoRecorder.record(frame);
                }
            }
        } catch (Exception e) {
            log.error("Recording error: {}", e.getMessage());
        } finally {
            cleanup();
        }
    }

    @Override
    public void stopRecording() {
        if (!isRecording && !isPaused) return;
        isRecording = false;
        isPaused = false;
        log.info("Recording stopped: {}", currentOutputPath);
    }

    @Override
    public void pauseRecording() {
        if (!isRecording || isPaused) return;
        isPaused = true;
        pauseStart = System.currentTimeMillis();
    }

    @Override
    public void resumeRecording() {
        if (!isPaused) return;
        pausedDuration += System.currentTimeMillis() - pauseStart;
        isPaused = false;
    }

    @Override
    public String capturePhoto(String deviceId) {
        ensureOutputDir();
        int idx = extractIndex(deviceId);
        if (idx < 0 && selectedCameraIndex >= 0) idx = selectedCameraIndex;
        if (idx < 0) idx = 0;

        String photoPath = outputDir + "/photo_" + System.currentTimeMillis() + ".jpg";
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(Integer.toString(idx));
            grabber.setFormat("dshow");
            grabber.start();

            Frame frame = grabber.grabImage();
            if (frame != null) {
                try (OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat()) {
                    Mat mat = converter.convert(frame);
                    if (mat != null) {
                        opencv_core.flip(mat, mat, 1);
                        opencv_imgcodecs.imwrite(photoPath, mat);
                        mat.close();
                    }
                }
            }
            log.info("Photo saved: {}", photoPath);
            return photoPath;
        } catch (Exception e) {
            // Fallback: try without dshow format
            try {
                if (grabber != null) { grabber.stop(); grabber.close(); }
                grabber = new FFmpegFrameGrabber(Integer.toString(idx));
                grabber.start();
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    try (OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat()) {
                        Mat mat = converter.convert(frame);
                        if (mat != null) {
                            opencv_core.flip(mat, mat, 1);
                            opencv_imgcodecs.imwrite(photoPath, mat);
                            mat.close();
                        }
                    }
                }
                log.info("Photo saved (fallback): {}", photoPath);
                return photoPath;
            } catch (Exception e2) {
                throw new RuntimeException("Photo capture failed: " + e2.getMessage(), e2);
            }
        } finally {
            if (grabber != null) {
                try { grabber.stop(); grabber.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public RecordingStatus getStatus() {
        long duration = 0;
        if (isRecording || isPaused) {
            long elapsed = System.currentTimeMillis() - startTime - pausedDuration;
            if (isPaused) elapsed -= (System.currentTimeMillis() - pauseStart);
            duration = Math.max(0, elapsed);
        }
        long fileSize = 0;
        if (currentOutputPath != null) {
            File f = new File(currentOutputPath);
            if (f.exists()) fileSize = f.length();
        }
        return RecordingStatus.builder()
                .recording(isRecording).paused(isPaused)
                .duration(duration).fileSize(fileSize)
                .outputPath(currentOutputPath).build();
    }

    @Override
    public List<FileInfo> getFileList() {
        List<FileInfo> files = new ArrayList<>();
        File dir = new File(outputDir);
        if (!dir.isDirectory()) return files;
        File[] arr = dir.listFiles();
        if (arr == null) return files;
        Arrays.sort(arr, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (File f : arr) {
            if (!f.isFile()) continue;
            String name = f.getName();
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
            String type;
            switch (ext) {
                case "mp4": case "avi": case "mkv": case "mov": type = "video"; break;
                case "mp3": case "wav": case "aac": type = "audio"; break;
                case "jpg": case "jpeg": case "png": type = "image"; break;
                default: type = "other";
            }
            files.add(FileInfo.builder()
                    .id(UUID.randomUUID().toString())
                    .name(name).path(f.getAbsolutePath())
                    .size(f.length()).type(type)
                    .createTime(LocalDateTime.now()).build());
        }
        return files;
    }

    @Override
    public String getFilePath(String fileId) {
        return outputDir + File.separator + fileId;
    }

    @Override
    public boolean deleteFile(String fileId) {
        File f = new File(outputDir, fileId);
        return f.exists() && f.delete();
    }

    private void cleanup() {
        try { if (videoGrabber != null) { videoGrabber.stop(); videoGrabber.close(); videoGrabber = null; } } catch (Exception e) { log.warn("Error closing grabber: {}", e.getMessage()); }
        try { if (videoRecorder != null) { videoRecorder.stop(); videoRecorder.close(); videoRecorder = null; } } catch (Exception e) { log.warn("Error closing recorder: {}", e.getMessage()); }
        try { if (audioLine != null) { audioLine.stop(); audioLine.close(); audioLine = null; } } catch (Exception e) { log.warn("Error closing audio: {}", e.getMessage()); }
    }

    private int extractIndex(String deviceId) {
        if (deviceId == null) return -1;
        String[] parts = deviceId.split("_");
        return parts.length > 1 ? Integer.parseInt(parts[parts.length - 1]) : -1;
    }

    @PreDestroy
    public void destroy() {
        if (isRecording) stopRecording();
        cleanup();
    }
}
