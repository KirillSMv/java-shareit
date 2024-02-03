package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User getById(long id);

    List<User> getAll();

    User updateUser(long id, User user);

    void deleteUserById(long id);

    void checkIfUserExistsByEmail(String email);

    void checkIfUserExistsById(long id);

    void checkIfUserWithEmailAlreadyExists(long id, User user);
}
