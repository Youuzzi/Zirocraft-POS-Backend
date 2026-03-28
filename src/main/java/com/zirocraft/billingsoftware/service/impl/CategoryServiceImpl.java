package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.CategoryEntity;
import com.zirocraft.billingsoftware.io.CategoryRequest;
import com.zirocraft.billingsoftware.io.CategoryResponse;
import com.zirocraft.billingsoftware.repository.CategoryRepository;
import com.zirocraft.billingsoftware.repository.ItemRepository;
import com.zirocraft.billingsoftware.service.CategoryService;
import com.zirocraft.billingsoftware.service.FileUploadService;
import com.zirocraft.billingsoftware.util.SanitizerUtil; // IMPORT INI
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ItemRepository itemRepository;
    private final SanitizerUtil sanitizer; // 1. INJECT SANITIZER

    @Override
    public CategoryResponse add(CategoryRequest request, MultipartFile file) {
        String imgUrl = fileUploadService.uploadFile(file);

        // 2. CUCI INPUT DISINI
        String cleanName = sanitizer.cleanTextOnly(request.getName());
        String cleanDesc = sanitizer.cleanTextOnly(request.getDescription());

        CategoryEntity newCategory = CategoryEntity.builder()
                .categoryId(UUID.randomUUID().toString())
                .name(cleanName) // Pake yang bersih
                .description(cleanDesc) // Pake yang bersih
                .bgColor(request.getBgColor())
                .imgUrl(imgUrl)
                .build();

        newCategory = categoryRepository.save(newCategory);
        return convertToResponse(newCategory);
    }

    @Override
    public List<CategoryResponse> read() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String categoryId) {
        CategoryEntity existingCategory = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found:" + categoryId));
        fileUploadService.deleteFile(existingCategory.getImgUrl());
        categoryRepository.delete(existingCategory);
    }

    private CategoryResponse convertToResponse(CategoryEntity newCategory) {
        Integer itemsCount = itemRepository.countByCategoryId(newCategory.getId());

        return CategoryResponse.builder()
                .categoryId(newCategory.getCategoryId())
                .name(newCategory.getName())
                .description(newCategory.getDescription())
                .bgColor(newCategory.getBgColor())
                .imgUrl(newCategory.getImgUrl())
                .createdAt(newCategory.getCreatedAt())
                .updatedAt(newCategory.getUpdatedAt())
                .items(itemsCount)
                .build();
    }
}