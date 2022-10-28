package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRequestInfoDto {
    Long id;
    String description;
    LocalDateTime created;
    List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long ownerId;
        private Long requestId;
    }
}
