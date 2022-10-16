package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingInfoDto createBooking(BookingDto bookingDto, Long userId) {
        Item item = getItemById(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new NonAvailableItemException(String.format("Вещь с id %d недоступна", item.getId()));
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new BookingItemByOwnerException(
                    String.format("Пользователь id %d не может бронировать принадлежащую ему вещь id %d",
                            userId, item.getId()));
        }
        User booker = getUserById(userId);
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Информация о бронировании id {} сохранена", savedBooking.getId());
        return BookingMapper.toBookingInfoDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingInfoDto approveBooking(Long userId, Long bookingId, Boolean isApproved) {
        if (isApproved == null) {
            throw new ValidationException("Неверный параметр подтверждения или отмены бронирования");
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NonExistentIdException("Не найдена запись о бронировании с id " + bookingId));
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ApprovedStatusDeniedToChangeException(String.format(
                    "Пользователь id %d не является владельцем вещи и не может изменить её статус бронирования",
                    userId));
        }
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new NonExistentIdException(String.format("Бронирование id %d уже подтверждено", bookingId));
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования id {} изменён на {}", savedBooking.getId(), savedBooking.getStatus());
        return BookingMapper.toBookingInfoDto(savedBooking);
    }

    @Override
    public BookingInfoDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NonExistentIdException("Не найдена запись о бронировании с id " + bookingId));
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new NonExistentIdException(String.format(
                    "Пользователь id %d не является владельцем вещи или забронировавшем вещь " +
                            "и не может просматривать информацию о бронировании",
                    userId));
        }
        return BookingMapper.toBookingInfoDto(booking);
    }

    @Override
    public List<BookingInfoDto> getBookings(Long userId, BookingState state) {
        final User booker = getUserById(userId);
        final List<Booking> bookings;

        switch (state) {
            case WAITING:
                bookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(
                        booker, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerAndStartAfterOrderByStartDesc(
                        booker, LocalDateTime.now());
                break;
            default:
                bookings = bookingRepository.findByBookerOrderByStartDesc(booker);
                break;
        }

        return bookings.stream().map(BookingMapper::toBookingInfoDto).collect(Collectors.toList());
    }

    public List<BookingInfoDto> getBookingsForOwner(Long userId, BookingState state) {
        final User owner = getUserById(userId);
        final List<Item> items = itemRepository.findByOwnerOrderByIdAsc(owner);
        final List<Booking> bookings;

        switch (state) {
            case WAITING:
                bookings = bookingRepository.findByItemInAndStatusOrderByStartDesc(items, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemInAndStatusOrderByStartDesc(items, BookingStatus.REJECTED);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(
                        items, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findByItemInAndEndBeforeOrderByStartDesc(
                        items, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemInAndStartAfterOrderByStartDesc(
                        items, LocalDateTime.now());
                break;
            default:
                bookings = bookingRepository.findByItemInOrderByStartDesc(items);
                break;
        }

        return bookings.stream().map(BookingMapper::toBookingInfoDto).collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        if (userId == null) {
            throw new ValidationException("Не передан id пользователя, бронирующего вещь");
        }
        return userRepository.findById(userId).orElseThrow(
                () -> new NonExistentIdException("Не найден пользователь с id " + userId));
    }

    private Item getItemById(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Не передан id бронируемой вещи");
        }
        return itemRepository.findById(itemId).orElseThrow(
                () -> new NonExistentIdException("Не найдена вещь с id " + itemId));
    }
}
