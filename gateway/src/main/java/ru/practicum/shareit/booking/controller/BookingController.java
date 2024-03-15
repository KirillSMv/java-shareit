package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoFromUser;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody @Valid BookingDtoFromUser bookingDtoFromUser,
                                      @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        return bookingClient.postBooking(userId, bookingDtoFromUser);
    }

    @PatchMapping(value = "/{bookingId}", params = "approved")
    public ResponseEntity<Object> processBooking(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                 @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId,
                                                 @RequestParam("approved") boolean approved) {
        return bookingClient.processBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingDetails(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                    @PathVariable("bookingId") @Positive(message = "id не может быть меньше 1") long bookingId) {
        return bookingClient.getBookingDetails(userId, bookingId);
    }


    @GetMapping
    public ResponseEntity<Object> getAllBookingsForUser(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String state,
                                                        @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                        @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return bookingClient.getAllBookingsForUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsForUserItems(@RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
                                                             @RequestParam(name = "state", defaultValue = "ALL") @NotBlank String state,
                                                             @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                             @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return bookingClient.getAllBookingsForUserItems(userId, state, from, size);
    }
}
