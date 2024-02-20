package ru.practicum.shareit.booking.enums;

import ru.practicum.shareit.exceptions.NoEnumValueExistsException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState convert(String stateParam) {
        BookingState bookingState;
        try {
            bookingState = valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NoEnumValueExistsException(String.format("Проверьте правильность переданных параметров: stateParam = %s", stateParam));
        }
        return bookingState;
    }
}


