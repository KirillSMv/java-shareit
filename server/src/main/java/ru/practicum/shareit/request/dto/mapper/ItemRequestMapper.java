package ru.practicum.shareit.request.dto.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemRequestMapper {
    public ItemRequest toItemRequest(ItemRequestFromUserDto itemRequestFromUserDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestFromUserDto.getDescription());
        return itemRequest;
    }

    public ItemRequestToUserDto toItemRequestForUserDto(ItemRequest itemRequest) {
        return new ItemRequestToUserDto(itemRequest.getId(), itemRequest.getDescription(), itemRequest.getCreated());
    }

    public ItemRequestInfoDto toItemRequestInfoDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestInfoDto itemRequestInfoDto = ItemRequestInfoDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();

        List<ItemRequestInfoDto.ItemInfoDto> itemInfoDtos = new ArrayList<>();
        if (!items.isEmpty()) {
            for (Item item : items) {
                itemInfoDtos.add(toItemInfoDto(item));
            }
        }
        return itemRequestInfoDto.toBuilder()
                .items(itemInfoDtos)
                .build();
    }

    public ItemRequestInfoDto.ItemInfoDto toItemInfoDto(Item item) {
        return new ItemRequestInfoDto.ItemInfoDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getRequest().getId(),
                item.getAvailable());
    }
}
