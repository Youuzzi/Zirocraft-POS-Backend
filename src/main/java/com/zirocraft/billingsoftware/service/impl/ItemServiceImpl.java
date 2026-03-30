package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.CategoryEntity;
import com.zirocraft.billingsoftware.entity.ItemEntity;
import com.zirocraft.billingsoftware.io.ItemRequest;
import com.zirocraft.billingsoftware.io.ItemResponse;
import com.zirocraft.billingsoftware.repository.CategoryRepository;
import com.zirocraft.billingsoftware.repository.ItemRepository;
import com.zirocraft.billingsoftware.service.FileUploadService;
import com.zirocraft.billingsoftware.service.ItemService;
import com.zirocraft.billingsoftware.util.SanitizerUtil; // IMPORT INI
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Override
    public ItemResponse add(ItemRequest request, MultipartFile file) {
        String imgUrl = fileUploadService.uploadFile(file);

        // Cuci input
        String cleanName = sanitizer.cleanTextOnly(request.getName());
        String cleanDesc = sanitizer.cleanTextOnly(request.getDescription());

        CategoryEntity existingCategory = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        ItemEntity newItem = ItemEntity.builder()
                .name(cleanName)
                .price(request.getPrice())
                .description(cleanDesc)
                .category(existingCategory)
                .imgUrl(imgUrl)
                // --- SET STOK DARI REQUEST KE ENTITY ---
                .stock(request.getStock())
                .build();

        ItemEntity savedItem = itemRepository.save(newItem);
        return convertToResponse(savedItem);
    }

    @Override
    public List<ItemResponse> fetchItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(String itemId) {
        ItemEntity item = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan"));
        fileUploadService.deleteFile(item.getImgUrl());
        itemRepository.delete(item);
    }

    private ItemResponse convertToResponse(ItemEntity entity) {
        String baseUrl = "http://localhost:8080/api/v1.0";
        String fullImgUrl = (entity.getImgUrl() != null) ? baseUrl + entity.getImgUrl() : null;

        return ItemResponse.builder()
                .itemId(entity.getItemId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .imgUrl(fullImgUrl)
                .stock(entity.getStock()) // <--- TAMBAHKAN INI
                .categoryId(entity.getCategory().getCategoryId())
                .categoryName(entity.getCategory().getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdateAt())
                .build();
    }
}