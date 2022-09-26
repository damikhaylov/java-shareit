package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserInMemoryRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Long nextId = 1L;

    public User save(User user) {
        Long id = takeNextId();
        user.setId(id);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return users.get(id);
    }

    public User update(User user) {
        Long id = user.getId();
        emails.remove(findById(id).getEmail());
        users.replace(id, user);
        emails.add(user.getEmail());
        return users.get(id);
    }

    public boolean delete(Long id) {
        User user = findById(id);
        if (user == null) {
            return false;
        }
        emails.remove(user.getEmail());
        users.remove(id);
        return true;
    }

    public User findById(Long id) {
        return users.get(id);
    }

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public boolean isOccupiedEmail(String email) {
        return emails.contains(email);
    }

    private Long takeNextId() {
        return nextId++;
    }
}
