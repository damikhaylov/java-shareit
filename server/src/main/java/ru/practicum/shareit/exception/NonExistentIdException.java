package ru.practicum.shareit.exception;

public class NonExistentIdException extends RuntimeException {

    public NonExistentIdException(String message) {
        super(message);
    }
}