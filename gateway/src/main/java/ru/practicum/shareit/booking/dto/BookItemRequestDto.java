package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@StartBeforeEnd
public class BookItemRequestDto {
	private Long itemId;
	@FutureOrPresent
	private LocalDateTime start;
	@Future
	private LocalDateTime end;
}
