package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long uniqueId;

    @Override
    public User add(User user) {
        user.setId(getUniqueId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User updateUser(long id, User userForUpdate) {
        users.put(id, userForUpdate);
        return userForUpdate;
    }

    @Override
    public void deleteUserById(long id) {
        users.remove(id);
    }

    private long getUniqueId() {
        return ++uniqueId;
    }
}
