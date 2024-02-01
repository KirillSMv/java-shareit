package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User add(User user) {
        return userStorage.add(user);
    }

    @Override
    public User getById(long id) {
        return userStorage.getById(id);
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    @Override
    public User updateUser(long id, User user) {
        return userStorage.updateUser(id, user);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.deleteUserById(id);
    }
}
