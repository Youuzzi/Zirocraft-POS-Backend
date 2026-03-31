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
        // 1. Verifikasi Shift & Kepemilikan
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Sesi Shift tidak ditemukan!"));

        if (!"OPEN".equals(shift.getStatus()) || !shift.getUserId().equals(email)) {
            throw new RuntimeException("AKSES ILEGAL: Sesi tidak valid!");
        }

        // 2. LOGIKA ANTREAN SAKTI (Sequential Daily Queue)
        // Menentukan awal dan akhir hari ini
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));

        // Cari angka terakhir hari ini di database
        Integer maxQueue = orderRepository.findMaxQueueNumberToday(start, end);
        int nextQueue = (maxQueue == null) ? 1 : maxQueue + 1;

        // 3. Anti-Hack: Validasi harga asli dari DB
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal totalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        // 4. Build Order Master
        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName())
                .orderNumber("ZIRO-" + (int)(Math.random() * 90000 + 10000))
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("COMPLETED") // Status otomatis LUNAS
                .queueNumber(nextQueue) // Nomor urut otomatis
                .build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Barang tidak valid!");

            // Cek & Potong Stok
            if (dbItem.getStock() < item.getQty()) {
                throw new RuntimeException("Stok " + dbItem.getName() + " habis!");
            }
            dbItem.setStock(dbItem.getStock() - item.getQty());
            itemRepository.save(dbItem);

            // Hitung Subtotal berdasarkan harga DB
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
        order.setItems(orderItems); // Pastikan list items masuk ke object order

        // 5. Update Saldo Shift secara otomatis
        BigDecimal currentSales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        shift.setTotalSales(currentSales.add(totalServer));
        shiftRepository.save(shift);

        // Save order dan simpan hasilnya ke variabel untuk di-return
        OrderEntity savedOrder = orderRepository.save(order);

        return savedOrder;
    }
}