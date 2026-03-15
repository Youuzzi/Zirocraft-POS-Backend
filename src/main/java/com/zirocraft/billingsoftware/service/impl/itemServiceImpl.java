package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.CategoryEntity;
import com.zirocraft.billingsoftware.entity.ItemEntity;
import com.zirocraft.billingsoftware.io.ItemRequest;
import com.zirocraft.billingsoftware.io.ItemResponse;
import com.zirocraft.billingsoftware.repository.CategoryRepository;
import com.zirocraft.billingsoftware.repository.ItemRepository;
import com.zirocraft.billingsoftware.service.FileUploadService;
import com.zirocraft.billingsoftware.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class itemServiceImpl implements ItemService {

    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemResponse add(ItemRequest request, MultipartFile file) {
        String imgUrl = fileUploadService.uploadFile(file);
        ItemEntity newItem = convertToEntity(request);


        CategoryEntity existingCategory = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        newItem.setCategory(existingCategory);
        newItem.setImgUrl(imgUrl);


        ItemEntity savedItem = itemRepository.save(newItem);
        return convertToResponse(savedItem);
    }

    @Override
    public List<ItemResponse> fetchItems() {
        // Jangan List.of() lagi, ambil data beneran
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


    private ItemEntity convertToEntity(ItemRequest request) {
        return ItemEntity.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
    }

    private ItemResponse convertToResponse(ItemEntity entity) {
        return ItemResponse.builder()
                .itemId(entity.getItemId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .imgUrl(entity.getImgUrl())
                .categoryId(entity.getCategory().getCategoryId())
                .categoryName(entity.getCategory().getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdateAt())
                .build();
    }
}