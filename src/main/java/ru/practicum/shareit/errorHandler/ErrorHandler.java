package ru.practicum.shareit.errorHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exceptions.AuthorizationException;
import ru.practicum.shareit.exceptions.BookingCannotBeProcessedException;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
        String stackTrace = getStackTraceAsString(e);
        log.error("Произошла ошибка: {}", stackTrace);
        return new ErrorResponse(e.getMessage(), "проверьте переданные параметры");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ConstraintViolationException e) {
        log.error("Ошибка валидации: {}, {}", e.getCause(), e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(final Throwable e) {
        String stackTrace = getStackTraceAsString(e);
        log.error("Произошла ошибка: {}", stackTrace);
        return new ErrorResponse("Произошла ошибка: ", e.getMessage());
    }

    private String getStackTraceAsString(Throwable e) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out2 = new PrintStream(outputStream, false, StandardCharsets.UTF_8);
        e.printStackTrace(out2);
        out2.close();
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
