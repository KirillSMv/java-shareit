package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Transactional
    public Booking add(Booking booking) {
        checkIfItemCanBeBooked(booking);
        checkIfBookerNotOwner(booking);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(long userId, long bookingId) {
        checkIfUserExists(userId);
        Booking booking = bookingRepository.findByIdJoinFetch(bookingId).orElseThrow(() -> new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        checkIfOwnerOrBookerRequests(userId, booking);
        return booking;
    }

    @Transactional
    @Override
    public Booking processBooking(long userId, long bookingId, boolean approved) {
        checkIfUserExists(userId);
        Booking booking = bookingRepository.findByIdJoinFetch(bookingId).orElseThrow(() -> new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        checkIfBookingStatusAllowsApproval(booking);
        checkIfItemOwnerUpdates(userId, booking.getItem().getOwner().getId());
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return booking;
    }

    @Override
    public List<Booking> getBookingsForUser(long userId, BookingState bookingState) {
        User user = userService.getById(userId);
        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByBooker(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentBookingsForUser(user);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastBookingsForUser(user);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureBookingsForUser(user);
                break;
            case WAITING:
                Status waitingStatus = Status.valueOf(BookingState.WAITING.toString());
                bookings = bookingRepository.findAllByBookerAndStatusEquals(user, waitingStatus);
                break;
            case REJECTED:
                Status rejectedStatus = Status.valueOf(BookingState.REJECTED.toString());
                bookings = bookingRepository.findAllByBookerAndStatusEquals(user, rejectedStatus);
                break;
        }
        return bookings;
    }

    @Override
    public List<Booking> getAllBookingsForUserItems(long userId, BookingState bookingState) {
        User user = userService.getById(userId);
        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllBookingsForUserItems(user);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentBookingsForUserItems(user);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastBookingsForUserItems(user);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureBookingsForUserItems(user);
                break;
            case WAITING:
                Status waitingStatus = Status.valueOf(BookingState.WAITING.name());
                bookings = bookingRepository.findAllBookingsForUserItemsAndStatusEquals(user, waitingStatus);
                break;
            case REJECTED:
                Status rejectedStatus = Status.valueOf(BookingState.REJECTED.name());
                bookings = bookingRepository.findAllBookingsForUserItemsAndStatusEquals(user, rejectedStatus);
                break;
        }
        return bookings;
    }

    @Override
    public Booking getLastOrNextBooking(Item item, boolean isLast) {
        if (isLast) {
            if (bookingRepository.findAllLastBookingsForItem(item).isEmpty()) {
                return null;
            } else {
                return bookingRepository.findAllLastBookingsForItem(item).get(0);
            }
        } else {
            if (bookingRepository.findAllNextBookingsForItem(item).isEmpty()) {
                return null;
            } else {
                return bookingRepository.findAllNextBookingsForItem(item).get(0);
            }
        }
    }

    @Override
    public List<Booking> findAllLastBookingsForItems(List<Item> items) {
        return bookingRepository.findAllLastBookingsForItems(items);
    }

    @Override
    public List<Booking> findAllNextBookingsForItems(List<Item> items) {
        return bookingRepository.findAllNextBookingsForItems(items);
    }

    @Override
    public Boolean checkIfUserRentedItem(User user, Item item) {
        return !bookingRepository.findAllForUserAndItemInThePast(user, item).isEmpty();
    }

    private void checkIfBookingStatusAllowsApproval(Booking booking) {
        if (booking.getStatus().equals(Status.APPROVED)) {
            log.error("Статус брони уже 'Approved' и не может быть подтверждена");
            throw new IllegalArgumentException("Статус брони уже 'Approved' и не может быть подтверждена");
        }
        if (booking.getStatus().equals(Status.CANCELLED)) {
            log.error("Статус брони 'CANCELLED' и не может быть подтверждена");
            throw new IllegalArgumentException("Статус брони 'CANCELLED' и не может быть подтверждена");
        }
    }

    private void checkIfOwnerOrBookerRequests(long userId, Booking booking) {
        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            log.error("Запрашивать данные бронирования может только владелец вещи или лицо, создавшее бронирование. " +
                    "userId = {}, bookerId = {}, ownerId = {}", userId, booking.getBooker().getId(), booking.getItem().getOwner().getId());
            throw new ObjectNotFoundException(String.format("Запрашивать данные бронирования может только владелец вещи или лицо, создавшее бронирование. " +
                    "userId = %d, bookerId = %d, ownerId = %d", userId, booking.getBooker().getId(), booking.getItem().getOwner().getId()));
        }
    }

    private void checkIfItemOwnerUpdates(long userId, long ownerId) {
        if (userId != ownerId) {
            log.error("Обработать запрос на бронь может только владелец вещи. userId = {}, bookerId = {}",
                    userId, ownerId);
            throw new ObjectNotFoundException("Обработать запрос на бронь может только владелец вещи");
        }
    }


    private void checkIfItemCanBeBooked(Booking booking) {
        if (!booking.getItem().getAvailable()) {
            log.error("Данная вещь с id {} не доступна для аренды", booking.getItem().getId());
            throw new IllegalArgumentException(String.format("Данная вещь с id %d не доступна для аренды",
                    booking.getItem().getId()));
        }
        checkIfItemNotBooked(booking);
    }

    private void checkIfItemNotBooked(Booking booking) {
        if (bookingRepository.findIfBookingTimeCrossed(booking.getItem(), booking.getStart(), booking.getEnd()).isPresent()) {
            log.error("На данные даты вещи уже забронирована");
            throw new ObjectAlreadyExistsException("На данные даты вещи уже забронирована");
        }
    }

    private void checkIfBookerNotOwner(Booking booking) {
        if (Objects.equals(booking.getItem().getOwner().getId(), booking.getBooker().getId())) {
            log.error("Владелец вещи не может ее забронировать");
            throw new ObjectNotFoundException("Владелец вещи не может ее забронировать");
        }
    }

    private void checkIfUserExists(long userId) {
        userService.getById(userId);
    }
}
