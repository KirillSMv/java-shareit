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
        userStorage.checkIfUserExistsByEmail(user.getEmail());
        return userStorage.add(user);
    }

    @Override
    public User getById(long id) {
        userStorage.checkIfUserExistsById(id);
        return userStorage.getById(id);
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    @Override
    public User updateUser(long id, User user) {
        userStorage.checkIfUserExistsById(id);
        userStorage.checkIfUserWithEmailAlreadyExists(id, user);

        User userForUpdate = getById(id);
        updateFields(userForUpdate, user);
        return userStorage.updateUser(id, userForUpdate);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.checkIfUserExistsById(id);
        userStorage.deleteUserById(id);
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
