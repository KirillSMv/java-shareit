package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.BookingCannotBeProcessedException;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    private static final long requesterUserId = 1L;
    private static final long bookingId = 1L;


    @Test
    void addTest_whenItemCannotBeBooked_thenThrowIllegalArgumentException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, user, null);

        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.add(booking));
        assertEquals("Данная вещь с id 1 не доступна для аренды", exception.getMessage());
    }

    @Test
    void addTest_whenItemAlreadyBookedForThisTime_thenObjectAlreadyExistsException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);

        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        when(bookingRepository.findIfBookingTimeCrossed(booking.getItem(), booking.getStart(), booking.getEnd())).thenReturn(Optional.of(booking));
        ObjectAlreadyExistsException exception = assertThrows(ObjectAlreadyExistsException.class, () ->
                bookingService.add(booking));
        assertEquals("На данные даты вещи уже забронирована: start = 2025-10-20T10:10:10, end = 2025-10-20T11:10:10", exception.getMessage());
    }

    @Test
    void addTest_whenOwnerBooks_thenThrowBookingCannotBeProcessedException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);

        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);

        BookingCannotBeProcessedException exception = assertThrows(BookingCannotBeProcessedException.class, () ->
                bookingService.add(booking));
        assertEquals("Владелец вещи не может ее забронировать. bookerId = 1, ownerId = 1", exception.getMessage());
    }

    @Test
    void addTest_whenNotOwnerBooksAvailableItem_thenReturnBooking() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        when(bookingRepository.save(expectedBooking)).thenReturn(expectedBooking);

        Booking resultBooking = bookingService.add(expectedBooking);

        assertEquals(expectedBooking, resultBooking);
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();
        assertEquals(expectedBooking, savedBooking);
    }

    @Test
    void getByIdTest_whenNoUserFound_thenThrowObjectNotFoundException() {
        when(userService.getById(requesterUserId)).thenThrow(new ObjectNotFoundException("Пользователь с id 1 не найден"));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                bookingService.getById(requesterUserId, bookingId));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getByIdTest_whenBookingNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                bookingService.getById(requesterUserId, bookingId));
        assertEquals("Бронирование с id 1 не найдено", exception.getMessage());
    }

    @Test
    void getByIdTest_whenNotOwnerOrBookerRequests_thenThrowBookingCannotBeProcessedException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        User requesterForBooking = new User(3L, "Igor", "igor@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        when(userService.getById(requesterForBooking.getId())).thenReturn(requesterForBooking);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingCannotBeProcessedException exception = assertThrows(BookingCannotBeProcessedException.class, () ->
                bookingService.getById(requesterForBooking.getId(), bookingId));
        assertEquals("Запрашивать данные бронирования может только владелец вещи или лицо, создавшее бронирование. " +
                "userId = 3, bookerId = 1, ownerId = 2", exception.getMessage());
    }

    @Test
    void getByIdTest_whenOwnerRequests_thenReturnBooking() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        when(userService.getById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(expectedBooking));

        Booking resultBooking = bookingService.getById(owner.getId(), bookingId);
        assertEquals(expectedBooking, resultBooking);
    }

    @Test
    void processBookingTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        when(userService.getById(requesterUserId)).thenThrow(new ObjectNotFoundException("Пользователь с id 1 не найден"));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                bookingService.getById(requesterUserId, bookingId));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void processBookingTest_whenBookingNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                bookingService.processBooking(requesterUserId, bookingId, true));
        assertEquals("Бронирование с id 1 не найдено", exception.getMessage());
    }

    @Test
    void processBookingTest_whenBookingStatusAlreadyApprovedAndApprovalRequested_thenThrowIllegalArgumentException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.APPROVED);
        boolean isRequestForApproval = true;
        when(userService.getById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.processBooking(owner.getId(), bookingId, isRequestForApproval));

        assertEquals("Статус брони уже 'Approved' и не может быть подтверждена", exception.getMessage());
    }

    @Test
    void processBookingTest_whenBookingStatusIsCancelled_thenThrowIllegalArgumentException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.CANCELLED);
        boolean isRequestForApproval = true;
        when(userService.getById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.processBooking(owner.getId(), bookingId, isRequestForApproval));

        assertEquals("Статус брони 'CANCELLED' и не может быть подтверждена", exception.getMessage());
    }

    @Test
    void processBookingTest_whenNotOwnerTriesToProcessBooking_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        User requesterToProcessBooking = new User(3L, "Igor", "igor@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        boolean isRequestForApproval = true;
        when(userService.getById(requesterToProcessBooking.getId())).thenReturn(requesterToProcessBooking);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.processBooking(requesterToProcessBooking.getId(), bookingId, isRequestForApproval));

        assertEquals("Обработать запрос на бронь может только владелец вещи. userId = 3, ownerId = 2", exception.getMessage());
    }

    @Test
    void processBookingTest_whenApproved_thenReturnApprovedBooking() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        Booking expectedBookingAfterApproval = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.APPROVED);
        boolean isRequestForApproval = true;
        when(userService.getById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Booking resultBooking = bookingService.processBooking(owner.getId(), bookingId, isRequestForApproval);
        assertEquals(expectedBookingAfterApproval, resultBooking);
    }

    @Test
    void processBookingTest_whenRejected_thenReturnRejectedBooking() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        Booking expectedBookingAfterApproval = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.REJECTED);
        boolean isRequestForApproval = false;
        when(userService.getById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Booking resultBooking = bookingService.processBooking(owner.getId(), bookingId, isRequestForApproval);
        assertEquals(expectedBookingAfterApproval, resultBooking);
    }

    @Test
    void getBookingsForUserTest_whenNoUserFound_thenThrowObjectNotFoundException() {
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenThrow(ObjectNotFoundException.class);

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                bookingService.getBookingsForUser(requesterUserId, BookingState.ALL, page, size));
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsAll_thenReturnListOfAllBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerOrderByStartDesc(user, PageRequest.of(page, size))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.ALL, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsCurrent_thenReturnListOfCurrentBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.CURRENT, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsPast_thenReturnListOfPastBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.PAST, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsFuture_thenReturnListOfFutureBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.FUTURE, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsWaiting_thenReturnListOfBookingsWithWaitingStatus() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerAndStatus(any(User.class), any(Status.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.WAITING, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserTest_whenBookingStateIsRejected_thenReturnListOfBookingsWithRejectedStatus() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByBookerAndStatus(any(User.class), any(Status.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getBookingsForUser(requesterUserId, BookingState.REJECTED, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }


    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsAll_thenReturnListOfAllBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(user, PageRequest.of(page, size))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.ALL, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsCurrent_thenReturnListOfCurrentBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.CURRENT, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsPast_thenReturnListOfPastBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2022, 2, 20, 10, 10, 10),
                LocalDateTime.of(2023, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.PAST, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsFuture_thenReturnListOfFutureBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 2, 20, 10, 10, 10),
                LocalDateTime.of(2026, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(any(User.class), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.FUTURE, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsWaiting_thenReturnListOfBookingsWithWaitingStatus() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2026, 2, 20, 10, 10, 10),
                LocalDateTime.of(2027, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(any(User.class), any(Status.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.WAITING, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getBookingsForUserItemsTest_whenBookingStateIsRejected_thenReturnListOfBookingsWithRejectedStatus() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2024, 2, 20, 10, 10, 10),
                LocalDateTime.of(2025, 2, 20, 11, 10, 10),
                item, user, Status.REJECTED);
        int page = 0;
        int size = 1;
        when(userService.getById(requesterUserId)).thenReturn(user);
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(any(User.class), any(Status.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> bookings = bookingService.getAllBookingsForUserItems(requesterUserId, BookingState.REJECTED, page, size);

        assertEquals(List.of(expectedBooking), bookings);
    }

    @Test
    void getLastOrNextBookingTest_whenLastBookingRequested_thenReturnLastBookingForItem() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2023, 2, 20, 10, 10, 10),
                LocalDateTime.of(2024, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);

        when(bookingRepository.findFirstByItemAndStatusNotInAndStartBeforeOrderByStartDesc(any(Item.class), anyList(), any(LocalDateTime.class))).thenReturn(expectedBooking);

        Booking resultBooking = bookingService.getLastOrNextBooking(item, true);
        assertEquals(expectedBooking, resultBooking);
    }

    @Test
    void getLastOrNextBookingTest_whenNextBookingRequested_thenReturnNextBookingForItem() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 2, 20, 10, 10, 10),
                LocalDateTime.of(2026, 2, 20, 11, 10, 10),
                item, user, Status.WAITING);

        when(bookingRepository.findFirstByItemAndStatusNotInAndStartAfterOrderByStartAsc(any(Item.class), anyList(), any(LocalDateTime.class))).thenReturn(expectedBooking);

        Booking resultBooking = bookingService.getLastOrNextBooking(item, false);
        assertEquals(expectedBooking, resultBooking);
    }

    @Test
    void findAllLastAndNextBookingsForItemsTest_whenNoBookings_thenReturnEmptyList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        int page = 0;
        int size = 1;
        when(bookingRepository.findAllUnion(anyList(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(Collections.emptyList());

        List<Booking> resultList = bookingService.findAllLastAndNextBookingsForItems(List.of(item), page, size);

        assertTrue(resultList.isEmpty());
    }

    @Test
    void findAllLastAndNextBookingsForItemsTest_whenFound_thenReturnBookingsList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        Booking expectedBooking = new Booking(1L, LocalDateTime.of(2025, 10, 20, 10, 10, 10),
                LocalDateTime.of(2025, 10, 20, 11, 10, 10),
                item, user, Status.WAITING);
        int page = 0;
        int size = 1;
        when(bookingRepository.findAllUnion(anyList(), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(List.of(expectedBooking));

        List<Booking> resultList = bookingService.findAllLastAndNextBookingsForItems(List.of(item), page, size);

        assertEquals(List.of(expectedBooking), resultList);
    }

    @Test
    void checkIfUserRentedItemTest_whenRented_thenReturnTrue() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        when(bookingRepository.countByBookerAndItemAndEndBefore(any(User.class), any(Item.class), any(LocalDateTime.class))).thenReturn(2L);

        boolean result = bookingService.checkIfUserRentedItem(user, item);

        assertTrue(result);
    }

    @Test
    void checkIfUserRentedItemTest_whenNeverRented_thenReturnFalse() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        when(bookingRepository.countByBookerAndItemAndEndBefore(any(User.class), any(Item.class), any(LocalDateTime.class))).thenReturn(0L);

        boolean result = bookingService.checkIfUserRentedItem(user, item);

        assertFalse(result);
    }
}