package ru.practicum.shareit.request.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRequestMapperTest {

    private ItemRequestMapper itemRequestMapper;

    @BeforeEach
    void setUp() {
        itemRequestMapper = new ItemRequestMapper();
    }

    @Test
    void toItemRequest() {
        ItemRequestFromUserDto itemRequestFromUserDto = new ItemRequestFromUserDto("описание");
        ItemRequest expectedItemRequest = new ItemRequest();
        expectedItemRequest.setDescription("описание");

        ItemRequest resultItemRequest = itemRequestMapper.toItemRequest(itemRequestFromUserDto);

        assertEquals(expectedItemRequest, resultItemRequest);
    }

    @Test
    void toItemRequestForUserDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestToUserDto expectedItemRequestToUserDto = new ItemRequestToUserDto(1L, "описание", itemRequest.getCreated());

        ItemRequestToUserDto resultItemRequestToUserDto = itemRequestMapper.toItemRequestForUserDto(itemRequest);

        assertEquals(expectedItemRequestToUserDto, resultItemRequestToUserDto);
    }

    @Test
    void toItemRequestInfoDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        List<Item> itemsList = List.of(new Item(1L, "имя", "описание", true, owner, itemRequest));
        ItemRequestInfoDto.ItemInfoDto itemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), List.of(itemInfoDto));

        ItemRequestInfoDto resultItemRequestInfoDto = itemRequestMapper.toItemRequestInfoDto(itemRequest, itemsList);

        assertEquals(expectedItemRequestInfoDto, resultItemRequestInfoDto);
    }

    @Test
    void toItemRequestInfoDto_whenItemsListIsEmpty() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        List<Item> itemsList = Collections.emptyList();
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), Collections.emptyList());

        ItemRequestInfoDto resultItemRequestInfoDto = itemRequestMapper.toItemRequestInfoDto(itemRequest, itemsList);

        assertEquals(expectedItemRequestInfoDto, resultItemRequestInfoDto);
    }


    @Test
    void toItemInfoDtoTest() {
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, owner, itemRequest);
        ItemRequestInfoDto.ItemInfoDto expectedItemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);

        ItemRequestInfoDto.ItemInfoDto resultItemInfoDto = itemRequestMapper.toItemInfoDto(item);

        assertEquals(expectedItemInfoDto, resultItemInfoDto);
    }
}