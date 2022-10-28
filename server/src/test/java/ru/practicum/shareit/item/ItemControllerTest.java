package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.MyPageRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final Long defaultItemId = 1L;
    private final Long defaultUserId = 1L;
    private final ItemDto itemDto =
            new ItemDto(1L, "Item", "Item description", true, 2L);

    private final ItemInfoDto itemInfoDto =
            new ItemInfoDto(1L, "Item", "Item description", true,
                    null, null, null);

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(any(), anyLong())).thenReturn(itemDto);

        mvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));

        verify(itemService, times(1)).createItem(itemDto, defaultUserId);
    }

    @Test
    void createCommentTest() throws Exception {
        final CommentDto commentDto = new CommentDto("comment");
        final CommentInfoDto commentInfoDto =
                new CommentInfoDto(1L, commentDto.getText(), defaultItemId, defaultUserId,
                        "Author Name", LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        when(itemService.createComment(anyLong(), any(), anyLong())).thenReturn(commentInfoDto);

        mvc.perform(MockMvcRequestBuilders.post("/items/" + defaultItemId + "/comment")
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentInfoDto.getText())))
                .andExpect(jsonPath("$.itemId", is(commentInfoDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(commentInfoDto.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.authorName", is(commentInfoDto.getAuthorName())));

        verify(itemService, times(1)).createComment(defaultItemId, commentDto, defaultUserId);
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(anyLong(), any(), anyLong())).thenReturn(itemDto);

        mvc.perform(MockMvcRequestBuilders.patch("/items/" + defaultItemId)
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));

        verify(itemService, times(1)).updateItem(defaultItemId, itemDto, defaultUserId);
    }

    @Test
    void deleteItemTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/items/" + defaultItemId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(defaultItemId);
    }

    @Test
    void getItemTest() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemInfoDto);

        mvc.perform(MockMvcRequestBuilders.get("/items/" + defaultItemId)
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemInfoDto.getName())))
                .andExpect(jsonPath("$.description", is(itemInfoDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemInfoDto.getAvailable())));

        verify(itemService, times(1)).getItem(defaultItemId, defaultUserId);
    }

    @Test
    void getAllItemsTest() throws Exception {
        when(itemService.getAll(anyLong(), any())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/items")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1))
                .getAll(defaultUserId, new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void searchItemsTest() throws Exception {
        final String searchText = "Sample text";

        when(itemService.searchItem(anyString(), any())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/items/search?text=" + searchText))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1))
                .searchItem(searchText, new MyPageRequest(0, 10, Sort.unsorted()));
    }
}
