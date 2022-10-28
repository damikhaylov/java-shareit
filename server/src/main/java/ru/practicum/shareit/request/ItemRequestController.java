package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping()
    ItemRequestInfoDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(itemRequestDto, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getItemRequest(@PathVariable Long requestId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getItemRequest(requestId, userId);
    }

    @GetMapping()
    public List<ItemRequestInfoDto> getAllForUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "from", defaultValue = "0")
                                           Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemRequestService.getAll(userId, new MyPageRequest(from, size, Sort.unsorted()));
    }
}
