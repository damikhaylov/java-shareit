package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {

    User save(User user);

    User update(User user);

    boolean delete(Long id);

    User findById(Long id);

    List<User> getAll();

    boolean isOccupiedEmail(String email);
}
