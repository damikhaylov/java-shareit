package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requester, LocalDateTime created) {
        return new ItemRequest(
                null,
                itemRequestDto.getDescription(),
                requester,
                created
        );
    }

    public static ItemRequestInfoDto toItemRequestInfoDto(ItemRequest itemRequest,
                                                          List<Item> items) {
        return new ItemRequestInfoDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                (items != null)
                        ? items.stream().map(ItemRequestMapper::toItemRequestInfoDtoItem).collect(Collectors.toList())
                        : null
        );
    }

    public static ItemRequestInfoDto.Item toItemRequestInfoDtoItem(Item item) {
        return new ItemRequestInfoDto.Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                (item.getRequest() != null) ? item.getRequest().getId() : null
        );
    }
}
