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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    // Fotonya nanti masuk ke folder "uploads" di dalam projectmu
    // Ganti baris UPLOAD_DIR yang lama dengan ini:
    private final String UPLOAD_DIR = "C:/Users/HP/Documents/SpringBootPOS/backend/billingsoftware/src/main/resources/static/uploads/";

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 1. Cek folder, kalau belum ada kita buatkan otomatis
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Kasih nama unik biar gak bentrok (pake UUID)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 3. Simpan file ke harddisk laptop
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Balikin alamat file-nya biar bisa disimpan di database
            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Gagal upload gambar: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String imgUrl) {
        if (imgUrl == null) return false;
        try {
            String fileName = imgUrl.replace("/uploads/", "");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}