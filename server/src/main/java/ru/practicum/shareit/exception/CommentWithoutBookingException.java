package ru.practicum.shareit.exception;

public class CommentWithoutBookingException extends RuntimeException {

    public CommentWithoutBookingException(String message) {
        super(message);
    }
}
