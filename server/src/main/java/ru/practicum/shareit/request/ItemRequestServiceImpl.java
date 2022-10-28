package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  ItemRepository itemRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemRequestInfoDto createItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        User requester = getUserById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requester, LocalDateTime.now());
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Информация о запросе id {} сохранена", savedItemRequest.getId());
        return ItemRequestMapper.toItemRequestInfoDto(savedItemRequest, null);
    }

    @Override
    public ItemRequestInfoDto getItemRequest(Long id, Long userId) {
        getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(id).orElseThrow(
                () -> new NonExistentIdException("Не найден запрос с id " + id));
        return getItemRequestInfoDtoWithItems(itemRequest);
    }

    @Override
    public List<ItemRequestInfoDto> getAll(Long userId, PageRequest pageRequest) {
        User requester = getUserById(userId);
        Page<ItemRequest> requests = itemRequestRepository.findByRequesterNotOrderByCreatedDesc(requester, pageRequest);
        log.info("Сформирована постраничная выдача из перечня всех запросов в количестве {} шт.",
                requests.getSize());
        return requests.stream().map(this::getItemRequestInfoDtoWithItems).collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestInfoDto> getAllForUser(Long userId) {
        User requester = getUserById(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterOrderByCreatedDesc(requester);
        return requests.stream().map(this::getItemRequestInfoDtoWithItems).collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NonExistentIdException("Не найден пользователь с id " + userId));
    }

    private ItemRequestInfoDto getItemRequestInfoDtoWithItems(ItemRequest itemRequest) {
        List<Item> items = itemRepository.findByRequestOrderByIdAsc(itemRequest);
        return ItemRequestMapper.toItemRequestInfoDto(itemRequest, items);
    }
}
