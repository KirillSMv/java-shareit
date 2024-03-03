package ru.practicum.shareit.booking.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoFromUser;
import ru.practicum.shareit.booking.dto.BookingDtoToUser;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoToUser;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDtoWithIdOnly;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingDtoMapperTest {

    private BookingDtoMapper bookingDtoMapper;

    @BeforeEach
    void setUp() {
        bookingDtoMapper = new BookingDtoMapper();
    }

    @Test
    void toBooking() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, user, null);
        Booking resultBooking = new Booking(null,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        BookingDtoFromUser bookingDtoFromUser = new BookingDtoFromUser(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                1L);

        Booking booking = bookingDtoMapper.toBooking(bookingDtoFromUser, user, item);
        assertEquals(resultBooking, booking);
    }

    @Test
    void toBookingDtoWithBookerIdTest_whenBookingNull() {
        Booking booking = null;
        BookingDto bookingDto = bookingDtoMapper.toBookingDtoWithBookerId(booking);

        assertNull(bookingDto);
    }

    @Test
    void toBookingDtoWithBookerIdTest_whenBookingIsNotNull() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        BookingDto expectedBookingDto = new BookingDto(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user.getId());

        BookingDto resultBookingDto = bookingDtoMapper.toBookingDtoWithBookerId(booking);

        assertEquals(expectedBookingDto, resultBookingDto);
    }

    @Test
    void toBookingDtoToUserTest() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);

        UserDtoWithIdOnly userDtoWithIdOnly = new UserDtoWithIdOnly(owner.getId());
        ItemDtoToUser itemDtoToUser = new ItemDtoToUser(1L, "имя", "описание", false, userDtoWithIdOnly);
        UserDtoWithIdOnly bookerDtoWithIdOnly = new UserDtoWithIdOnly(user.getId());
        BookingDtoToUser expectedBookingDtoToUser = new BookingDtoToUser(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                Status.WAITING, itemDtoToUser, bookerDtoWithIdOnly);

        BookingDtoToUser resultBooking = bookingDtoMapper.toBookingDtoToUser(booking);

        assertEquals(expectedBookingDtoToUser, resultBooking);
    }

    @Test
    void toBookingDtoToUserListTest() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, owner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        UserDtoWithIdOnly userDtoWithIdOnly = new UserDtoWithIdOnly(owner.getId());
        ItemDtoToUser itemDtoToUser = new ItemDtoToUser(1L, "имя", "описание", false, userDtoWithIdOnly);
        UserDtoWithIdOnly bookerDtoWithIdOnly = new UserDtoWithIdOnly(user.getId());
        BookingDtoToUser expectedBookingDtoToUser = new BookingDtoToUser(1L,
                LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                Status.WAITING, itemDtoToUser, bookerDtoWithIdOnly);

        List<BookingDtoToUser> resultListOfBooking = bookingDtoMapper.toBookingDtoToUserList(List.of(booking));

        assertEquals(List.of(expectedBookingDtoToUser), resultListOfBooking);
    }
}