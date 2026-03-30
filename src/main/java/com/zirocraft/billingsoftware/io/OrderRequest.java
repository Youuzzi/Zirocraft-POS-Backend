package com.zirocraft.billingsoftware.io;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private String tableNumber;
    private BigDecimal totalAmount;
    private String paymentType;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private String name;
        private BigDecimal price;
        private Integer qty;
    }
}