package ru.practicum.shareit.exception;

public class NonAvailableItemException extends RuntimeException {

    public NonAvailableItemException(String message) {
        super(message);
    }
}
