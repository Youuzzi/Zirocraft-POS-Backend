package com.zirocraft.billingsoftware.io;

import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponse {
    private String itemId;
    private String name;
    private BigDecimal price;
    private String categoryId;
    private String description;
    private String categoryName;
    private String imgUrl;
    private Integer stock;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}