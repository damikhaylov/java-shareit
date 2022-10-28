package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.exception.NonExistentIdException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
        user = new User(1L, "Test User", "user@mail.com");
    }

    @Test
    void createUserTest() {
        when(userRepository.save(user)).thenReturn(user);

        final UserDto userDto = userService.createUser(UserMapper.toUserDto(user));

        assertEquals(UserMapper.toUserDto(user), userDto);

        verify(userRepository, times(1))
                .save(user);
    }

    @ParameterizedTest
    @MethodSource("updateUserValues")
    void updateUserTest(String oldName, String newName, String resultName,
                        String oldEmail, String newEmail, String resultEmail) {

        User oldUser = new User(1L, oldName, oldEmail);
        User newUser = new User(1L, newName, newEmail);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));

        when(userRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, User.class));

        final UserDto userDto = userService.updateUser(1L, UserMapper.toUserDto(newUser));

        assertEquals(1L, userDto.getId());
        assertEquals(resultName, userDto.getName());
        assertEquals(resultEmail, userDto.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void updateUserNotFoundTest() {
        final long id = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class,
                () -> userService.updateUser(id, UserMapper.toUserDto(user)));

        assertTrue(exception.getMessage().contains("Не найден пользователь с id " + id));
    }

    @Test
    void deleteUserTest() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void getUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        final UserDto userDto = userService.getUser(1L);

        assertEquals(UserMapper.toUserDto(user), userDto);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserNotFoundTest() {
        final Long id = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentIdException.class, () -> userService.getUser(id));

        assertTrue(exception.getMessage().contains("Не найден пользователь с id " + id));
    }

    @Test
    void getAllUsersTest() {
        final Page<User> users = new PageImpl<>(Collections.singletonList(user));
        final PageRequest pageRequest = new MyPageRequest(0, 10, Sort.unsorted());

        when(userRepository.findAll(new MyPageRequest(0, 10, Sort.unsorted()))).thenReturn(users);

        final List<UserDto> userDtos = userService.getAll(pageRequest);

        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());
        assertEquals(UserMapper.toUserDto(user), userDtos.get(0));

        verify(userRepository, times(1)).findAll(pageRequest);
    }

    private static Stream<Arguments> updateUserValues() {
        return Stream.of(
                Arguments.of("Old Name", "New Name", "New Name",
                        "old@email.com", "new@email.com", "new@email.com"),
                Arguments.of("Old Name", null, "Old Name",
                        "old@email.com", null, "old@email.com"),
                Arguments.of("Old Name", "New Name", "New Name",
                        "old@email.com", null, "old@email.com"),
                Arguments.of("Old Name", null, "Old Name",
                        "old@email.com", "new@email.com", "new@email.com")
        );
    }
}
