package ru.practicum.shareit.item;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommentInfoDto {
    Long id;
    String text;
    Long itemId;
    Long authorId;
    String authorName;
    LocalDateTime created;
}
