package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    BookingInfoDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Validated({Create.class}) @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingInfoDto updateBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingInfoDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingInfoDto> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                            Integer from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        try {
            BookingState state = BookingState.valueOf(stateParam);
            return bookingService.getBookings(userId, state, new MyPageRequest(from, size, Sort.unsorted()));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatusException("Unknown state: " + stateParam);
        }
    }

    @GetMapping("/owner")
    public List<BookingInfoDto> getBookingsForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL")
                                                    String stateParam,
                                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                    Integer from,
                                                    @Positive @RequestParam(name = "size", defaultValue = "10")
                                                    Integer size) {
        try {
            BookingState state = BookingState.valueOf(stateParam);
            return bookingService.getBookingsForOwner(userId, state, new MyPageRequest(from, size, Sort.unsorted()));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatusException("Unknown state: " + stateParam);
        }
    }
}
