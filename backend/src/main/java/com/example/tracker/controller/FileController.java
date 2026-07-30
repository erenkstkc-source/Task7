package com.example.tracker.controller;

import com.example.tracker.entity.FileAttachment;
import com.example.tracker.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired private FileService fileService;

    @PostMapping("/activities/{activityId}/files")
    public ResponseEntity<?> uploadFile(@PathVariable Long activityId, @RequestParam("file") MultipartFile file) {
        try {
            FileAttachment attachment = fileService.uploadFile(activityId, file);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/activities/{activityId}/files")
    public ResponseEntity<List<FileAttachment>> getFiles(@PathVariable Long activityId) {
        return ResponseEntity.ok(fileService.getFilesByActivity(activityId));
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        try {
            FileAttachment metadata = fileService.getFileMetadata(fileId);
            byte[] content = fileService.getFileContent(fileId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}