package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
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

    @Override
    public void checkIfUserExistsByEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            log.error("Пользователь с email = {} уже существует", email);
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует", email));
        }
    }

    @Override
    public void checkIfUserExistsById(long id) {
        if (users.values().stream().noneMatch(user -> user.getId().equals(id))) {
            log.error("Пользователя с id = {} не существует", id);
            throw new ObjectNotFoundException(String.format("Пользователя с id = %d не существует", id));
        }
    }

    @Override
    public void checkIfUserWithEmailAlreadyExists(long id, User user) {
        if (users.values().stream().filter(us -> !us.getId().equals(id))
                .anyMatch(us -> us.getEmail().equals(user.getEmail()))) {
            log.error("Пользователь с email = {} уже существует", user.getEmail());
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует",
                    user.getEmail()));
        }
    }

    private long getUniqueId() {
        return ++uniqueId;
    }
}
