package com.zirocraft.billingsoftware.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zirocraft.billingsoftware.io.ItemRequest;
import com.zirocraft.billingsoftware.io.ItemResponse;
import com.zirocraft.billingsoftware.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // JALUR UMUM: /api/v1.0/items (Admin & Kasir bisa akses lewat SecurityConfig)
    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.fetchItems());
    }

    // JALUR ADMIN: /api/v1.0/admin/items (Hanya Admin)
    @PostMapping("/admin/items")
    public ResponseEntity<ItemResponse> addItem(
            @RequestPart("item") String itemString,
            @RequestPart("file") MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ItemRequest request = mapper.readValue(itemString, ItemRequest.class);
        return new ResponseEntity<>(itemService.add(request, file), HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}