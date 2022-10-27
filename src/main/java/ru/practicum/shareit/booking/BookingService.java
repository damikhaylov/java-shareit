package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import java.util.List;

public interface BookingService {

    BookingInfoDto createBooking(BookingDto bookingDto, Long userId);

    BookingInfoDto approveBooking(Long userId, Long bookingId, Boolean isApproved);

    BookingInfoDto getBooking(Long userId, Long bookingId);

    List<BookingInfoDto> getBookings(Long userId, BookingState state, PageRequest pageRequest);

    List<BookingInfoDto> getBookingsForOwner(Long userId, BookingState state, PageRequest pageRequest);
}
