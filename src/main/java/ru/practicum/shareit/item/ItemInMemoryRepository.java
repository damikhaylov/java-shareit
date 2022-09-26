package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ItemInMemoryRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Item save(Item item) {
        Long id = takeNextId();
        item.setId(id);
        items.put(item.getId(), item);
        return items.get(id);
    }

    @Override
    public Item update(Item item) {
        Long id = item.getId();
        items.replace(id, item);
        return items.get(id);
    }

    @Override
    public boolean delete(Long id) {
        return items.remove(id) != null;
    }

    @Override
    public Item findById(Long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getAll(Long userId) {
        return new ArrayList<>(items.values()).stream().filter(i -> i.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItem(String text) {
        return new ArrayList<>(items.values()).stream().filter(
                i -> (containsIgnoreCase(i.getName(), text) || containsIgnoreCase(i.getDescription(), text))
                        && i.getAvailable()
        ).collect(Collectors.toList());
    }

    private Long takeNextId() {
        return nextId++;
    }

    private boolean containsIgnoreCase(String baseString, String subString) {
        return baseString != null && !baseString.isEmpty() && !baseString.isBlank()
                && subString != null && !subString.isEmpty() && !subString.isBlank()
                && baseString.toLowerCase().contains(subString.toLowerCase());
    }
}