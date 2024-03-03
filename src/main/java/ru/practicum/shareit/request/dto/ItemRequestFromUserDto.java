package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestFromUserDto {
    @NotBlank(message = "описание запроса не может быть пустым")
    private String description;
}
