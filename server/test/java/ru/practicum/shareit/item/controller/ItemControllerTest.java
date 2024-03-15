package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.dto.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.dto.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @Mock
    private CommentDtoMapper commentDtoMapper;
    @InjectMocks
    private ItemController itemController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mvc;

    private User user;
    private Item item;
    private ItemDtoFromOrToUser itemDtoFromOrToUser;
    private ItemDtoWithComments expectedItemDtoWithComments;
    private CommentDto commentDto;
    private final Long requesterId = 1L;
    private Comment comment;
    private CommentDtoFromUser commentDtoFromUser;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemController).build();

        user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        item = new Item(1L, "имя", "описание", true, user, null);
        itemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", true, null);
        comment = new Comment(1L, "комментарий", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        commentDto = new CommentDto(1L, "комментарий", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        Booking lastBooking = new Booking(1L, LocalDateTime.of(2020, 10, 20, 10, 10, 10), LocalDateTime.of(2021, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        Booking nextBooking = new Booking(1L, LocalDateTime.of(2050, 10, 20, 10, 10, 10), LocalDateTime.of(2051, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        BookingDto lastBookingDto = new BookingDto(1L, lastBooking.getStart(), lastBooking.getEnd(), item, 1L);
        BookingDto nextBookingDto = new BookingDto(1L, nextBooking.getStart(), nextBooking.getEnd(), item, 1L);
        expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, lastBookingDto, nextBookingDto, List.of(commentDto));
        commentDtoFromUser = new CommentDtoFromUser("комментарий");
    }

    @Test
    void addTestTest_whenItemDtoValid_thenReturnItem() throws Exception {
        when(itemService.add(requesterId, itemDtoFromOrToUser)).thenReturn(item);
        when(itemDtoMapper.toDto(item)).thenReturn(itemDtoFromOrToUser);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requesterId)
                        .content(objectMapper.writeValueAsString(itemDtoFromOrToUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoFromOrToUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoFromOrToUser.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoFromOrToUser.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoFromOrToUser.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoFromOrToUser.getRequestId())));
    }

    @Test
    void updateItemTest_whenItemDtoValid_thenReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(requesterId, item.getId(), item)).thenReturn(item);
        when(itemDtoMapper.toItem(itemDtoFromOrToUser)).thenReturn(item);
        when(itemDtoMapper.toDto(item)).thenReturn(itemDtoFromOrToUser);

        mvc.perform(patch("/items/{itemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requesterId)
                        .content(objectMapper.writeValueAsString(itemDtoFromOrToUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoFromOrToUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoFromOrToUser.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoFromOrToUser.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoFromOrToUser.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoFromOrToUser.getRequestId())));
    }

    @Test
    void getByIdTest_whenItemExists_thenReturnItem() throws Exception {
        when(itemService.getWithBookingsById(requesterId, item.getId())).thenReturn(expectedItemDtoWithComments);

        mvc.perform(get("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedItemDtoWithComments.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(expectedItemDtoWithComments.getName())))
                .andExpect(jsonPath("$.description", is(expectedItemDtoWithComments.getDescription())))
                .andExpect(jsonPath("$.lastBooking.start", is(expectedItemDtoWithComments.getLastBooking().getStart().toString())))
                .andExpect(jsonPath("$.lastBooking.end", is(expectedItemDtoWithComments.getLastBooking().getEnd().toString())))
                .andExpect(jsonPath("$.lastBooking.item.id", is(expectedItemDtoWithComments.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(expectedItemDtoWithComments.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.start", is(expectedItemDtoWithComments.getNextBooking().getStart().toString())))
                .andExpect(jsonPath("$.nextBooking.end", is(expectedItemDtoWithComments.getNextBooking().getEnd().toString())))
                .andExpect(jsonPath("$.nextBooking.item.id", is(expectedItemDtoWithComments.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(expectedItemDtoWithComments.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(expectedItemDtoWithComments.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(expectedItemDtoWithComments.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(expectedItemDtoWithComments.getComments().get(0).getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))));
    }

    @Test
    void getAllForUserTest_whenItemExists_thenReturnListOfItem() throws Exception {
        int from = 0;
        int size = 1;
        when(itemService.getAllForUserPageable(requesterId, from / size, size)).thenReturn(List.of(expectedItemDtoWithComments));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(expectedItemDtoWithComments.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(expectedItemDtoWithComments.getName())))
                .andExpect(jsonPath("$[0].description", is(expectedItemDtoWithComments.getDescription())))
                .andExpect(jsonPath("$[0].available", is(expectedItemDtoWithComments.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.start", is(expectedItemDtoWithComments.getLastBooking().getStart().toString())))
                .andExpect(jsonPath("$[0].lastBooking.end", is(expectedItemDtoWithComments.getLastBooking().getEnd().toString())))
                .andExpect(jsonPath("$[0].lastBooking.item.id", is(expectedItemDtoWithComments.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(expectedItemDtoWithComments.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.start", is(expectedItemDtoWithComments.getNextBooking().getStart().toString())))
                .andExpect(jsonPath("$[0].nextBooking.end", is(expectedItemDtoWithComments.getNextBooking().getEnd().toString())))
                .andExpect(jsonPath("$[0].nextBooking.item.id", is(expectedItemDtoWithComments.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(expectedItemDtoWithComments.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(expectedItemDtoWithComments.getComments().get(0).getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(expectedItemDtoWithComments.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created", is(expectedItemDtoWithComments.getComments().get(0).getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))));
    }

    @Test
    void searchTest_whenSearchForItemExists_thenReturnItem() throws Exception {
        int from = 1;
        int size = 1;
        String text = "описание";
        when(itemService.search(requesterId, text, from / size, size)).thenReturn(List.of(item));
        when(itemDtoMapper.toDto(item)).thenReturn(itemDtoFromOrToUser);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "1")
                        .param("size", "1")
                        .param("text", "описание"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoFromOrToUser.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoFromOrToUser.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoFromOrToUser.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoFromOrToUser.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoFromOrToUser.getRequestId())));

    }

    @Test
    void addCommentTest_whenCommentDtoValid_thenReturnComment() throws Exception {
        String text = "описание";
        when(itemService.addComment(anyLong(), anyLong(), any(Comment.class))).thenReturn(comment);
        when(commentDtoMapper.toCommentDto(comment)).thenReturn(commentDto);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        mvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header("X-Sharer-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoFromUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().format(dateTimeFormatter))));

    }
}