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

    // 1. AMBIL SEMUA MENU (Bisa diakses Admin & Kasir)
    // URL: /api/v1.0/items
    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.fetchItems());
    }

    // 2. TAMBAH MENU BARU (Hanya Admin)
    // URL: /api/v1.0/admin/items
    @PostMapping("/admin/items")
    public ResponseEntity<ItemResponse> addItem(
            @RequestPart("item") String itemString,
            @RequestPart("file") MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ItemRequest request = mapper.readValue(itemString, ItemRequest.class);
        return new ResponseEntity<>(itemService.add(request, file), HttpStatus.CREATED);
    }

    // 3. UPDATE / RESTOK MENU (Hanya Admin)
    // URL: /api/v1.0/admin/items/{itemId}
    @PutMapping("/admin/items/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable String itemId,
            @RequestPart("item") String itemString,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ItemRequest request = mapper.readValue(itemString, ItemRequest.class);

        // Memanggil logika update di service
        return ResponseEntity.ok(itemService.update(itemId, request, file));
    }

    // 4. HAPUS MENU (Hanya Admin)
    // URL: /api/v1.0/admin/items/{itemId}
    @DeleteMapping("/admin/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}