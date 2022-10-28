package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    private final Long defaultRequestId = 1L;
    private final Long defaultUserId = 1L;
    private final String defaultDescription = "description";
    private final LocalDateTime defaultCreated = LocalDateTime.of(2022, 1, 1, 12, 0);

    private final ItemRequestDto itemRequestDto = new ItemRequestDto(defaultDescription);
    private final ItemRequestInfoDto itemRequestInfoDto =
            new ItemRequestInfoDto(defaultRequestId, defaultDescription, defaultCreated, Collections.emptyList());

    @Test
    void createItemRequestTest() throws Exception {
        when(itemRequestService.createItemRequest(any(), anyLong())).thenReturn(itemRequestInfoDto);

        mvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestInfoDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        Matchers.containsString(itemRequestInfoDto.getCreated().toString())));

        verify(itemRequestService, times(1)).createItemRequest(itemRequestDto, defaultUserId);
    }

    @Test
    void getItemRequestTest() throws Exception {
        when(itemRequestService.getItemRequest(anyLong(), anyLong())).thenReturn(itemRequestInfoDto);

        mvc.perform(MockMvcRequestBuilders.get("/requests/" + defaultRequestId)
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestInfoDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        Matchers.containsString(itemRequestInfoDto.getCreated().toString())));

        verify(itemRequestService, times(1)).getItemRequest(defaultRequestId, defaultUserId);
    }

    @Test
    void getAllForUserTest() throws Exception {
        when(itemRequestService.getAllForUser(anyLong())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1)).getAllForUser(defaultUserId);
    }

    @Test
    void getAllTest() throws Exception {
        when(itemRequestService.getAll(anyLong(), any())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService, times(1))
                .getAll(defaultUserId, new MyPageRequest(0, 10, Sort.unsorted()));
    }
}
