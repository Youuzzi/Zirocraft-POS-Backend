package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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
        // 1. Verifikasi Shift & Kepemilikan
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Sesi Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus()) || !shift.getUserId().equals(email)) {
            throw new RuntimeException("AKSES ILEGAL: Sesi tidak valid!");
        }

        // 2. Anti-Hack: Ambil harga asli dari DB
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal totalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        OrderEntity order = OrderEntity.builder()
                .orderNumber("ZIRO-" + System.currentTimeMillis())
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email).shiftId(shiftId).status("PENDING").build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Barang " + item.getName() + " ilegal!");

            // Cek Stok
            if (dbItem.getStock() < item.getQty()) throw new RuntimeException("Stok " + item.getName() + " habis!");

            // Potong Stok
            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            BigDecimal sub = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            totalServer = totalServer.add(sub);
            orderItems.add(OrderItemEntity.builder().itemName(dbItem.getName()).price(dbItem.getPrice())
                    .quantity(item.getQty()).subTotal(sub).order(order).build());
        }

        order.setTotalAmount(totalServer);
        order.setItems(orderItems);

        // Update Saldo Shift
        BigDecimal currentSales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        shift.setTotalSales(currentSales.add(totalServer));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }
}