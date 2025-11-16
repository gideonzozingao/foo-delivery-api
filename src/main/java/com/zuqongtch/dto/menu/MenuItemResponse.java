package com.zuqongtch.dto.menu;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName;
    private Long categoryId;
    private Boolean available;
    private Boolean vegetarian;
    private Boolean vegan;
    private Integer preparationTime;
}
