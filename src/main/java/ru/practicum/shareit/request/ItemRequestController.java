package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    ItemRequestInfoDto createItemRequest(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Validated({Create.class}) @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(itemRequestDto, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getItemRequest(@Positive @PathVariable Long requestId,
                                      @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getItemRequest(requestId, userId);
    }

    @GetMapping()
    public List<ItemRequestInfoDto> getAllForUser(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getAll(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                           Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemRequestService.getAll(userId, new MyPageRequest(from, size, Sort.unsorted()));
    }
}
