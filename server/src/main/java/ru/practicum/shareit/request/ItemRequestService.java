package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestInfoDto createItemRequest(ItemRequestDto itemRequestDto, Long userId);

    ItemRequestInfoDto getItemRequest(Long id, Long userId);

    List<ItemRequestInfoDto> getAll(Long userId, PageRequest pageRequest);

    List<ItemRequestInfoDto> getAllForUser(Long userId);
}
