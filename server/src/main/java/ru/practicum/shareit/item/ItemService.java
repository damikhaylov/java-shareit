package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    CommentInfoDto createComment(Long itemId, CommentDto commentDto, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    void deleteItem(Long id);

    ItemInfoDto getItem(Long id, Long userId);

    List<ItemInfoDto> getAll(Long userId, PageRequest pageRequest);

    List<ItemDto> searchItem(String text, PageRequest pageRequest);
}
