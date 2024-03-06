package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.booking.dto.BookingDtoFromUser;
import ru.practicum.shareit.booking.dto.BookingDtoToUser;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDtoToUser;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDtoWithIdOnly;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private BookingDtoMapper bookingDtoMapper;
    @InjectMocks
    private BookingController bookingController;
    private ObjectMapper objectMapper;
    private MockMvc mvc;

    private final Long requesterId = 1L;
    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDtoToUser bookingDtoToUser;
    private ItemDtoToUser itemDtoToUser;
    private UserDtoWithIdOnly userDtoWithIdOnly;
    private UserDtoWithIdOnly ownerUserDtoWithIdOnly;
    private Booking bookingForAdding;
    private BookingDtoFromUser bookingDtoFromUser;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();


        user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        owner = new User(2L, "Ilya", "ilya@yandex.ru");
        userDtoWithIdOnly = new UserDtoWithIdOnly(1L);
        ownerUserDtoWithIdOnly = new UserDtoWithIdOnly(2L);
        item = new Item(1L, "имя", "описание", true, owner, null);

        booking = new Booking(1L, LocalDateTime.of(2025, 2, 20, 10, 10, 10),
                LocalDateTime.of(2026, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);

        bookingDtoFromUser = new BookingDtoFromUser();
        bookingDtoFromUser.setStart(LocalDateTime.of(2025, 2, 20, 10, 10, 10));
        bookingDtoFromUser.setEnd(LocalDateTime.of(2026, 2, 20, 11, 10, 10));
        bookingDtoFromUser.setItemId(item.getId());

        bookingForAdding = new Booking();
        bookingForAdding.setStart(LocalDateTime.of(2025, 2, 20, 10, 10, 10));
        bookingForAdding.setEnd(LocalDateTime.of(2026, 2, 20, 11, 10, 10));
        bookingForAdding.setItem(item);
        bookingForAdding.setBooker(user);

        itemDtoToUser = new ItemDtoToUser(1L, "имя", "описание", true, ownerUserDtoWithIdOnly);

        bookingDtoToUser = new BookingDtoToUser(1L, LocalDateTime.of(2025, 2, 20, 10, 10, 10),
                LocalDateTime.of(2026, 2, 20, 11, 10, 10),
                Status.WAITING, itemDtoToUser, userDtoWithIdOnly);
    }

    @SneakyThrows
    @Test
    void addTestTest_returnBookingDtoToUser() {
        Booking booking = new Booking(1L, bookingForAdding.getStart(), bookingForAdding.getEnd(),
                bookingForAdding.getItem(), bookingForAdding.getBooker(), Status.WAITING);

        when(userService.getById(requesterId)).thenReturn(user);
        when(itemService.getById(item.getId())).thenReturn(item);
        when(bookingDtoMapper.toBooking(bookingDtoFromUser, user, item)).thenReturn(bookingForAdding);
        when(bookingService.add(bookingForAdding)).thenReturn(booking);
        when(bookingDtoMapper.toBookingDtoToUser(booking)).thenReturn(bookingDtoToUser);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", requesterId)
                        .content(objectMapper.writeValueAsString(bookingDtoFromUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoToUser.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoToUser.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoToUser.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingDtoToUser.getStatus().toString())))
                .andExpect(jsonPath("$.item.name", is(bookingDtoToUser.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingDtoToUser.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingDtoToUser.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.owner.id", is(bookingDtoToUser.getItem().getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoToUser.getBooker().getId()), Long.class));
    }

    @SneakyThrows
    @Test
    void addTestTest_whenNotValidBooking_thenThrowNestedServletExceptionDueToValidationException() {
        BookingDtoFromUser notValidBookingDtoFromUser = new BookingDtoFromUser(1L, null, null, null);


        Assertions.assertThrows(
                NestedServletException.class,
                () -> mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notValidBookingDtoFromUser))
                        .header("X-Sharer-User-Id", requesterId)
                        .accept(MediaType.APPLICATION_JSON)));


        verify(bookingService, never()).add(booking);
    }

    @SneakyThrows
    @Test
    void processBookingTest_returnApprovedBooking() {
        bookingDtoToUser = new BookingDtoToUser(1L, booking.getStart(),
                booking.getEnd(), Status.APPROVED, itemDtoToUser, userDtoWithIdOnly);

        when(bookingService.processBooking((requesterId), 1L, true)).thenReturn(booking);
        when(bookingDtoMapper.toBookingDtoToUser(booking)).thenReturn(bookingDtoToUser);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", requesterId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())));
    }


    @SneakyThrows
    @Test
    void getBookingDetailsTest_returnBookingsWithDetails() {
        when(bookingService.getById(requesterId, 1L)).thenReturn(booking);
        when(bookingDtoMapper.toBookingDtoToUser(booking)).thenReturn(bookingDtoToUser);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(booking.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.owner.id", is(booking.getItem().getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @SneakyThrows
    @Test
    void getAllBookingsForUserTest_returnBookings() {
        int from = 0;
        int size = 1;
        when(bookingService.getBookingsForUser(requesterId, BookingState.ALL, from / size, size)).thenReturn(List.of(booking));
        when(bookingDtoMapper.toBookingDtoToUserList(List.of(booking))).thenReturn(List.of(bookingDtoToUser));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "0")
                        .param("size", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(booking.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.owner.id", is(booking.getItem().getOwner().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @SneakyThrows
    @Test
    void getAllBookingsForUserItemsTest_returnBookings() {
        int from = 0;
        int size = 1;
        when(bookingService.getAllBookingsForUserItems(owner.getId(), BookingState.ALL, from / size, size)).thenReturn(List.of(booking));
        when(bookingDtoMapper.toBookingDtoToUserList(List.of(booking))).thenReturn(List.of(bookingDtoToUser));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("from", "0")
                        .param("size", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().toString())))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(booking.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.owner.id", is(booking.getItem().getOwner().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class));
    }
}


