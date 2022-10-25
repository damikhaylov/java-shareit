package ru.practicum.shareit.user;

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
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final UserDto userDto = new UserDto(1L, "Test User", "user@mail.com");

    @Test
    void createUserTest() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).createUser(userDto);
    }

    @Test
    void createUserTestWithBlancParameters() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(mapper.writeValueAsString(new UserDto(null, "", "")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserTest() throws Exception {
        when(userService.updateUser(anyLong(), any())).thenReturn(userDto);

        mvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).updateUser(1L, userDto);
    }

    @Test
    void deleteUserTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getUserTest() throws Exception {
        when(userService.getUser(anyLong())).thenReturn(userDto);

        mvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).getUser(anyLong());
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userService.getAll(new MyPageRequest(0, 10, Sort.unsorted())))
                .thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).getAll(new MyPageRequest(0, 10, Sort.unsorted()));
    }
}
