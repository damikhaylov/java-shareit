package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class CommentMapper {
    public static Comment toComment(CommentDto commentDto, Item item, User author) {
        return new Comment(
                null,
                commentDto.getText(),
                item,
                author,
                null
        );
    }

    public static CommentInfoDto toCommentInfoDto(Comment comment) {
        return new CommentInfoDto(
                comment.getId(),
                comment.getText(),
                comment.getItem().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
