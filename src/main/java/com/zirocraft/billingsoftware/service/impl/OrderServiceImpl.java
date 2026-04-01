package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.DashboardSummaryDTO;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final ShiftRepository shiftRepository;
    private final ItemRepository itemRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderEntity createOrder(OrderRequest request, String email, Long shiftId) {

        // 1. IDEMPOTENCY CHECK
        if (request.getIdempotencyKey() != null && orderRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new RuntimeException("Transaksi ini sudah masuk ke sistem. Silakan cek riwayat.");
        }

        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Sesi Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new RuntimeException("AKSES DITOLAK: Sesi shift ini sudah ditutup!");
        }

        BigDecimal subTotalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // 2. DEADLOCK PREVENTION & PESSIMISTIC LOCKING
        List<OrderRequest.CartItem> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(OrderRequest.CartItem::getName))
                .toList();

        for (OrderRequest.CartItem item : sortedItems) {
            ItemEntity dbItem = itemRepository.findByNameForUpdate(item.getName())
                    .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan: " + item.getName()));

            if (dbItem.getStock() < item.getQty()) {
                throw new RuntimeException("Stok " + dbItem.getName() + " tidak cukup! Sisa: " + dbItem.getStock());
            }

            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            BigDecimal lineTotal = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            subTotalServer = subTotalServer.add(lineTotal);

            orderItems.add(OrderItemEntity.builder()
                    .itemName(dbItem.getName())
                    .price(dbItem.getPrice())
                    .quantity(item.getQty())
                    .subTotal(lineTotal)
                    .build());
        }

        // 3. CLEAN FINANCIAL ROUNDING (Penentu Utama)
        BigDecimal serviceCharge = subTotalServer.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subTotalServer.add(serviceCharge).multiply(new BigDecimal("0.11")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal rawTotal = subTotalServer.add(serviceCharge).add(taxAmount);

        // Pembulatan ke 500 terdekat
        BigDecimal grandTotal = rawTotal.divide(new BigDecimal("500"), 0, RoundingMode.HALF_UP).multiply(new BigDecimal("500"));

        // 4. GENERATE QUEUE
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        Integer maxQueue = orderRepository.findMaxQueueNumberToday(start, end);
        int nextQueue = (maxQueue == null) ? 1 : maxQueue + 1;

        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName() == null || request.getCustomerName().isEmpty() ? "Walk-in" : request.getCustomerName())
                .orderNumber("ZIRO-" + (int)(Math.random() * 90000 + 10000))
                .tableNumber(request.getTableNumber())
                .subTotal(subTotalServer).serviceCharge(serviceCharge).taxAmount(taxAmount).totalAmount(grandTotal)
                .paymentType(request.getPaymentType()).userId(email).shiftId(shiftId).status("COMPLETED")
                .idempotencyKey(request.getIdempotencyKey()).queueNumber(nextQueue).build();

        for(OrderItemEntity oi : orderItems) { oi.setOrder(order); }
        order.setItems(orderItems);

        shift.setTotalSales(shift.getTotalSales().add(grandTotal));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void voidOrder(Long orderId, String reason, String adminEmail) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan!"));

        if ("VOID".equals(order.getStatus())) throw new RuntimeException("Sudah dibatalkan!");

        // --- LOGIC AUDIT: ADMIN BYPASS ---
        String currentRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        boolean isAdmin = currentRole.contains("ROLE_ADMIN");

        ShiftEntity orderShift = shiftRepository.findById(order.getShiftId()).get();

        if (!isAdmin && !"OPEN".equals(orderShift.getStatus())) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin yang bisa membatalkan transaksi dari shift yang sudah tutup!");
        }

        // Kembalikan Stok
        for (OrderItemEntity oi : order.getItems()) {
            ItemEntity it = itemRepository.findByNameForUpdate(oi.getItemName()).get();
            it.setStock(it.getStock() + oi.getQuantity());
            itemRepository.save(it);
        }

        // Kurangi Sales di Shift Asal
        orderShift.setTotalSales(orderShift.getTotalSales().subtract(order.getTotalAmount()));
        shiftRepository.save(orderShift);

        order.setStatus("VOID");
        order.setVoidReason(reason);
        order.setVoidBy(adminEmail);
        orderRepository.save(order);
    }

    // Fungsi getTodayOrders, getOrdersByShift, searchOrders tetap sama...
    public List<OrderEntity> getTodayOrders() {
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        return orderRepository.findByCreatedAtBetweenOrderByIdDesc(start, end);
    }
}