package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoFromUser;
import ru.practicum.shareit.booking.dto.BookingDtoToUser;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingDtoMapper bookingDtoMapper;

    @PostMapping
    public BookingDtoToUser add(@RequestBody @Valid BookingDtoFromUser bookingDtoFromUser,
                                @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        User user = userService.getById(userId);
        Item item = itemService.getById(bookingDtoFromUser.getItemId());
        Booking booking = bookingService.add(bookingDtoMapper.toBooking(bookingDtoFromUser, user, item));
        return bookingDtoMapper.toBookingDtoToUser(booking);
    }

    @PatchMapping(value = "/{bookingId}", params = "approved")
    public BookingDtoToUser processBooking(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                           @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId,
                                           @RequestParam("approved") boolean approved) {
        Booking booking = bookingService.processBooking(userId, bookingId, approved);
        return bookingDtoMapper.toBookingDtoToUser(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoToUser getBookingDetails(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                              @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId) {

        Booking booking = bookingService.getById(userId, bookingId);
        return bookingDtoMapper.toBookingDtoToUser(booking);
    }


    @GetMapping
    public List<BookingDtoToUser> getAllBookingsForUser(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam,
                                                        @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                        @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        BookingState bookingState = BookingState.convert(stateParam.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        List<Booking> bookings = bookingService.getBookingsForUser(userId, bookingState, from / size, size);
        return bookingDtoMapper.toBookingDtoToUserList(bookings);
    }

    @GetMapping("/owner")
    public List<BookingDtoToUser> getAllBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                             @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam,
                                                             @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                             @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        BookingState bookingState = BookingState.convert(stateParam.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        List<Booking> bookings = bookingService.getAllBookingsForUserItems(userId, bookingState, from / size, size);
        return bookingDtoMapper.toBookingDtoToUserList(bookings);
    }
}
