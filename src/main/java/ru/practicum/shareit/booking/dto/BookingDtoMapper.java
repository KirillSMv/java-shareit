package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingDtoMapper {

    public static Booking toBooking(BookingDtoIncoming bookingDtoIncoming, User user, Item item) {
        Booking booking = new Booking();
        booking.setStart(bookingDtoIncoming.getStart());
        booking.setEnd(bookingDtoIncoming.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        return booking;
    }

    public static BookingDto toBookingDtoWithBookerId(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(booking.getItem());
        bookingDto.setBookerId(booking.getBooker().getId());
        return bookingDto;
    }
}