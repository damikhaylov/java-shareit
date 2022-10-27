package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.CommentWithoutBookingException;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    private ItemService itemService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private ItemRequestRepository itemRequestRepository;
    private Item item;
    private User user;
    private ItemRequest request;
    private Booking lastBooking;
    private Booking nextBooking;

    private ItemInfoDto itemInfoDto;
    private final Long defaultItemId = 1L;
    private final Long defaultUserId = 1L;


    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);

        itemService = new ItemServiceImpl(itemRepository, userRepository,
                bookingRepository, commentRepository, itemRequestRepository);

        user = new User(defaultUserId, "Test User", "user@mail.com");
        request = new ItemRequest(1L, "description",
                new User(2L, "Requester", "requester@mail.com"),
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        item = new Item(defaultItemId, "Item", "Item description", true, user, request);
        lastBooking = new Booking(1L,
                LocalDateTime.of(2000, 1, 1, 12, 0),
                LocalDateTime.of(2000, 2, 1, 12, 0),
                item, user, BookingStatus.APPROVED);
        nextBooking = new Booking(2L,
                LocalDateTime.of(2050, 1, 1, 12, 0),
                LocalDateTime.of(2050, 2, 1, 12, 0),
                item, user, BookingStatus.APPROVED);
        itemInfoDto = ItemMapper.toItemInfoDto(item, lastBooking, nextBooking, Collections.emptyList());
    }

    @Test
    void createItemTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenReturn(item);

        final ItemDto itemDto = itemService.createItem(ItemMapper.toItemDto(item), defaultUserId);

        assertEquals(ItemMapper.toItemDto(item), itemDto);

        verify(userRepository, times(1)).findById(defaultUserId);
        verify(itemRequestRepository, times(1)).findById(request.getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void operationNullUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        Exception exception = assertThrows(ValidationException.class,
                () -> itemService.createItem(ItemMapper.toItemDto(item), null));

        assertTrue(exception.getMessage().contains("Не передан id владельца вещи"));
    }

    @Test
    void createCommentTest() {
        String commentContent = "comment";
        Comment comment = new Comment(1L, commentContent, item, user, LocalDateTime.now());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsBookingByBookerAndEndBefore(any(), any())).thenReturn(true);
        when(commentRepository.save(any())).thenReturn(comment);

        final CommentInfoDto commentInfoDto =
                itemService.createComment(defaultItemId, new CommentDto(commentContent), defaultUserId);

        assertEquals(CommentMapper.toCommentInfoDto(comment), commentInfoDto);

        verify(userRepository, times(1)).findById(defaultUserId);
        verify(itemRepository, times(1)).findById(defaultItemId);
        verify(bookingRepository, times(1)).existsBookingByBookerAndEndBefore(any(), any());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void operationCreatingCommentItemNotFoundTest() {
        Long userId = 100L;
        String commentContent = "comment";
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsBookingByBookerAndEndBefore(any(), any())).thenReturn(false);

        Exception exception = assertThrows(CommentWithoutBookingException.class,
                () -> itemService.createComment(defaultItemId, new CommentDto(commentContent), userId));

        assertTrue(exception.getMessage().contains(
                String.format("Пользователь id %d не может оставить комментарий по поводу вещи id %d",
                        userId, defaultItemId)));
    }

    @Test
    void operationCreatingCommentByIllegalUserTest() {
        String commentContent = "comment";
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemService.createComment(defaultItemId, new CommentDto(commentContent), defaultUserId));

        assertTrue(exception.getMessage().contains("Не найдена вещь с id " + item.getId()));
    }

    @ParameterizedTest
    @MethodSource("updateItemValues")
    void updateUserTest(String oldName, String newName, String resultName,
                        String oldDescription, String newDescription, String resultDescription,
                        Boolean oldAvailable, Boolean newAvailable, Boolean resultAvailable,
                        ItemRequest oldRequest, ItemRequest newRequest, ItemRequest resultRequest) {

        Item oldItem = new Item(defaultItemId, oldName, oldDescription, oldAvailable, user, oldRequest);
        Item newItem = new Item(defaultItemId, newName, newDescription, newAvailable, user, newRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn((newRequest != null) ? Optional.of(newRequest) : Optional.empty());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Item.class));

        final ItemDto itemDto = itemService.updateItem(defaultItemId, ItemMapper.toItemDto(newItem), defaultUserId);

        assertEquals(defaultItemId, itemDto.getId());
        assertEquals(resultName, itemDto.getName());
        assertEquals(resultDescription, itemDto.getDescription());
        assertEquals(resultAvailable, itemDto.getAvailable());
        assertEquals(resultRequest.getId(), itemDto.getRequestId());

        verify(userRepository, times(1)).findById(defaultUserId);
        verify(itemRepository, times(1)).findById(defaultItemId);
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void operationUpdatingItemItemNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemService.updateItem(defaultItemId, ItemMapper.toItemDto(item), defaultUserId));

        assertTrue(exception.getMessage().contains("Не найдена вещь с id " + item.getId()));
    }

    @Test
    void operationUpdatingByIllegalUserTest() {
        Long userId = 100L;
        Item updatingItem = new Item(item.getId(), item.getName(), item.getDescription(), item.getAvailable(),
                new User(userId, "Illegal User", "illegal@mail.com"), item.getRequest());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(updatingItem));

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemService.updateItem(defaultItemId, ItemMapper.toItemDto(updatingItem), defaultUserId));

        assertTrue(exception.getMessage().contains(
                String.format("Владелец вещи id %d, указанный при обновлении, не соответствует указанному при создании",
                        item.getId())));
    }

    @Test
    void operationUserNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemService.createItem(ItemMapper.toItemDto(item), defaultUserId));

        assertTrue(exception.getMessage().contains("Не найден пользователь с id " + defaultUserId));
    }

    @Test
    void deleteItemTest() {
        itemService.deleteItem(defaultItemId);
        verify(itemRepository, times(1)).deleteById(defaultItemId);
    }

    @Test
    void getItemTest() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemAndStartBeforeOrderByStartDesc(any(), any())).thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(any(), any())).thenReturn(nextBooking);
        when(commentRepository.findByItemOrderByCreatedDesc(any())).thenReturn(Collections.emptyList());

        final ItemInfoDto itemInfoDtoResult = itemService.getItem(defaultItemId, defaultUserId);

        assertEquals(itemInfoDto, itemInfoDtoResult);

        verify(itemRepository, times(1)).findById(defaultItemId);
        verify(bookingRepository, times(1))
                .findFirstByItemAndStartBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, times(1))
                .findFirstByItemAndStartAfterOrderByStartAsc(any(), any());
        verify(commentRepository, times(1)).findByItemOrderByCreatedDesc(item);
    }

    @Test
    void getItemNotByOwnerTest() {
        Long userId = 100L;
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemOrderByCreatedDesc(any())).thenReturn(Collections.emptyList());

        final ItemInfoDto itemInfoDtoResult = itemService.getItem(defaultItemId, userId);

        assertNull(itemInfoDtoResult.getLastBooking());
        assertNull(itemInfoDtoResult.getNextBooking());

        verify(itemRepository, times(1)).findById(defaultItemId);
        verify(commentRepository, times(1)).findByItemOrderByCreatedDesc(item);
    }

    @Test
    void getItemNotFoundTest() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemService.getItem(defaultItemId, defaultUserId));

        assertTrue(exception.getMessage().contains("Не найдена вещь с id " + defaultItemId));
    }

    @Test
    void getAllItemsTest() {
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
        final Page<Item> items = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.findByOwnerOrderByIdAsc(any(), any())).thenReturn(items);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findFirstByItemAndStartBeforeOrderByStartDesc(any(), any())).thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(any(), any())).thenReturn(nextBooking);
        when(commentRepository.findByItemOrderByCreatedDesc(any())).thenReturn(Collections.emptyList());

        final List<ItemInfoDto> itemDtos = itemService.getAll(defaultUserId, pageRequest);

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(itemInfoDto, itemDtos.get(0));

        verify(itemRepository, times(1)).findByOwnerOrderByIdAsc(user, pageRequest);
        verify(bookingRepository, times(1))
                .findFirstByItemAndStartBeforeOrderByStartDesc(any(), any());
        verify(bookingRepository, times(1))
                .findFirstByItemAndStartAfterOrderByStartAsc(any(), any());
        verify(commentRepository, times(1)).findByItemOrderByCreatedDesc(item);
    }

    @Test
    void searchItemTest() {
        final String searchText = "Sample text";
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
        final Page<Item> items = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.search(anyString(), any())).thenReturn(items);

        final List<ItemDto> itemDtos = itemService.searchItem(searchText, pageRequest);

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(ItemMapper.toItemDto(item), itemDtos.get(0));
        verify(itemRepository, times(1)).search(searchText, pageRequest);
    }

    @Test
    void searchItemByBlancStringTest() {
        final String searchText = "";
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());

        final List<ItemDto> itemDtos = itemService.searchItem(searchText, pageRequest);

        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    private static Stream<Arguments> updateItemValues() {
        User requester = new User(2L, "Requester", "requester@mail.com");
        ItemRequest oldRequest = new ItemRequest(1L, "description", requester,
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        ItemRequest newRequest = new ItemRequest(2L, "New description", requester,
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        return Stream.of(
                Arguments.of("Old Name", "New Name", "New Name",
                        "Old description", "New description", "New description",
                        false, true, true,
                        oldRequest, newRequest, newRequest),
                Arguments.of("Old Name", null, "Old Name",
                        "Old description", null, "Old description",
                        false, null, false,
                        oldRequest, null, oldRequest)
        );
    }
}
