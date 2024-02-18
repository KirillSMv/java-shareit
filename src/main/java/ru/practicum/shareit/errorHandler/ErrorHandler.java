package ru.practicum.shareit.errorHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.*;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final ObjectNotFoundException e) {
        log.error("Произошла ошибка: {}", e.getMessage());
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final BookingCannotBeProcessedException e) {
        log.error("Произошла ошибка: {}", e.getMessage());
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final IllegalArgumentException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final NoEnumValueExistsException e) {
        log.error("Unknown state: UNSUPPORTED_STATUS, {}", e.getMessage());
        return new ErrorResponse("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ConstraintViolationException e) {
        log.error("Ошибка валидации: {}, {}", e.getCause(), e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(final AuthorizationException e) {
        log.error("Произошла ошибка: {}", e.getMessage());
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(final ObjectAlreadyExistsException e) {
        log.error("Произошла ошибка: {}", e.getMessage());
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(final SQLException e) {
        log.error("Произошла ошибка: {}", e.getMessage());
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }
}
