package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;


public interface BookingService {

    Booking add(Booking booking);

    Booking getById(long userId, long id);

    Booking processBooking(long userId, long bookingId, boolean approved);

    List<Booking> getBookingsForUser(long userId, BookingState bookingState, int page, int size);

    List<Booking> getAllBookingsForUserItems(long userId, BookingState bookingState, int page, int size);

    Booking getLastOrNextBooking(Item item, boolean isLast);

    boolean checkIfUserRentedItem(User user, Item item);

    List<Booking> findAllLastAndNextBookingsForItems(List<Item> items, int page, int size);

    //List<Booking> findAllNextBookingsForItems(List<Item> items, int page, int size);
}
