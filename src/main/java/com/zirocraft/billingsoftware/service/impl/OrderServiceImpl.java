package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.*;
import com.zirocraft.billingsoftware.io.OrderRequest;
import com.zirocraft.billingsoftware.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Transactional
    public OrderEntity createOrder(OrderRequest request, String email, Long shiftId) {
        // 1. Validasi Sesi Shift
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Sesi Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new RuntimeException("AKSES DITOLAK: Sesi shift ini sudah ditutup!");
        }

        // 2. LOGIKA ANTREAN SAKTI (Reset Setiap Hari)
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        Integer maxQueue = orderRepository.findMaxQueueNumberToday(start, end);
        int nextQueue = (maxQueue == null) ? 1 : maxQueue + 1;

        // 3. Ambil data produk dari DB untuk validasi harga (Anti-Hack)
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal subTotalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // 4. Proses Item & Potong Stok
        for (OrderRequest.CartItem item : request.getItems()) {
            // Gembok manipulasi jumlah
            if (item.getQty() == null || item.getQty() <= 0) {
                throw new RuntimeException("Jumlah barang tidak valid!");
            }

            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Barang tidak ditemukan: " + item.getName());

            // Validasi Stok
            if (dbItem.getStock() < item.getQty()) {
                throw new RuntimeException("Stok " + dbItem.getName() + " tidak mencukupi!");
            }

            // Eksekusi Potong Stok
            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            // Hitung Subtotal per baris
            BigDecimal lineTotal = dbItem.getPrice().multiply(new BigDecimal(item.getQty()));
            subTotalServer = subTotalServer.add(lineTotal);

            // Bangun Detail Item Order
            orderItems.add(OrderItemEntity.builder()
                    .itemName(dbItem.getName())
                    .price(dbItem.getPrice())
                    .quantity(item.getQty())
                    .subTotal(lineTotal)
                    .build());
        }

        // 5. LOGIKA FINANSIAL (Pajak & Service Charge)
        // Service Charge: 5% dari Subtotal
        BigDecimal serviceCharge = subTotalServer.multiply(new BigDecimal("0.05"))
                .setScale(0, RoundingMode.HALF_UP);

        // PPN: 11% dari (Subtotal + Service Charge)
        BigDecimal taxAmount = subTotalServer.add(serviceCharge).multiply(new BigDecimal("0.11"))
                .setScale(0, RoundingMode.HALF_UP);

        // Grand Total
        BigDecimal grandTotal = subTotalServer.add(serviceCharge).add(taxAmount);

        // 6. Bangun Master Order
        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName() == null || request.getCustomerName().isEmpty() ? "Walk-in Customer" : request.getCustomerName())
                .orderNumber("ZIRO-" + (int)(Math.random() * 90000 + 10000))
                .tableNumber(request.getTableNumber())
                .subTotal(subTotalServer)
                .serviceCharge(serviceCharge)
                .taxAmount(taxAmount)
                .totalAmount(grandTotal)
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("COMPLETED")
                .queueNumber(nextQueue)
                .build();

        // Hubungkan item ke order (agar order_id terisi di tbl_order_items)
        for(OrderItemEntity oi : orderItems) {
            oi.setOrder(order);
        }
        order.setItems(orderItems);

        // 7. Update Akumulasi Penjualan di Shift
        BigDecimal currentShiftSales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        shift.setTotalSales(currentShiftSales.add(grandTotal));
        shiftRepository.save(shift);

        // 8. Final Save
        return orderRepository.save(order);
    }
}