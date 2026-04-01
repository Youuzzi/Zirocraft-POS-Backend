package com.zirocraft.billingsoftware.io;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalSales;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalUsers;
}