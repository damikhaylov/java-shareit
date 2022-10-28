package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;


import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping()
    public ResponseEntity<Object> createItemRequest(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @Validated({Create.class}) @RequestBody RequestRequestDto requestDto) {
        log.info("Creating Item Request {}", requestDto);
        return requestClient.createItemRequest(requestDto, userId);
    }


    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@Positive @PathVariable Long requestId,
                                                 @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get Item Request Id={}", requestId);
        return requestClient.getItemRequest(requestId, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllForUser(@Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all requests for user Id={}", userId);
        return requestClient.getAllForUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return requestClient.getAll(userId, from, size);
    }
}
