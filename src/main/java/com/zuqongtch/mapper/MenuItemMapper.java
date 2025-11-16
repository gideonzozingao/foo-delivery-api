
package com.zuqongtch.mapper;

import com.zuqongtch.dto.menu.MenuItemRequest;
import com.zuqongtch.dto.menu.MenuItemResponse;
import com.zuqongtch.entity.Category;
import com.zuqongtch.entity.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItem toEntity(MenuItemRequest request, Category category) {
        return MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(category)
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .vegetarian(request.getVegetarian() != null ? request.getVegetarian() : false)
                .vegan(request.getVegan() != null ? request.getVegan() : false)
                .preparationTime(request.getPreparationTime())
                .build();
    }

    public MenuItemResponse toResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .imageUrl(menuItem.getImageUrl())
                .categoryName(menuItem.getCategory().getName())
                .categoryId(menuItem.getCategory().getId())
                .available(menuItem.getAvailable())
                .vegetarian(menuItem.getVegetarian())
                .vegan(menuItem.getVegan())
                .preparationTime(menuItem.getPreparationTime())
                .build();
    }

    public void updateEntity(MenuItem menuItem, MenuItemRequest request, Category category) {
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setCategory(category);
        menuItem.setAvailable(request.getAvailable());
        menuItem.setVegetarian(request.getVegetarian());
        menuItem.setVegan(request.getVegan());
        menuItem.setPreparationTime(request.getPreparationTime());
    }
}
