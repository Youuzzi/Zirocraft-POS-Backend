package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    // Gunakan folder relatif project agar aman saat di-hosting
    private final String UPLOAD_DIR = "uploads/";

    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File kosong!");

        // Validasi Malware lewat ekstensi
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("MALWARE ALERT: Ekstensi file dilarang!");
        }

        try {
            // Path dinamis
            Path root = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
            if (!Files.exists(root)) Files.createDirectories(root);

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Gagal simpan file ke server.");
        }
    }

    @Override
    public boolean deleteFile(String imgUrl) {
        if (imgUrl == null || imgUrl.isEmpty()) return true;
        try {
            String fileName = imgUrl.replace("/uploads/", "");
            Path filePath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) { return false; }
    }
}