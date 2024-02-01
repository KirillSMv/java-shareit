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
        checkParameters(user);
        checkIfUserExistsByEmail(user.getEmail());
        user.setId(calculateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(long id) {
        checkIfUserExistsById(id);
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
        /*
        Здесь я возвращаю значения из мапы, я могу вернуть new ArrayList<>(...) или List.of(...) для соблюдения
        инкапсуляции. Но здесь я думаю в этом нет смысла, поскольку как такого объекта - списка, у меня все равно нет,
        а доступ к ссылочным типам внутри списка остается и так и так. Верно?
        */
    }

    @Override
    public User updateUser(long id, User user) {
        checkIfUserExistsById(id);
        checkIfNameBlank(user);
        checkIfUserWithEmailAlreadyExists(id, user);

        User savedUser = users.get(id);
        updateFields(savedUser, user);
        users.put(id, savedUser);
        return savedUser;
    }

    private void checkIfUserWithEmailAlreadyExists(long id, User user) {
        boolean isNameUsed = users.values().stream().anyMatch(us -> us.getEmail().equals(user.getEmail()));
        if (isNameUsed && !users.get(id).getEmail().equals(user.getEmail())) {
            log.error("Пользователь с email = {} уже существует", user.getEmail());
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует",
                    user.getEmail()));
        }
    }

    @Override
    public void deleteUserById(long id) {
        checkIfUserExistsById(id);
        users.remove(id);
    }

    private void updateFields(User user, User updatedUser) {
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
    }

    private void checkIfUserExistsById(long id) {
        if (users.get(id) == null) {
            log.error("Пользователя с id = {} не существует", id);
            throw new ObjectNotFoundException(String.format("Пользователя с id = %d не существует", id));
        }
    }

    private void checkIfUserExistsByEmail(String email) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
            log.error("Пользователь с email = {} уже существует", email);
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email = %s уже существует", email));
        }
    }

    private long calculateId() {
        return ++uniqueId;
    }

    private void checkParameters(User user) {
        if (user.getName() == null || user.getEmail() == null) {
            log.error("Для добавления пользователя необходимо указать имя и адрес электронной почты");
            throw new IllegalArgumentException("Для добавления пользователя необходимо указать имя " +
                    "и адрес электронной почты");
        }
        checkIfNameBlank(user);
    }

    private void checkIfNameBlank(User user) {
        if (user.getName() != null) {
            if ((user.getName().isBlank())) {
                log.error("Имя пользователя не может быть пустым");
                throw new IllegalArgumentException("Имя пользователя не может быть пустым");
            }
        }
    }
}
