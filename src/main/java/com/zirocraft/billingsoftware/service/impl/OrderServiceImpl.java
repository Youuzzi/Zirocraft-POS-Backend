package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.DashboardSummaryDTO;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation; // WAJIB
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
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
    private final UserRepository userRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderEntity createOrder(OrderRequest request, String email, Long shiftId) {

        // 1. IDEMPOTENCY CHECK
        if (request.getIdempotencyKey() != null && orderRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new RuntimeException("Transaksi ini sudah diproses. Silakan cek riwayat.");
        }

        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Sesi Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new RuntimeException("AKSES DITOLAK: Sesi shift ini sudah ditutup!");
        }

        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        Integer maxQueue = orderRepository.findMaxQueueNumberToday(start, end);
        int nextQueue = (maxQueue == null) ? 1 : maxQueue + 1;

        BigDecimal subTotalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // 2. DEADLOCK PREVENTION (Sort by name)
        List<OrderRequest.CartItem> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(OrderRequest.CartItem::getName))
                .toList();

        for (OrderRequest.CartItem item : sortedItems) {
            if (item.getQty() == null || item.getQty() <= 0) throw new RuntimeException("Jumlah barang tidak valid!");

            // 3. PESSIMISTIC LOCKING (Gembok baris DB)
            ItemEntity dbItem = itemRepository.findByNameForUpdate(item.getName())
                    .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan: " + item.getName()));

            if (dbItem.getStock() < item.getQty()) {
                throw new RuntimeException("Stok " + dbItem.getName() + " tidak mencukupi! Sisa: " + dbItem.getStock());
            }

            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            BigDecimal lineTotal = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            subTotalServer = subTotalServer.add(lineTotal);

            orderItems.add(OrderItemEntity.builder().itemName(dbItem.getName()).price(dbItem.getPrice())
                    .quantity(item.getQty()).subTotal(lineTotal).build());
        }

        // --- BACKEND FINAL ROUNDING (THE SOURCE OF TRUTH) ---
        BigDecimal serviceCharge = subTotalServer.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subTotalServer.add(serviceCharge).multiply(new BigDecimal("0.11")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal rawTotal = subTotalServer.add(serviceCharge).add(taxAmount);

        // Pembulatan 500 Terdekat (IndustrialUMKM Standard)
        BigDecimal grandTotal = rawTotal.divide(new BigDecimal("500"), 0, RoundingMode.HALF_UP).multiply(new BigDecimal("500"));

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

        // --- STRICT VOID LOGIC (SAFETY AUDIT) ---
        ShiftEntity currentActiveShift = shiftRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(order.getUserId()) && "OPEN".equals(s.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Kasir tidak memiliki shift aktif untuk melakukan void!"));

        if (!order.getShiftId().equals(currentActiveShift.getId())) {
            throw new RuntimeException("Akses Ditolak: Transaksi di luar shift aktif tidak bisa dibatalkan.");
        }

        if ("VOID".equals(order.getStatus())) throw new RuntimeException("Sudah dibatalkan!");

        for (OrderItemEntity oi : order.getItems()) {
            ItemEntity it = itemRepository.findByNameForUpdate(oi.getItemName()).get();
            it.setStock(it.getStock() + oi.getQuantity());
            itemRepository.save(it);
        }

        currentActiveShift.setTotalSales(currentActiveShift.getTotalSales().subtract(order.getTotalAmount()));
        shiftRepository.save(currentActiveShift);

        order.setStatus("VOID");
        order.setVoidReason(reason);
        order.setVoidBy(adminEmail);
        orderRepository.save(order);
    }

    public List<OrderEntity> getTodayOrders() {
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        return orderRepository.findByCreatedAtBetweenOrderByIdDesc(start, end);
    }

    public List<OrderEntity> getOrdersByShift(Long shiftId) {
        return orderRepository.findTop5ByShiftIdOrderByIdDesc(shiftId);
    }

    public List<OrderEntity> searchOrders(String query) {
        return orderRepository.findByOrderNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrderByIdDesc(query, query);
    }

    public DashboardSummaryDTO getDashboardSummary() {
        List<OrderEntity> all = orderRepository.findAll();
        BigDecimal sales = all.stream().filter(o -> "COMPLETED".equals(o.getStatus())).map(OrderEntity::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return DashboardSummaryDTO.builder().totalSales(sales).totalOrders((long)all.size()).totalProducts(itemRepository.count()).totalUsers(userRepository.count()).build();
    }
}