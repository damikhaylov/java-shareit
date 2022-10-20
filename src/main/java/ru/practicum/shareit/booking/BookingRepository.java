package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerOrderByStartDesc(User booker, Pageable pageable);

    Page<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status, Pageable pageable);

    Page<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end,
                                                                        Pageable pageable);

    Page<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start, Pageable pageable);

    Page<Booking> findByItemInOrderByStartDesc(List<Item> items, Pageable pageable);

    Page<Booking> findByItemInAndStatusOrderByStartDesc(List<Item> items, BookingStatus status, Pageable pageable);

    Page<Booking> findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(List<Item> items,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end,
                                                                        Pageable pageable);

    Page<Booking> findByItemInAndEndBeforeOrderByStartDesc(List<Item> items, LocalDateTime end, Pageable pageable);

    Page<Booking> findByItemInAndStartAfterOrderByStartDesc(List<Item> items, LocalDateTime start, Pageable pageable);

    Booking findFirstByItemAndStartBeforeOrderByStartDesc(Item item, LocalDateTime moment);

    Booking findFirstByItemAndStartAfterOrderByStartAsc(Item item, LocalDateTime moment);

    boolean existsBookingByBookerAndEndBefore(User booker, LocalDateTime moment);
}
