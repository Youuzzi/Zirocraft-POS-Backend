package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {
    private final OrderRepository orderRepository;
    private final ShiftRepository shiftRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderEntity createOrder(OrderRequest request, String email, Long shiftId) {
        // 1. Verifikasi Sesi & Kasir
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus()) || !shift.getUserId().equals(email)) {
            throw new RuntimeException("AKSES ILEGAL!");
        }

        // 2. LOGIKA DAILY QUEUE (Reset Tiap Hari)
        Timestamp startOfDay = Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        Timestamp endOfDay = Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        long countToday = orderRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        Integer nextQueue = (int) countToday + 1;

        // 3. Validasi Produk & Hitung Ulang (Anti-Hack)
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal totalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // Build Master Nota
        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName())
                .orderNumber("ZIRO-" + (int)(Math.random() * 90000 + 10000)) // Random ID
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("COMPLETED") // STATUS SEKARANG OTOMATIS LUNAS
                .queueNumber(nextQueue) // MASUKKAN NOMOR ANTREAN ASLI
                .build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Item ilegal!");

            // Potong Stok
            if (dbItem.getStock() < item.getQty()) throw new RuntimeException("Stok habis!");
            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            BigDecimal sub = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            totalServer = totalServer.add(sub);
            orderItems.add(OrderItemEntity.builder().itemName(dbItem.getName()).price(dbItem.getPrice())
                    .quantity(item.getQty()).subTotal(sub).order(order).build());
        }

        order.setTotalAmount(totalServer);
        order.setItems(orderItems);

        // Update Sales di Shift
        BigDecimal currentSales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        shift.setTotalSales(currentSales.add(totalServer));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }
}