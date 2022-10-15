package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end);

    List<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end);

    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start);

    List<Booking> findByItemInOrderByStartDesc(List<Item> items);

    List<Booking> findByItemInAndStatusOrderByStartDesc(List<Item> items, BookingStatus status);

    List<Booking> findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(List<Item> items,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end);

    List<Booking> findByItemInAndEndBeforeOrderByStartDesc(List<Item> items, LocalDateTime end);

    List<Booking> findByItemInAndStartAfterOrderByStartDesc(List<Item> items, LocalDateTime start);

    Booking findFirstByItemAndStartBeforeOrderByStartDesc(Item item, LocalDateTime moment);

    Booking findFirstByItemAndStartAfterOrderByStartAsc(Item item, LocalDateTime moment);

    boolean existsBookingByBookerAndEndBefore(User booker, LocalDateTime moment);
}
