package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.CommentWithoutBookingException;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository
    ) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = getOwnerById(userId);
        ItemRequest itemRequest = (itemDto.getRequestId() != null)
                ? itemRequestRepository.findById(itemDto.getRequestId()).orElse(null)
                : null;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = itemRepository.save(item);
        log.info("Информация о вещи id {} сохранена", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public CommentInfoDto createComment(Long itemId, CommentDto commentDto, Long userId) {
        User author = getOwnerById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NonExistentIdException("Не найдена вещь с id " + itemId));
        if (!bookingRepository.existsBookingByBookerAndEndBefore(author, LocalDateTime.now())) {
            throw new CommentWithoutBookingException(
                    String.format("Пользователь id %d не может оставить комментарий по поводу вещи id %d",
                            userId, itemId));
        }
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий id {} о вещи id {} сохранён", savedComment.getId(), itemId);
        return CommentMapper.toCommentInfoDto(savedComment);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        User owner = getOwnerById(userId);
        ItemRequest itemRequest = (itemDto.getRequestId() != null)
                ? itemRequestRepository.findById(itemDto.getRequestId()).orElse(null)
                : null;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);

        Item updatingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NonExistentIdException("Не найдена вещь с id " + itemId));
        if (!Objects.equals(item.getOwner().getId(), updatingItem.getOwner().getId())) {
            throw new NonExistentIdException(String.format(
                    "Владелец вещи id %d, указанный при обновлении, не соответствует указанному при создании", itemId));
        }
        String updatedName = (item.getName() != null) ? item.getName() : updatingItem.getName();
        String updatedDescription = (item.getDescription() != null)
                ? item.getDescription() : updatingItem.getDescription();
        Boolean updatedAvailable = (item.getAvailable() != null)
                ? item.getAvailable() : updatingItem.getAvailable();
        ItemRequest updatedItemRequest = (item.getRequest() != null)
                ? item.getRequest() : updatingItem.getRequest();

        Item updatedItem = itemRepository.save(new Item(itemId, updatedName, updatedDescription,
                updatedAvailable, owner, updatedItemRequest));
        log.info("Информация о вещи id {} обновлена", updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        log.info("Выполняется удаление вещи id {}", id);
        itemRepository.deleteById(id);
    }

    @Override
    public ItemInfoDto getItem(Long id, Long userId) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new NonExistentIdException("Не найдена вещь с id " + id));
        if (item.getOwner().getId().equals(userId)) {
            return getItemInfoDtoWithBookings(item);
        } else {
            return ItemMapper.toItemInfoDto(item, null, null,
                    commentRepository.findByItemOrderByCreatedDesc(item));
        }
    }

    @Override
    public List<ItemInfoDto> getAll(Long userId, PageRequest pageRequest) {
        User owner = getOwnerById(userId);
        Page<Item> items = itemRepository.findByOwnerOrderByIdAsc(owner, pageRequest);
        log.info("Сформирована постраничная выдача из списка всех вещей, " +
                        "принадлежащих пользователю id {} в количестве {} шт.",
                userId, items.getSize());
        return items.stream().map(this::getItemInfoDtoWithBookings).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text, PageRequest pageRequest) {
        if (text.isBlank()) {
            log.info("Поиск по пустой строке вернул пустой список вещей.");
            return new ArrayList<>();
        }
        Page<Item> items = itemRepository.search(text, pageRequest);
        log.info("Сформирована постраничная выдача из результатов поиска по строке '{}' в количестве {} шт.",
                text, items.getSize());
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private User getOwnerById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NonExistentIdException("Не найден пользователь с id " + userId));
    }

    private ItemInfoDto getItemInfoDtoWithBookings(Item item) {
        Booking lastBooking =
                bookingRepository.findFirstByItemAndStartBeforeOrderByStartDesc(item, LocalDateTime.now());
        Booking nextBooking =
                bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(item, LocalDateTime.now());
        List<Comment> comments = commentRepository.findByItemOrderByCreatedDesc(item);
        return ItemMapper.toItemInfoDto(item, lastBooking, nextBooking, comments);
    }
}
