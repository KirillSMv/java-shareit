package ru.practicum.shareit.exceptions;

public class NoEnumValueExistsException extends IllegalArgumentException {

    public NoEnumValueExistsException(String message) {
        super(message);
    }
}
