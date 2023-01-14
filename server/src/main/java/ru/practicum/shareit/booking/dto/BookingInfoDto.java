package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BookingInfoDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    Item item;
    User booker;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class User {
        private Long id;
        private String name;
        private String email;
    }
}
