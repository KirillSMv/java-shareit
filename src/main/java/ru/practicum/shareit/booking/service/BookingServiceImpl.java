package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
                    log.error("Бронирование с id {} не найдено", bookingId);
                    return new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId));
                }
        );
        checkIfNotOwnerOrBookerRequests(userId, booking);
        return booking;
    }

    @Transactional
    @Override
    public Booking processBooking(long userId, long bookingId, boolean approved) {
        checkIfUserExists(userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
                    log.error("Бронирование с id {} не найдено", bookingId);
                    return new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId));
                }
        );
        checkIfBookingStatusAllowsApproval(booking, approved);
        checkIfNotItemOwnerUpdates(userId, booking.getItem().getOwner().getId());
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return booking;
    }

    @Override
    public List<Booking> getBookingsForUser(long userId, BookingState bookingState, int page, int size) {
        User user = userService.getById(userId);
        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByBookerOrderByStartDesc(user, PageRequest.of(page, size));
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        PageRequest.of(page, size));
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(user,
                        LocalDateTime.now(),
                        PageRequest.of(page, size));
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(user,
                        LocalDateTime.now(),
                        PageRequest.of(page, size));
                break;
            case WAITING:
                Status waitingStatus = Status.valueOf(BookingState.WAITING.toString());
                bookings = bookingRepository.findAllByBookerAndStatus(user, waitingStatus, PageRequest.of(page, size));
                break;
            case REJECTED:
                Status rejectedStatus = Status.valueOf(BookingState.REJECTED.toString());
                bookings = bookingRepository.findAllByBookerAndStatus(user, rejectedStatus, PageRequest.of(page, size));
                break;
        }
        return bookings;
    }

    @Override
    public List<Booking> getAllBookingsForUserItems(long userId, BookingState bookingState, int page, int size) {
        User user = userService.getById(userId);
        List<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerOrderByStartDesc(user, PageRequest.of(page, size));
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(user,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        PageRequest.of(page, size));
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(),
                        PageRequest.of(page, size));
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(user, LocalDateTime.now(),
                        PageRequest.of(page, size));
                ;
                break;
            case WAITING:
                Status waitingStatus = Status.valueOf(BookingState.WAITING.name());
                bookings = bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(user, waitingStatus,
                        PageRequest.of(page, size));
                break;
            case REJECTED:
                Status rejectedStatus = Status.valueOf(BookingState.REJECTED.name());
                bookings = bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(user, rejectedStatus,
                        PageRequest.of(page, size));
                break;
        }
        return bookings;
    }

    @Override
    public Booking getLastOrNextBooking(Item item, boolean isLast) {
        Booking booking;
        if (isLast) {
            booking = bookingRepository.findFirstByItemAndStatusNotInAndStartBeforeOrderByStartDesc(item,
                    List.of(Status.REJECTED, Status.CANCELLED),
                    LocalDateTime.now());
        } else {
            booking = bookingRepository.findFirstByItemAndStatusNotInAndStartAfterOrderByStartAsc(item,
                    List.of(Status.REJECTED, Status.CANCELLED),
                    LocalDateTime.now());
        }
        return booking;
    }

    @Override
    public List<Booking> findAllLastAndNextBookingsForItems(List<Item> items, int page, int size) {
        return bookingRepository.findAllUnion(items, LocalDateTime.now(), PageRequest.of(page, size));
/*        return bookingRepository.findAllByItemInAndStatusNotInAndStartBeforeOrderByStartDesc(items,
                LocalDateTime.now(), PageRequest.of(page, size));*/
    }

/*    @Override
    public List<Booking> findAllNextBookingsForItems(List<Item> items, int page, int size) {
        return bookingRepository.findAllByItemInAndStatusNotInAndStartAfterOrderByStartAsc(items,
                LocalDateTime.now(), PageRequest.of(page, size));
    }*/

    @Override
    public boolean checkIfUserRentedItem(User user, Item item) {
        return bookingRepository.countByBookerAndItemAndEndBefore(user, item, LocalDateTime.now()) > 0;
    }

    private void checkIfBookingStatusAllowsApproval(Booking booking, boolean approved) {
        if (booking.getStatus() == Status.APPROVED && approved) {
            log.error("Статус брони уже 'Approved' и поэтому не может быть подтверждена");
            throw new IllegalArgumentException("Статус брони уже 'Approved' и не может быть подтверждена");
        }
        if (booking.getStatus() == Status.CANCELLED) {
            log.error("Статус брони 'CANCELLED' и поэтому не может быть подтверждена");
            throw new IllegalArgumentException("Статус брони 'CANCELLED' и не может быть подтверждена");
        }
    }

    private void checkIfNotOwnerOrBookerRequests(long userId, Booking booking) {
        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            log.error("Запрашивать данные бронирования может только владелец вещи или лицо, создавшее бронирование. " +
                    "userId = {}, bookerId = {}, ownerId = {}", userId, booking.getBooker().getId(), booking.getItem().getOwner().getId());
            throw new BookingCannotBeProcessedException(String.format("Запрашивать данные бронирования может только владелец вещи или лицо, создавшее бронирование. " +
                    "userId = %d, bookerId = %d, ownerId = %d", userId, booking.getBooker().getId(), booking.getItem().getOwner().getId()));
        }
    }

    private void checkIfNotItemOwnerUpdates(long userId, long ownerId) {
        if (userId != ownerId) {
            log.error("Обработать запрос на бронь может только владелец вещи. userId = {}, ownerId = {}",
                    userId, ownerId);
            throw new ObjectNotFoundException(String.format("Обработать запрос на бронь может только владелец вещи. " +
                    "userId = %d, ownerId = %d", userId, ownerId));
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
            log.error("На данные даты вещи уже забронирована: start = {}, end = {}", booking.getStart(), booking.getEnd());
            throw new ObjectAlreadyExistsException(String.format("На данные даты вещи уже забронирована: start = %s, end = %s",
                    booking.getStart(), booking.getEnd()));
        }
    }

    private void checkIfBookerNotOwner(Booking booking) {
        if (Objects.equals(booking.getItem().getOwner().getId(), booking.getBooker().getId())) {
            log.error("Владелец вещи не может ее забронировать. bookerId = {}, ownerId = {}",
                    booking.getBooker().getId(), booking.getItem().getOwner().getId());
            throw new BookingCannotBeProcessedException(String.format("Владелец вещи не может ее забронировать. bookerId = %d, ownerId = %d",
                    booking.getBooker().getId(), booking.getItem().getOwner().getId()));
        }
    }

    private void checkIfUserExists(long userId) {
        userService.getById(userId);
    }
}
