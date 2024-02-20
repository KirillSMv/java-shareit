package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDtoWithIdOnly;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoToUser {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private UserDtoWithIdOnly owner;
}
