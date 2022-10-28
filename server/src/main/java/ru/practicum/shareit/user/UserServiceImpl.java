package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NonExistentIdException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.save(user);
        log.info("Информация о пользователе id {} сохранена", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User updatingUser = userRepository.findById(id).orElseThrow(
                () -> new NonExistentIdException("Не найден пользователь с id " + id));
        String updatedName = updatingUser.getName();
        String updatedEmail = updatingUser.getEmail();
        if (user.getName() != null) {
            updatedName = user.getName();
        }
        if (user.getEmail() != null) {
            updatedEmail = user.getEmail();
        }
        User updatedUser = userRepository.save(new User(id, updatedName, updatedEmail));
        log.info("Информация о пользователе id {} обновлена", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Выполняется удаление пользователя id {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        return UserMapper.toUserDto(user.orElseThrow(
                () -> new NonExistentIdException("Не найден пользователь с id " + id)));
    }

    @Override
    public List<UserDto> getAll(PageRequest pageRequest) {
        Page<User> users = userRepository.findAll(pageRequest);
        log.info("Сформирована постраничная выдача из перечня всех пользователей в количестве {} чел.",
                users.getSize());
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
