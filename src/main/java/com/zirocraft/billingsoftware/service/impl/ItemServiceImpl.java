package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.CategoryEntity;
import com.zirocraft.billingsoftware.entity.ItemEntity;
import com.zirocraft.billingsoftware.io.ItemRequest;
import com.zirocraft.billingsoftware.io.ItemResponse;
import com.zirocraft.billingsoftware.repository.CategoryRepository;
import com.zirocraft.billingsoftware.repository.ItemRepository;
import com.zirocraft.billingsoftware.service.FileUploadService;
import com.zirocraft.billingsoftware.service.ItemService;
import com.zirocraft.billingsoftware.util.SanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final SanitizerUtil sanitizer;

    // 1. TAMBAH MENU BARU
    @Override
    @Transactional
    public ItemResponse add(ItemRequest request, MultipartFile file) {
        // Upload gambar dan dapatkan URL-nya
        String imgUrl = fileUploadService.uploadFile(file);

        // Cuci input teks agar aman dari injeksi script/link
        String cleanName = sanitizer.cleanTextOnly(request.getName());
        String cleanDesc = sanitizer.cleanTextOnly(request.getDescription());

        // Cari kategori berdasarkan ID dari request
        CategoryEntity existingCategory = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan: " + request.getCategoryId()));

        // Bangun Entity Item
        ItemEntity newItem = ItemEntity.builder()
                .name(cleanName)
                .price(request.getPrice())
                .description(cleanDesc)
                .category(existingCategory)
                .imgUrl(imgUrl)
                .stock(request.getStock()) // Set Stok Awal
                .build();

        ItemEntity savedItem = itemRepository.save(newItem);
        return convertToResponse(savedItem);
    }

    // 2. AMBIL SEMUA DAFTAR MENU
    @Override
    public List<ItemResponse> fetchItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 3. UPDATE / RESTOK MENU (LOGIKA BARU)
    @Override
    @Transactional
    public ItemResponse update(String itemId, ItemRequest request, MultipartFile file) {
        // Cari barang lama di database
        ItemEntity existingItem = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Menu dengan ID " + itemId + " tidak ditemukan!"));

        // Update data teks (setelah dicuci)
        existingItem.setName(sanitizer.cleanTextOnly(request.getName()));
        existingItem.setPrice(request.getPrice());
        existingItem.setDescription(sanitizer.cleanTextOnly(request.getDescription()));
        existingItem.setStock(request.getStock()); // UPDATE STOK DI SINI

        // Update Kategori jika berubah
        CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak valid!"));
        existingItem.setCategory(category);

        // Update Gambar (Hanya jika ada file baru yang dikirim)
        if (file != null && !file.isEmpty()) {
            // Hapus file fisik lama di folder uploads agar tidak penuh
            fileUploadService.deleteFile(existingItem.getImgUrl());
            // Upload yang baru
            String newImgUrl = fileUploadService.uploadFile(file);
            existingItem.setImgUrl(newImgUrl);
        }

        ItemEntity updatedItem = itemRepository.save(existingItem);
        return convertToResponse(updatedItem);
    }

    // 4. HAPUS MENU
    @Override
    @Transactional
    public void deleteItem(String itemId) {
        ItemEntity item = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Menu tidak ditemukan"));

        // Hapus file gambarnya juga
        fileUploadService.deleteFile(item.getImgUrl());
        itemRepository.delete(item);
    }

    // --- HELPER: KONVERSI DARI ENTITY KE RESPONSE DTO ---
    private ItemResponse convertToResponse(ItemEntity entity) {
        String baseUrl = "http://localhost:8080/api/v1.0";
        String fullImgUrl = (entity.getImgUrl() != null) ? baseUrl + entity.getImgUrl() : null;

        return ItemResponse.builder()
                .itemId(entity.getItemId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .imgUrl(fullImgUrl)
                .stock(entity.getStock()) // Mengirim data stok ke frontend
                .categoryId(entity.getCategory().getCategoryId())
                .categoryName(entity.getCategory().getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdateAt())
                .build();
    }
}