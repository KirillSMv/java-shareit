package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User add(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с id {} не найден", id);
            return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", id));
        });
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public User updateUser(long id, User user) {
        User savedUser = userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с id {} не найден", id);
            return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", id));
        });
        updateRequiredFields(savedUser, user);
        return savedUser;
    }

    @Transactional
    @Override
    public void deleteUserById(long id) {
        userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь с id {} не найден", id);
            return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", id));
        });
        userRepository.deleteById(id);
    }

    private void updateRequiredFields(User savedUser, User newUser) {
        if (newUser.getEmail() != null) {
            savedUser.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            savedUser.setName(newUser.getName());
        }
    }
}
