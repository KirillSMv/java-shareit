package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestToUserDto add(ItemRequestFromUserDto itemRequestFromUserDto, long userId);

    ItemRequestInfoDto getById(long id, long userId);

    List<ItemRequestInfoDto> getAllForUser(long userId);

    List<ItemRequestInfoDto> getAllFromOtherUsersPageable(long userId, int from, int size);
}
