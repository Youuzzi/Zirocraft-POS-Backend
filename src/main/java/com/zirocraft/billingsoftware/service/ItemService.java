package com.zirocraft.billingsoftware.service;

import com.zirocraft.billingsoftware.io.ItemRequest;
import com.zirocraft.billingsoftware.io.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ItemService {


    ItemResponse add(ItemRequest request, MultipartFile file);
    ItemResponse update(String itemId, ItemRequest request, MultipartFile file);

    List<ItemResponse> fetchItems();

    void deleteItem(String itemId);
}