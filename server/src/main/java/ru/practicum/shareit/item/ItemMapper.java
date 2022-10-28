package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static Item toItem(ItemDto itemDto, User owner, ItemRequest itemRequest) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                itemRequest
        );
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                (item.getRequest() != null) ? item.getRequest().getId() : null
        );
    }

    public static ItemInfoDto toItemInfoDto(Item item, Booking lastBooking, Booking nextBooking,
                                            List<Comment> comments) {
        return new ItemInfoDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                toBookingDto(lastBooking),
                toBookingDto(nextBooking),
                comments.stream().map(ItemMapper::toCommentInfoDto).collect(Collectors.toList())
        );
    }

    private static ItemInfoDto.BookingDto toBookingDto(Booking booking) {
        if (booking != null) {
            return new ItemInfoDto.BookingDto(
                    booking.getId(),
                    booking.getStart(),
                    booking.getEnd(),
                    booking.getItem().getId(),
                    booking.getBooker().getId(),
                    booking.getStatus()
            );
        } else {
            return null;
        }
    }

    private static ItemInfoDto.CommentInfoDto toCommentInfoDto(Comment comment) {
        return new ItemInfoDto.CommentInfoDto(
                comment.getId(),
                comment.getText(),
                comment.getItem().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
