package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoToUser;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDtoWithIdOnly;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingDtoMapper {

    public static Booking toBooking(BookingDtoFromUser bookingDtoFromUser, User user, Item item) {
        Booking booking = new Booking();
        booking.setStart(bookingDtoFromUser.getStart());
        booking.setEnd(bookingDtoFromUser.getEnd());
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

    public static BookingDtoToUser toBookingDtoToUser(Booking booking) {
        BookingDtoToUser bookingDtoToUser = new BookingDtoToUser();
        bookingDtoToUser.setId(booking.getId());
        bookingDtoToUser.setStart(booking.getStart());
        bookingDtoToUser.setEnd(booking.getEnd());
        bookingDtoToUser.setStatus(booking.getStatus());

        //конструирование DTO для item
        Item item = booking.getItem();
        ItemDtoToUser itemDtoToUser = new ItemDtoToUser();
        itemDtoToUser.setId(item.getId());
        itemDtoToUser.setName(item.getName());
        itemDtoToUser.setDescription(item.getDescription());
        itemDtoToUser.setAvailable(item.getAvailable());

        User owner = booking.getItem().getOwner();
        UserDtoWithIdOnly itemOwnerWithIdOnly = new UserDtoWithIdOnly(owner.getId());
        itemDtoToUser.setOwner(itemOwnerWithIdOnly);

        //конструирование DTO для Booker
        UserDtoWithIdOnly bookerDtoWithIdOnly = new UserDtoWithIdOnly(booking.getBooker().getId());

        bookingDtoToUser.setItem(itemDtoToUser);
        bookingDtoToUser.setBooker(bookerDtoWithIdOnly);

        return bookingDtoToUser;
    }

    public static List<BookingDtoToUser> toBookingDtoToUserList(List<Booking> bookings) {
        List<BookingDtoToUser> bookingDtoToUserList = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDtoToUserList.add(toBookingDtoToUser(booking));
        }
        return bookingDtoToUserList;
    }
}