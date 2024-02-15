package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserComment {
    @NotBlank(message = "Комментарий не может быть пустым")
    String text;
}
