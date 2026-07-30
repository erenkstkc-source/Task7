package com.example.tracker.service;

import com.example.tracker.entity.Activity;
import com.example.tracker.entity.FileAttachment;
import com.example.tracker.repository.ActivityRepository;
import com.example.tracker.repository.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired private FileAttachmentRepository fileRepository;
    @Autowired private ActivityRepository activityRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public FileAttachment uploadFile(Long activityId, MultipartFile file) throws IOException {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Faaliyet bulunamadı!"));

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, file.getBytes());

        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(uniqueFileName);
        attachment.setOriginalName(originalFilename);
        attachment.setContentType(file.getContentType());
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setActivity(activity);

        return fileRepository.save(attachment);
    }

    public List<FileAttachment> getFilesByActivity(Long activityId) {
        return fileRepository.findByActivityId(activityId);
    }

    public byte[] getFileContent(Long fileId) throws IOException {
        FileAttachment attachment = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Dosya bulunamadı!"));
        Path filePath = Paths.get(uploadDir, attachment.getFileName());
        return Files.readAllBytes(filePath);
    }

    public FileAttachment getFileMetadata(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Dosya bulunamadı!"));
    }
}