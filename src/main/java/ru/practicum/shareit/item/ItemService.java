package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    void deleteItem(Long id);

    ItemDto getItem(Long id);

    List<ItemDto> getAll(Long userId);

    List<ItemDto> searchItem(String text);
}
