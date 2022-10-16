package ru.practicum.shareit.exception;

public class ApprovedStatusDeniedToChangeException extends RuntimeException {

    public ApprovedStatusDeniedToChangeException(String message) {
        super(message);
    }
}
