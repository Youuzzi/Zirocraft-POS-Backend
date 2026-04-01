package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.OrderEntity;
import com.zirocraft.billingsoftware.io.DashboardSummaryDTO;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderServiceImpl orderService;

    @PostMapping("/create")
    public OrderEntity checkout(@RequestBody OrderRequest request, @RequestParam String email, @RequestParam Long shiftId) {
        return orderService.createOrder(request, email, shiftId);
    }

    @GetMapping("/recent")
    public List<OrderEntity> getRecentOrders(@RequestParam(required = false) Long shiftId) {
        if (shiftId != null) return orderService.getOrdersByShift(shiftId);
        return orderService.getTodayOrders(); // Default Admin: Hari ini
    }

    @GetMapping("/search")
    public List<OrderEntity> search(@RequestParam String query) {
        return orderService.searchOrders(query);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public DashboardSummaryDTO getSummary() {
        return orderService.getDashboardSummary();
    }

    @DeleteMapping("/void/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> voidOrder(@PathVariable Long orderId, @RequestParam String reason, @RequestParam String adminEmail) {
        orderService.voidOrder(orderId, reason, adminEmail);
        return ResponseEntity.ok("VOID BERHASIL");
    }
}