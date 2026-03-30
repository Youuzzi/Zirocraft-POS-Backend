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
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        // 1. HITUNG ANTREAN (Ambil Max + 1)
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(LocalTime.MAX));
        Integer lastQueue = orderRepository.findMaxQueueNumberToday(start, end);
        int nextQueue = (lastQueue == null) ? 1 : lastQueue + 1;

        // 2. VALIDASI HARGA & STOK
        Map<String, ItemEntity> dbItems = itemRepository.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getName, Function.identity()));

        BigDecimal totalServer = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        OrderEntity order = OrderEntity.builder()
                .customerName(request.getCustomerName())
                .orderNumber("ZIRO-" + (int)(Math.random() * 90000 + 10000))
                .tableNumber(request.getTableNumber())
                .paymentType(request.getPaymentType())
                .userId(email)
                .shiftId(shiftId)
                .status("COMPLETED") // <--- PAKSA STATUS LUNAS
                .queueNumber(nextQueue)
                .build();

        for (OrderRequest.CartItem item : request.getItems()) {
            ItemEntity dbItem = dbItems.get(item.getName());
            if (dbItem == null) throw new RuntimeException("Item tidak valid!");
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
        shift.setTotalSales(shift.getTotalSales().add(totalServer));
        shiftRepository.save(shift);

        return orderRepository.save(order);
    }
}