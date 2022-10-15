package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long userId);

    BookingInfoDto approveBooking(Long userId, Long bookingId, Boolean isApproved);

    BookingInfoDto getBooking(Long userId, Long bookingId);

    List<BookingInfoDto> getBookings(Long userId, BookingState state);

    List<BookingInfoDto> getBookingsForOwner(Long userId, BookingState state);
}
