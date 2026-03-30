package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.OrderEntity;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping("/create")
    public OrderEntity checkout(@RequestBody OrderRequest request,
                                @RequestParam String email,
                                @RequestParam Long shiftId) {
        return orderService.createOrder(request, email, shiftId);
    }
}