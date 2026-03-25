package com.zirocraft.billingsoftware.controller;

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
@RequestMapping("/api/v1.0/items")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> addItem(
            @ModelAttribute ItemRequest request,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(itemService.add(request, file), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.fetchItems());
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable String itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("Item berhasil dihapus beserta gambarnya!");
    }
}