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

    List<Booking> getBookingsForUser(long userId, BookingState bookingState);

    List<Booking> getAllBookingsForUserItems(long userId, BookingState bookingState);

    Booking getLastOrNextBooking(Item item, boolean isLast);

    Boolean checkIfUserRentedItem(User user, Item item);

    List<Booking> findAllLastBookingsForItems(List<Item> items);

    List<Booking> findAllNextBookingsForItems(List<Item> items);  //todo
}
