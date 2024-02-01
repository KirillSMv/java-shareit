package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User add(User user);

    User getById(long id);

    List<User> getAll();

    User updateUser(long id, User user);

    void deleteUserById(long id);
}
