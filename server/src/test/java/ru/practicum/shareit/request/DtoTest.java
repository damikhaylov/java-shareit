package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJacksonTester;
    @Autowired
    private JacksonTester<ItemRequestInfoDto> itemRequestInfoDtoJacksonTester;

    private final LocalDateTime created = LocalDateTime.of(2022, 1, 1, 12, 0);

    @Test
    void testSerializeCommentDto() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto("description");
        JsonContent<ItemRequestDto> result = itemRequestDtoJacksonTester.write(itemRequestDto);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    void testSerializeItemRequestInfoDto() throws Exception {
        ItemRequestInfoDto itemRequestInfoDto =
                new ItemRequestInfoDto(1L, "description", created,
                        Collections.singletonList(new ItemRequestInfoDto.Item(1L, "Item",
                                        "Item description",true, 1L, 1L)));

        JsonContent<ItemRequestInfoDto> result = itemRequestInfoDtoJacksonTester.write(itemRequestInfoDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemRequestInfoDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestInfoDto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created")
                .contains(itemRequestInfoDto.getCreated().toString());

        assertThat(result).extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(itemRequestInfoDto.getItems().get(0).getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo(itemRequestInfoDto.getItems().get(0).getName());
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo(itemRequestInfoDto.getItems().get(0).getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId")
                .isEqualTo(itemRequestInfoDto.getItems().get(0).getOwnerId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId")
                .isEqualTo(itemRequestInfoDto.getItems().get(0).getRequestId().intValue());
    }
}
