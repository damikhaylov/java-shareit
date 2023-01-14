package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ServerErrorHandler {
    @ExceptionHandler
    public ResponseEntity<?> handleNonExistentIdException(final NonExistentIdException exception) {
        log.warn("Ошибка (объект не найден): {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка (объект не найден)", exception.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleValidationException(final NonAvailableItemException exception) {
        log.warn("Ошибка — вещь недоступна для бронирования: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка (вещь недоступна)", exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleValidationException(final ApprovedStatusDeniedToChangeException exception) {
        log.warn("Ошибка — невозможно изменить уже одобренный статус бронирования: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка (невозможно изменить уже одобренный статус бронирования)", exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleValidationException(final BookingItemByOwnerException exception) {
        log.warn("Ошибка — бронирование вещи её хозяином: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка (бронирование вещи её хозяином)", exception.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler
    public ResponseEntity<?> handleValidationException(final CommentWithoutBookingException exception) {
        log.warn("Ошибка — невозможно оставить комментарий, если не было бронирования: {}", exception.getMessage());
        return new ResponseEntity<>(
                Map.of("Ошибка (невозможно оставить комментарий, если не было бронирования)", exception.getMessage()),
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
