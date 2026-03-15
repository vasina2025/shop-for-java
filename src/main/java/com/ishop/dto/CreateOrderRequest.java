package com.ishop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotEmpty(message = "订单商品不能为空")
    @Schema(description = "商品列表", example = "[{\"productId\": 1, \"quantity\": 2}]")
    private List<OrderItem> items;

    @NotBlank(message = "收货地址不能为空")
    @Schema(description = "收货地址", example = "北京市朝阳区xxx")
    private String address;

    @Data
    @Schema(description = "订单商品项")
    public static class OrderItem {
        @Schema(description = "商品ID", example = "1")
        private Long productId;
        @Schema(description = "数量", example = "2")
        private Integer quantity;
    }
}
