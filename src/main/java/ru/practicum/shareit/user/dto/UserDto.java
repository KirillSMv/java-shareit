package ru.practicum.shareit.user.dto;


import lombok.Data;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserDto {
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Имя не должно быть пустым")
    @Size(max = 40, message = "Слишком длинное имя пользователя (лимит: {max} символов)",
            groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotNull(groups = OnCreate.class, message = "Адрес почты не должен быть пустым")
    @Email(message = "Некорректный формат электронной почты", groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 100, message = "Слишком длинный email (лимит: {max} символов)",
            groups = {OnCreate.class, OnUpdate.class})
    private String email;
}
