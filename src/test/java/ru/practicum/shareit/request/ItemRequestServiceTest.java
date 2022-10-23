package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemRequestServiceTest {

    private ItemRequestService itemRequestService;
    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;
    private User requester;
    private ItemRequest itemRequest;

    private final LocalDateTime defaultCreated = LocalDateTime.of(2022, 1, 1, 12, 0);
    private final String defaultDescription = "description";

    @BeforeEach
    void beforeEach() {
        ItemRepository itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);

        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, itemRepository, userRepository);

        final Long defaultRequesterId = 1L;
        final Long defaultRequestId = 1L;

        requester = new User(defaultRequesterId, "Requester User", "requester@mail.com");
        itemRequest = new ItemRequest(defaultRequestId, defaultDescription, requester, defaultCreated);
    }

    @Test
    void createItemRequestTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        final ItemRequestInfoDto itemRequestInfoDto
                = itemRequestService.createItemRequest(new ItemRequestDto(defaultDescription), requester.getId());

        assertEquals(ItemRequestMapper.toItemRequestInfoDto(itemRequest, null), itemRequestInfoDto);

        verify(userRepository, times(1)).findById(requester.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    void operationUserNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemRequestService.createItemRequest(new ItemRequestDto(defaultDescription), requester.getId()));

        assertTrue(exception.getMessage().contains("Не найден пользователь с id " + requester.getId()));
    }

    @Test
    void operationNullUserTest() {
        Exception exception = assertThrows(ValidationException.class,
                () -> itemRequestService.createItemRequest(new ItemRequestDto(defaultDescription), null));

        assertTrue(exception.getMessage().contains("Не передан id пользователя, бронирующего вещь"));
    }

    @Test
    void getItemRequestTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        final ItemRequestInfoDto itemRequestInfoDto =
                itemRequestService.getItemRequest(itemRequest.getId(), requester.getId());

        assertEquals(ItemRequestMapper.toItemRequestInfoDto(itemRequest, Collections.emptyList()), itemRequestInfoDto);

        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
    }

    @Test
    void operationItemRequestNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> itemRequestService.getItemRequest(itemRequest.getId(), requester.getId()));

        assertTrue(exception.getMessage().contains("Не найден запрос с id " + itemRequest.getId()));
    }

    @Test
    void getAllTest() {
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
        final Page<ItemRequest> requests = new PageImpl<>(Collections.singletonList(itemRequest));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterNotOrderByCreatedDesc(any(), any())).thenReturn(requests);

        final List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getAll(requester.getId(), pageRequest);

        assertNotNull(itemRequestInfoDtos);
        assertEquals(1, itemRequestInfoDtos.size());
        assertEquals(ItemRequestMapper.toItemRequestInfoDto(itemRequest, Collections.emptyList()),
                itemRequestInfoDtos.get(0));

        verify(userRepository, times(1)).findById(requester.getId());
        verify(itemRequestRepository, times(1))
                .findByRequesterNotOrderByCreatedDesc(requester, pageRequest);
    }

    @Test
    void getAllForUserTest() {
        final List<ItemRequest> requests = Collections.singletonList(itemRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterOrderByCreatedDesc(any())).thenReturn(requests);

        final List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getAllForUser(requester.getId());

        assertNotNull(itemRequestInfoDtos);
        assertEquals(1, itemRequestInfoDtos.size());
        assertEquals(ItemRequestMapper.toItemRequestInfoDto(itemRequest, Collections.emptyList()),
                itemRequestInfoDtos.get(0));

        verify(userRepository, times(1)).findById(requester.getId());
        verify(itemRequestRepository, times(1))
                .findByRequesterOrderByCreatedDesc(requester);
    }
}
