package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final String UPLOAD_DIR = "C:/Users/HP/Documents/SpringBootPOS/backend/billingsoftware/src/main/resources/static/uploads/";

    // DAFTAR EKSTENSI YANG DIIZINKAN (Whitelist)
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. VALIDASI: Cek apakah file kosong
        if (file.isEmpty()) {
            throw new RuntimeException("Gagal upload: File kosong, Zi!");
        }

        // 2. VALIDASI KEAMANAN: Cek MIME Type (Mencegah file .exe, .php, .html masuk)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_EXTENSIONS.contains(contentType.toLowerCase())) {
            throw new RuntimeException("ILEGAL FILE! Hanya boleh upload gambar (JPG/PNG/WEBP) untuk mencegah penyusupan!");
        }

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 3. Nama file unik dengan UUID
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Gagal simpan file: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String imgUrl) {
        if (imgUrl == null || imgUrl.isEmpty()) return true;
        try {
            String fileName = imgUrl.replace("/uploads/", "");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}