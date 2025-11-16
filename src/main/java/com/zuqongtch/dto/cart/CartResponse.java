
// ============= CartResponse.java =============
package com.zuqongtch.dto.cart;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private Integer totalItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long menuItemId;
        private String menuItemName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal itemTotal;
        private String specialInstructions;
        private String imageUrl;
    }
}