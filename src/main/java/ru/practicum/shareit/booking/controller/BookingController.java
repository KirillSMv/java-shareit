package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoFromUser;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingDtoToUser;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
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

    /*
    В этом контроллере у меня довольно много "сторонних" сервисов, задумка передавать в сервис по работе с бронированиями
    именно готовый объект, а не DTO. Наставник говорил, что так в целом правильнее. Что сервис призван работать скорее
     с самими бизнес сущностями. Но с другой стороны лишние зависимости, которые и так уже есть в сервисе...)
     */

    @PostMapping
    public BookingDtoToUser add(@Valid @RequestBody BookingDtoFromUser bookingDtoFromUser,
                                @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        checkIfBookingPeriodValid(bookingDtoFromUser);
        User user = userService.getById(userId);
        Item item = itemService.getById(bookingDtoFromUser.getItemId());
        Booking booking = bookingService.add(BookingDtoMapper.toBooking(bookingDtoFromUser, user, item));
        return BookingDtoMapper.toBookingDtoToUser(booking);
    }

    @PatchMapping(value = "/{bookingId}", params = "approved")
    public BookingDtoToUser processBooking(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                           @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId,
                                           @RequestParam("approved") boolean approved) {
        Booking booking = bookingService.processBooking(userId, bookingId, approved);
        return BookingDtoMapper.toBookingDtoToUser(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoToUser getBookingDetails(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                              @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId) {

        Booking booking = bookingService.getById(userId, bookingId);
        return BookingDtoMapper.toBookingDtoToUser(booking);
    }


    @GetMapping
    public List<BookingDtoToUser> getAllBookingsForUser(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam) {
        BookingState bookingState = BookingState.convert(stateParam.toUpperCase());
        List<Booking> bookings = bookingService.getBookingsForUser(userId, bookingState);
        return BookingDtoMapper.toBookingDtoToUserList(bookings);
    }

    @GetMapping("/owner")
    public List<BookingDtoToUser> getAllBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                             @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam) {
        BookingState bookingState = BookingState.convert(stateParam.toUpperCase());
        List<Booking> bookings = bookingService.getAllBookingsForUserItems(userId, bookingState);
        return BookingDtoMapper.toBookingDtoToUserList(bookings);
    }

    private void checkIfBookingPeriodValid(BookingDtoFromUser bookingDtoFromUser) {
        if (bookingDtoFromUser.getEnd().isBefore(bookingDtoFromUser.getStart()) || bookingDtoFromUser.getEnd().isEqual(bookingDtoFromUser.getStart())) {
            log.error("Неверно указан срок аренды");
            throw new IllegalArgumentException("Неверно указан срок аренды");
        }
    }
}
