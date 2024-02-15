package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIncoming;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
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
    public Booking add(@Valid @RequestBody BookingDtoIncoming bookingDtoIncoming,
                       @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        checkIfBookingPeriodValid(bookingDtoIncoming);
        User user = userService.getById(userId);
        Item item = itemService.getById(bookingDtoIncoming.getItemId());
        return bookingService.add(BookingDtoMapper.toBooking(bookingDtoIncoming, user, item));
    }

    @PatchMapping(value = "/{bookingId}", params = "approved")
    public Booking processBooking(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                  @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId,
                                  @RequestParam("approved") boolean approved) {
        return bookingService.processBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingDetails(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                     @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId) {
        return bookingService.getById(userId, bookingId);
    }


    @GetMapping
    public List<Booking> getAllBookingForUser(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam) {
        BookingState bookingState = BookingState.valueOf(stateParam.toUpperCase());
        return bookingService.getBookingsForUser(userId, bookingState);

    }

    @GetMapping("/owner")
    public List<Booking> getAllBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String stateParam) {
        BookingState bookingState = BookingState.valueOf(stateParam.toUpperCase());
        return bookingService.getAllBookingsForUserItems(userId, bookingState);

    }

    private void checkIfBookingPeriodValid(BookingDtoIncoming bookingDtoIncoming) {
        if (bookingDtoIncoming.getEnd().isBefore(bookingDtoIncoming.getStart()) || bookingDtoIncoming.getEnd().isEqual(bookingDtoIncoming.getStart())) {
            log.error("Неверно указан срок аренды");
            throw new IllegalArgumentException("Неверно указан срок аренды");
        }
    }
}
