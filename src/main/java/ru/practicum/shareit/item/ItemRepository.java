package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item save(Item item);

    Item update(Item item);

    boolean delete(Long id);

    Item findById(Long id);

    List<Item> getAll(Long userId);

    List<Item> searchItem(String text);
}
