package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final ShiftRepository shiftRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderEntity createOrder(OrderRequest request, String email, Long shiftId) {
        // 1. KEAMANAN: Verifikasi Sesi
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        if (!shift.getStatus().equals("OPEN") || !shift.getUserId().equals(email)) {
            throw new RuntimeException("ILEGAL: Sesi tidak valid atau sudah ditutup!");
        }

        // 2. LOGIKA: Hitung Ulang & Validasi Barang
        BigDecimal serverTotal = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        OrderEntity order = OrderEntity.builder()
                .orderNumber("ZIRO-" + System.currentTimeMillis())
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("PENDING") // Status awal nota
                .build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = itemRepository.findAll().stream()
                    .filter(i -> i.getName().equals(item.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item " + item.getName() + " tidak terdaftar!"));

            BigDecimal subTotal = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            serverTotal = serverTotal.add(subTotal);

            orderItems.add(OrderItemEntity.builder()
                    .itemName(dbItem.getName())
                    .price(dbItem.getPrice())
                    .quantity(item.getQty())
                    .subTotal(subTotal)
                    .order(order)
                    .build());
        }

        order.setTotalAmount(serverTotal);
        order.setItems(orderItems);

        // 3. UPDATE: Saldo Laci
        shift.setTotalSales(shift.getTotalSales().add(serverTotal));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }
}