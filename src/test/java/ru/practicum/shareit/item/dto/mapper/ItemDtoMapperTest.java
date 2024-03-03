package ru.practicum.shareit.item.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemDtoMapperTest {

    private ItemDtoMapper itemDtoMapper;

    @BeforeEach
    void setUp() {
        itemDtoMapper = new ItemDtoMapper();
    }

    @Test
    void toItemTest() {
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser(null, "имя", "описание", false, 1L);
        Item item = new Item(null, "имя", "описание", false, null, null);

        Item resultItem = itemDtoMapper.toItem(itemDtoFromOrToUser);

        assertEquals(item, resultItem);
    }

    @Test
    void toDto_whenRequestIsNull() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, user, null);
        ItemDtoFromOrToUser expectedItemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", false, null);

        ItemDtoFromOrToUser resultItemDto = itemDtoMapper.toDto(item);

        assertEquals(expectedItemDtoFromOrToUser, resultItemDto);
    }

    @Test
    void toDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", false, user, itemRequest);
        ItemDtoFromOrToUser expectedItemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", false, itemRequest.getId());

        ItemDtoFromOrToUser resultItemDto = itemDtoMapper.toDto(item);

        assertEquals(expectedItemDtoFromOrToUser, resultItemDto);
    }

    @Test
    void toItemDtoWithComments() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user,
                LocalDateTime.of(2024, 2, 20, 10, 10, 10));
        Item item = new Item(1L, "имя", "описание", false, owner, itemRequest);

        List<CommentDto> commentDtoList = List.of(new CommentDto(1L, "text", "Ilya",
                LocalDateTime.of(2024, 2, 20, 10, 10, 10)));

        BookingDto lastBookingDto = new BookingDto(1L,
                LocalDateTime.of(2023, 10, 20, 10, 10, 10),
                LocalDateTime.of(2023, 10, 20, 11, 10, 10),
                item, user.getId());

        BookingDto nextBookingDto = new BookingDto(1L,
                LocalDateTime.of(2024, 10, 20, 10, 10, 10),
                LocalDateTime.of(2024, 10, 20, 11, 10, 10),
                item, user.getId());

        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L,
                "имя",
                "описание",
                false,
                lastBookingDto,
                nextBookingDto,
                commentDtoList);

        ItemDtoWithComments resultItemDtoWithComments = itemDtoMapper.toItemDtoWithComments(item, commentDtoList, lastBookingDto, nextBookingDto);

        assertEquals(expectedItemDtoWithComments, resultItemDtoWithComments);
    }
}