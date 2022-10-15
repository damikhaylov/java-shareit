package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@StartBeforeEnd(groups = {Create.class})
public class BookingDto {
    Long id;
    @FutureOrPresent(groups = {Create.class})
    LocalDateTime start;
    @FutureOrPresent(groups = {Create.class})
    LocalDateTime end;
    @Positive(groups = {Create.class})
    Long itemId;
    @Positive(groups = {Create.class})
    Long bookerId;
    BookingStatus status;
}
