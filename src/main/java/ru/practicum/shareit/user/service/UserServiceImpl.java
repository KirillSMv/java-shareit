package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
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
        checkIfUserExistsByEmail(user.getEmail());
        return userStorage.add(user);
    }

    @Override
    public User getById(long id) {
        checkIfUserExistsById(id);
        return userStorage.getById(id);
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    @Override
    public User updateUser(long id, User user) {
        checkIfUserExistsById(id);
        checkIfUserWithEmailAlreadyExists(id, user);

        User userForUpdate = getById(id);
        updateFields(userForUpdate, user);
        return userStorage.updateUser(id, userForUpdate);
    }

    @Override
    public void deleteUserById(long id) {
        checkIfUserExistsById(id);
        userStorage.deleteUserById(id);
    }

    private void checkIfUserExistsByEmail(String email) {
        if (getAll().stream().anyMatch(user -> user.getEmail().equals(email))) {
            log.error("Пользователь с email = {} уже существует", email);
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует", email));
        }
    }

    private void checkIfUserExistsById(long id) {
        if (getAll().stream().noneMatch(user -> user.getId().equals(id))) {
            log.error("Пользователя с id = {} не существует", id);
            throw new ObjectNotFoundException(String.format("Пользователя с id = %d не существует", id));
        }
    }

    private void checkIfUserWithEmailAlreadyExists(long id, User user) {
        if (getAll().stream().filter(us -> !us.getId().equals(id))
                .anyMatch(us -> us.getEmail().equals(user.getEmail()))) {
            log.error("Пользователь с email = {} уже существует", user.getEmail());
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует",
                    user.getEmail()));
        }
    }

    private void updateFields(User userForUpdate, User newUser) {
        if (newUser.getEmail() != null) {
            userForUpdate.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            userForUpdate.setName(newUser.getName());
        }
    }
}
