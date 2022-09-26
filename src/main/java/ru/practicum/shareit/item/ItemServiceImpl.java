package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = getOwnerById(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        log.info("Информация о вещи id {} сохранена", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        User owner = getOwnerById(userId);

        Item item = ItemMapper.toItem(itemDto, owner);
        Item updatingItem = itemRepository.findById(itemId);
        if (updatingItem == null) {
            throw new NonExistentIdException("Не найдена вещь с id " + itemId);
        }
        if (!Objects.equals(item.getOwner().getId(), updatingItem.getOwner().getId())) {
            throw new NonExistentIdException(String.format(
                    "Владелец вещи id %d, указанный при обновлении, не соответствует указанному при создании", itemId));
        }
        String updatedName = (item.getName() != null) ? item.getName() : updatingItem.getName();
        String updatedDescription = (item.getDescription() != null)
                ? item.getDescription() : updatingItem.getDescription();
        Boolean updatedAvailable = (item.getAvailable() != null)
                ? item.getAvailable() : updatingItem.getAvailable();

        Item updatedItem = itemRepository.update(new Item(itemId, updatedName, updatedDescription,
                updatedAvailable, owner, null));
        log.info("Информация о вещи id {} обновлена", updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }

    public void deleteItem(Long id) {
        if (itemRepository.delete(id)) {
            log.info("Информация о вещи id {} удалена", id);
        } else {
            throw new NonExistentIdException("Не найдена вещь с id " + id);
        }
    }

    public ItemDto getItem(Long id) {
        Item item = itemRepository.findById(id);
        if (item == null) {
            throw new NonExistentIdException("Не найдена вещь с id " + id);
        }
        log.info("Найдена информация о вещи id {}", id);
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getAll(Long userId) {
        List<Item> items = itemRepository.getAll(userId);
        log.info("Сформирован список вещей, принадлежащих пользователю id {} в количестве {} шт.", userId, items.size());
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> searchItem(String text) {
        List<Item> items = itemRepository.searchItem(text);
        log.info("Поиск по строке '{}' выдал список вещей в количестве {} шт.", text, items.size());
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private User getOwnerById(Long userId) {
        if (userId == null) {
            throw new ValidationException("Не передан id владельца вещи");
        }
        User owner = userRepository.findById(userId);
        if (owner == null) {
            throw new NonExistentIdException("Не найден пользователь с id " + userId);
        }
        return owner;
    }
}
