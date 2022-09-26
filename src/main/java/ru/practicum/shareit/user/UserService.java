package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    UserDto getUser(Long id);

    List<UserDto> getAll();
}
