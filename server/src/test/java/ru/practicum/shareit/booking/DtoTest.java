package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DtoTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJacksonTester;
    @Autowired
    private JacksonTester<BookingInfoDto> bookingInfoDtoJacksonTester;

    private final LocalDateTime defaultStart = LocalDateTime.of(2050, 1, 1, 12, 0);
    private final LocalDateTime defaultEnd = LocalDateTime.of(2050, 2, 1, 12, 0);
    private final BookingStatus defaultBookingStatus = BookingStatus.WAITING;

    @Test
    void testSerializeBookingDto() throws Exception {
        BookingDto bookingDto =
                new BookingDto(1L, defaultStart, defaultEnd, 1L, 1L, defaultBookingStatus);

        JsonContent<BookingDto> result = bookingDtoJacksonTester.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start")
                .contains(bookingDto.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .contains(bookingDto.getEnd().toString());
        assertThat(result).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(bookingDto.getItemId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.bookerId")
                .isEqualTo(bookingDto.getBookerId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().toString());
    }

    @Test
    void testSerializeBookingInfoDto() throws Exception {
        BookingInfoDto bookingInfoDto =
                new BookingInfoDto(1L, defaultStart, defaultEnd, defaultBookingStatus,
                        new BookingInfoDto.Item(1L, "Item", "Item description", true),
                        new BookingInfoDto.User(1L, "User Name", "user@mail.com"));

        JsonContent<BookingInfoDto> result = bookingInfoDtoJacksonTester.write(bookingInfoDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingInfoDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start")
                .contains(bookingInfoDto.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .contains(bookingInfoDto.getEnd().toString());
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingInfoDto.getStatus().toString());

        assertThat(result).extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(bookingInfoDto.getItem().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name")
                .isEqualTo(bookingInfoDto.getItem().getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description")
                .isEqualTo(bookingInfoDto.getItem().getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isTrue();

        assertThat(result).extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(bookingInfoDto.getBooker().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name")
                .isEqualTo(bookingInfoDto.getBooker().getName());
        assertThat(result).extractingJsonPathStringValue("$.booker.email")
                .isEqualTo(bookingInfoDto.getBooker().getEmail());
    }
}
