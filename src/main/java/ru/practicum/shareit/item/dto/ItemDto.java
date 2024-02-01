package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class ItemDto {
    private Long id;
    @Size(max = 40, message = "Слишком длинное название (лимит: {max} символов)")
    private String name;
    @Size(max = 200, message = "Слишком длинное описание (лимит: {max} символов)")
    private String description;
    private Boolean available;
}
