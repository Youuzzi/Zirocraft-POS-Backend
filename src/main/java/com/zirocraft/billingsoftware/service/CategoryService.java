package com.zirocraft.billingsoftware.service;

import com.zirocraft.billingsoftware.io.CategoryRequest;
import com.zirocraft.billingsoftware.io.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    CategoryResponse add(CategoryRequest request, MultipartFile file);

    List<CategoryResponse> read();

    void delete(String categoryId);
}
