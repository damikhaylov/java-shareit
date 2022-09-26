package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NonExistentIdException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (userRepository.isOccupiedEmail(user.getEmail())) {
            throw new ValidationException("Задан неуникальный email для пользователя " + user.getName());
        }
        User createdUser = userRepository.save(user);
        log.info("Информация о пользователе id {} сохранена", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User updatingUser = userRepository.findById(id);
        if (updatingUser == null) {
            throw new NonExistentIdException("Не найден пользователь с id " + id);
        }
        String updatedName = updatingUser.getName();
        String updatedEmail = updatingUser.getEmail();
        if (user.getName() != null) {
            updatedName = user.getName();
        }
        if (user.getEmail() != null && !user.getEmail().equals(updatingUser.getEmail())) {
            if (userRepository.isOccupiedEmail(user.getEmail())) {
                throw new ValidationException("Задан неуникальный email для пользователя id " + id);
            }
            updatedEmail = user.getEmail();
        }
        User updatedUser = userRepository.update(new User(id, updatedName, updatedEmail));
        log.info("Информация о пользователе id {} обновлена", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.delete(id)) {
            log.info("Информация о пользователе id {} удалена", id);
        } else {
            throw new NonExistentIdException("Не найден пользователь с id " + id);
        }
    }

    @Override
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NonExistentIdException("Не найден пользователь с id " + id);
        }
        log.info("Найдена информация о пользователе id {}", id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.getAll();
        log.info("Сформирован список всех пользователей в количестве {} чел.", users.size());
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
