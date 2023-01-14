package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GatewayErrorHandler {
    @ExceptionHandler
    public ResponseEntity<?> handleThrowableArgumentException(final MethodArgumentNotValidException exception) {
        log.warn("Ошибка валидации аргументов запроса: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка валидации аргументов запроса", exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleThrowableHeaderException(final MissingRequestHeaderException exception) {
        log.warn("Ошибка параметра в заголовке запроса: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка параметра в заголовке запроса", Objects.requireNonNull(exception.getMessage())),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleValidationException(final UnsupportedStatusException exception) {
        log.warn("Ошибка статуса бронирования: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("error", exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleThrowable(final Exception exception) {
        log.warn("Internal server error: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Internal server error", exception.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
