package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DtoTest {

    @Autowired
    private JacksonTester<ItemDto> itemDtoJacksonTester;
    @Autowired
    private JacksonTester<ItemInfoDto> itemInfoDtoJacksonTester;
    @Autowired
    private JacksonTester<CommentDto> commentDtoJacksonTester;
    @Autowired
    private JacksonTester<CommentInfoDto> commentInfoDtoJacksonTester;

    @Test
    void testSerializeItemDto() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Item", "Item description", true, 1L);
        JsonContent<ItemDto> result = itemDtoJacksonTester.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(itemDto.getRequestId().intValue());
    }

    @Test
    void testSerializeItemInfoDto() throws Exception {
        LocalDateTime lastBookingStart = LocalDateTime.of(2000, 1, 1, 12, 0);
        LocalDateTime lastBookingEnd = LocalDateTime.of(2000, 2, 1, 12, 0);
        LocalDateTime nextBookingStart = LocalDateTime.of(2050, 1, 1, 12, 0);
        LocalDateTime nextBookingEnd = LocalDateTime.of(2050, 2, 1, 12, 0);
        ItemInfoDto.BookingDto lastBookingDto = new ItemInfoDto.BookingDto(1L, lastBookingStart, lastBookingEnd,
                1L, 1L, BookingStatus.APPROVED);
        ItemInfoDto.BookingDto nextBookingDto = new ItemInfoDto.BookingDto(2L, nextBookingStart, nextBookingEnd,
                1L, 2L, BookingStatus.APPROVED);
        ItemInfoDto itemInfoDto = new ItemInfoDto(1L, "Item", "Item description", true,
                lastBookingDto, nextBookingDto, Collections.emptyList());

        JsonContent<ItemInfoDto> result = itemInfoDtoJacksonTester.write(itemInfoDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemInfoDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemInfoDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemInfoDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(itemInfoDto.getLastBooking().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .contains(itemInfoDto.getLastBooking().getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end")
                .contains(itemInfoDto.getLastBooking().getEnd().toString());
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.itemId")
                .isEqualTo(itemInfoDto.getLastBooking().getItemId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId")
                .isEqualTo(itemInfoDto.getLastBooking().getBookerId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.status")
                .isEqualTo(itemInfoDto.getLastBooking().getStatus().toString());

        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id")
                .isEqualTo(itemInfoDto.getNextBooking().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start")
                .contains(itemInfoDto.getNextBooking().getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end")
                .contains(itemInfoDto.getNextBooking().getEnd().toString());
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.itemId")
                .isEqualTo(itemInfoDto.getNextBooking().getItemId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId")
                .isEqualTo(itemInfoDto.getNextBooking().getBookerId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.status")
                .isEqualTo(itemInfoDto.getNextBooking().getStatus().toString());

        assertThat(result).extractingJsonPathArrayValue("$.comments").size().isEqualTo(0);
    }

    @Test
    void testSerializeCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto("comment");
        JsonContent<CommentDto> result = commentDtoJacksonTester.write(commentDto);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(commentDto.getText());
    }

    @Test
    void testSerializeCommentInfoDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2022, 1, 1, 12, 0);
        CommentInfoDto commentInfoDto = new CommentInfoDto(1L, "comment", 1L, 1L,
                "Author Name", created);
        JsonContent<CommentInfoDto> result = commentInfoDtoJacksonTester.write(commentInfoDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(commentInfoDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(commentInfoDto.getText());
        assertThat(result).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(commentInfoDto.getItemId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.authorId")
                .isEqualTo(commentInfoDto.getAuthorId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.authorName")
                .isEqualTo(commentInfoDto.getAuthorName());
        assertThat(result).extractingJsonPathStringValue("$.created")
                .contains(commentInfoDto.getCreated().toString());
    }
}
