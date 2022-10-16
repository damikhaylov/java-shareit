package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInfoDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    CommentInfoDto createComment(Long itemId, CommentDto commentDto, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    void deleteItem(Long id);

    ItemInfoDto getItem(Long id, Long userId);

    List<ItemInfoDto> getAll(Long userId);

    List<ItemDto> searchItem(String text);
}
