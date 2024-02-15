package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class ItemDto {
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Имя не должно быть пустым")
    @Size(max = 40, message = "Слишком длинное название (лимит: {max} символов)",
            groups = {OnUpdate.class, OnCreate.class})
    private String name;

    @NotBlank(groups = OnCreate.class, message = "Описание не должно быть пустым")
    @Size(max = 200, message = "Слишком длинное описание (лимит: {max} символов)",
            groups = {OnUpdate.class, OnCreate.class})
    private String description;

    @NotNull(groups = OnCreate.class, message = "Статус не должен быть пустым")
    private Boolean available;
}
