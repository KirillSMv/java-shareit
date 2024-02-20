package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CommentDtoFromUser {
    @NotBlank(message = "Комментарий не может быть пустым")
    String text;
}
