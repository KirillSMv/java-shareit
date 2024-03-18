package ru.practicum.shareit.exceptions;

public class BookingCannotBeProcessedException extends RuntimeException {

    public BookingCannotBeProcessedException(String message) {
        super(message);
    }
}
