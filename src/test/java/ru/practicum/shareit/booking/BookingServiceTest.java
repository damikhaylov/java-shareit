package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private Item item;
    private User owner;
    private User booker;
    private Booking booking;
    private BookingDto bookingDto;

    private final LocalDateTime defaultStart = LocalDateTime.of(2023, 1, 1, 12, 0);
    private final LocalDateTime defaultEnd = LocalDateTime.of(2023, 2, 1, 12, 0);
    private final BookingStatus defaultBookingStatus = BookingStatus.WAITING;


    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);

        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);

        final Long defaultOwnerId = 1L;
        final Long defaultBookerId = 2L;
        final Long defaultItemId = 1L;
        final Long defaultBookingId = 1L;

        owner = new User(defaultOwnerId, "Owner User", "owner@mail.com");

        booker = new User(defaultBookerId, "Booker User", "booker@mail.com");

        item = new Item(defaultItemId, "Item", "Item description", true, owner, null);

        booking = new Booking(defaultBookingId, defaultStart, defaultEnd, item, booker, defaultBookingStatus);
        bookingDto = BookingMapper.toBookingDto(booking);
    }

    @Test
    void createBookingTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        final BookingInfoDto bookingInfoDtoResult = bookingService.createBooking(bookingDto, booker.getId());

        assertEquals(BookingMapper.toBookingInfoDto(booking), bookingInfoDtoResult);

        verify(userRepository, times(1)).findById(booker.getId());
        verify(itemRepository, times(1)).findById(booking.getItem().getId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void nonAvailableItemTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        item.setAvailable(false);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Exception exception = assertThrows(NonAvailableItemException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));

        assertTrue(exception.getMessage().contains(String.format("Вещь с id %d недоступна", item.getId())));
    }

    @Test
    void operationBookingItemByOwnerTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Exception exception = assertThrows(BookingItemByOwnerException.class,
                () -> bookingService.createBooking(bookingDto, owner.getId()));

        assertTrue(exception.getMessage().contains(
                String.format("Пользователь id %d не может бронировать принадлежащую ему вещь id %d",
                        owner.getId(), item.getId())));
    }

    @Test
    void operationUserNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));

        assertTrue(exception.getMessage().contains("Не найден пользователь с id " + booker.getId()));
    }

    @Test
    void operationNullUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, null));

        assertTrue(exception.getMessage().contains("Не передан id пользователя, бронирующего вещь"));
    }

    @Test
    void operationItemNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenReturn(booking);

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));

        assertTrue(exception.getMessage().contains("Не найдена вещь с id " + bookingDto.getItemId()));
    }

    @Test
    void operationNullItemTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(new BookingDto(), booker.getId()));

        assertTrue(exception.getMessage().contains("Не передан id бронируемой вещи"));
    }

    @Test
    void approveBookingTest() {
        Booking approvedBooking = new Booking(booking.getId(), booking.getStart(), booking.getEnd(), booking.getItem(),
                booking.getBooker(), BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(approvedBooking);

        final BookingInfoDto bookingInfoDtoResult = bookingService.approveBooking(booking.getItem().getOwner().getId(),
                booking.getId(), true);

        assertEquals(BookingMapper.toBookingInfoDto(approvedBooking), bookingInfoDtoResult);

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, times(1)).save(approvedBooking);
    }

    @Test
    void operationBookingForApproveNotFoundTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.approveBooking(booking.getItem().getOwner().getId(), booking.getId(),
                        true));

        assertTrue(exception.getMessage().contains("Не найдена запись о бронировании с id " + bookingDto.getId()));
    }

    @Test
    void operationNullApprovingParameterTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(booking.getItem().getOwner().getId(), booking.getId(),
                        null));

        assertTrue(exception.getMessage().contains("Неверный параметр подтверждения или отмены бронирования"));
    }

    @Test
    void operationApprovedStatusDeniedToChangeExceptionTest() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(ApprovedStatusDeniedToChangeException.class,
                () -> bookingService.approveBooking(booking.getItem().getOwner().getId(), booking.getId(),
                        true));

        assertTrue(exception.getMessage().contains(
                String.format("Бронирование id %d уже подтверждено", booking.getId())));
    }

    @Test
    void operationApproveBookingByIllegalUserTest() {
        Long userId = 100L;
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.approveBooking(userId, booking.getId(),
                        true));

        assertTrue(exception.getMessage().contains((String.format(
                "Пользователь id %d не является владельцем вещи и не может изменить её статус бронирования",
                userId))));
    }

    @Test
    void getBookingTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final BookingInfoDto bookingInfoDtoResult =
                bookingService.getBooking(booking.getBooker().getId(), booking.getId());

        assertEquals(BookingMapper.toBookingInfoDto(booking), bookingInfoDtoResult);

        verify(bookingRepository, times(1)).findById(booking.getId());
    }

    @Test
    void operationBookingNotFoundTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.getBooking(booking.getBooker().getId(), booking.getId()));

        assertTrue(exception.getMessage().contains("Не найдена запись о бронировании с id " + bookingDto.getId()));
    }

    @Test
    void operationGetBookingByIllegalUserTest() {
        Long userId = 100L;
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> bookingService.getBooking(userId, booking.getId()));

        assertTrue(exception.getMessage().contains((String.format(
                "Пользователь id %d не является владельцем вещи или забронировавшем вещь " +
                        "и не может просматривать информацию о бронировании",
                userId))));
    }

    @ParameterizedTest
    @MethodSource("bookingStateValues")
    void getBookingsTest(BookingState state) {
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
        final Page<Booking> bookings = new PageImpl<>(Collections.singletonList(booking));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerOrderByStartDesc(any(), any()))
                .thenReturn(bookings);

        final List<BookingInfoDto> bookingInfoDtos
                = bookingService.getBookings(booking.getBooker().getId(), state, pageRequest);

        assertNotNull(bookingInfoDtos);
        assertEquals(1, bookingInfoDtos.size());
        assertEquals(BookingMapper.toBookingInfoDto(booking), bookingInfoDtos.get(0));

        verify(userRepository, times(1)).findById(booking.getBooker().getId());

        switch (state) {
            case WAITING:
                verify(bookingRepository, times(1))
                        .findByBookerAndStatusOrderByStartDesc(
                                booking.getBooker(), BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                verify(bookingRepository, times(1))
                        .findByBookerAndStatusOrderByStartDesc(
                                booking.getBooker(), BookingStatus.REJECTED, pageRequest);
                break;
            case CURRENT:
                verify(bookingRepository, times(1))
                        .findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
                break;
            case PAST:
                verify(bookingRepository, times(1))
                        .findByBookerAndEndBeforeOrderByStartDesc(any(), any(), any());
                break;
            case FUTURE:
                verify(bookingRepository, times(1))
                        .findByBookerAndStartAfterOrderByStartDesc(any(), any(), any());
                break;
            default:
                verify(bookingRepository, times(1))
                        .findByBookerOrderByStartDesc(booking.getBooker(), pageRequest);
                break;
        }
    }

    @ParameterizedTest
    @MethodSource("bookingStateValues")
    void getBookingsForOwnerTest(BookingState state) {
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
        final Page<Booking> bookings = new PageImpl<>(Collections.singletonList(booking));

        List<Item> items = Collections.singletonList(item);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerOrderByIdAsc(any())).thenReturn(items);

        when(bookingRepository.findByItemInAndStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemInAndEndBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemInAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemInOrderByStartDesc(any(), any()))
                .thenReturn(bookings);

        final List<BookingInfoDto> bookingInfoDtos
                = bookingService.getBookingsForOwner(owner.getId(), state, pageRequest);

        assertNotNull(bookingInfoDtos);
        assertEquals(1, bookingInfoDtos.size());
        assertEquals(BookingMapper.toBookingInfoDto(booking), bookingInfoDtos.get(0));

        verify(userRepository, times(1)).findById(owner.getId());
        verify(itemRepository, times(1)).findByOwnerOrderByIdAsc(owner);

        switch (state) {
            case WAITING:
                verify(bookingRepository, times(1))
                        .findByItemInAndStatusOrderByStartDesc(
                                items, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                verify(bookingRepository, times(1))
                        .findByItemInAndStatusOrderByStartDesc(
                                items, BookingStatus.REJECTED, pageRequest);
                break;
            case CURRENT:
                verify(bookingRepository, times(1))
                        .findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
                break;
            case PAST:
                verify(bookingRepository, times(1))
                        .findByItemInAndEndBeforeOrderByStartDesc(any(), any(), any());
                break;
            case FUTURE:
                verify(bookingRepository, times(1))
                        .findByItemInAndStartAfterOrderByStartDesc(any(), any(), any());
                break;
            default:
                verify(bookingRepository, times(1))
                        .findByItemInOrderByStartDesc(items, pageRequest);
                break;
        }
    }

    private static Stream<BookingState> bookingStateValues() {
        return Stream.of(BookingState.values());
    }
}
