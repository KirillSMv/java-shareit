package ru.practicum.shareit.user.dto;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UserDto {
    private Long id;
    @Size(max = 40, message = "Слишком длинное имя пользователя (лимит: {max} символов)")
    private String name;
    @Email(regexp = "([A-Za-z0-9]{1,}[\\\\-]{0,1}[A-Za-z0-9]{1,}[\\\\.]{0,1}[A-Za-z0-9]{1,})+@"
            + "([A-Za-z0-9]{1,}[\\\\-]{0,1}[A-Za-z0-9]{1,}[\\\\.]{0,1}[A-Za-z0-9]{1,})+[\\\\.]{1}[a-z]{2,10}",
            message = "Некорректный адресс электронной почты: ${validatedValue}")
    @Size(max = 100, message = "Слишком длинный email (лимит: {max} символов)")
    private String email;

}
