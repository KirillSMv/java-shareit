package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
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
        checkIfUserExistsByEmail(user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", id)));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll(); //todo if we need findAll
    }

    @Transactional
    @Override
    public User updateUser(long id, User user) {
        checkIfDifferentUserWithEmailExists(id, user.getEmail());
        User savedUser = userRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", id)));
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
        //В выражении выше я логирую ошибку, но думаю, нужно ли это, если в хендлере все равно логируется потом исключение
        //Принято ли вообще логировать в случаях orElseThrow()?

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

    private void checkIfUserExistsByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Пользователь с email {} уже существует", email);
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
        }
    }

    private void checkIfDifferentUserWithEmailExists(long id,String email) {
        if (userRepository.findByEmail(email).stream().anyMatch(us -> us.getId() != id)) {
            log.error("Пользователь с email {} уже существует", email);
            throw new ObjectAlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
        }
    }
}
