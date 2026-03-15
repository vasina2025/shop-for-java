package com.ishop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加购物车请求")
public class AddCartRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Min(value = 1, message = "数量至少为1")
    @Schema(description = "数量", example = "2")
    private Integer quantity = 1;
}
