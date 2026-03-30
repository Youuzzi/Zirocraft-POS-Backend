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

        // 2. Anti-Hack: Ambil data produk asli dari DB buat validasi harga & stok
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal totalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // 3. Build Order Master
        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName())
                .orderNumber("ZIRO-" + System.currentTimeMillis())
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("PENDING")
                .build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Barang " + item.getName() + " tidak valid!");

            // Cek & Potong Stok
            if (dbItem.getStock() < item.getQty()) {
                throw new RuntimeException("Stok " + dbItem.getName() + " tidak cukup!");
            }
            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            // Hitung Subtotal berdasarkan harga asli DB (Bukan harga kiriman frontend)
            BigDecimal subTotal = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            totalServer = totalServer.add(subTotal);

            orderItems.add(OrderItemEntity.builder()
                    .itemName(dbItem.getName())
                    .price(dbItem.getPrice())
                    .quantity(item.getQty())
                    .subTotal(subTotal)
                    .order(order)
                    .build());
        }

        order.setTotalAmount(totalServer);
        order.setItems(orderItems);

        // 4. Update Saldo Shift
        BigDecimal currentSales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        shift.setTotalSales(currentSales.add(totalServer));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }
}