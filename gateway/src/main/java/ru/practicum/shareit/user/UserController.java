package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserClient userClient;

    @Validated(OnCreate.class)
    @PostMapping
    public ResponseEntity<Object> add(@Valid @RequestBody UserDto userDto) {
        log.info("add user {}", userDto);
        return userClient.postUser(userDto);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id,
                                             @Valid @RequestBody UserDto userDto) {
        log.info("updateUser {}, userId ={}", userDto, id);
        return userClient.updateUser(id, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id) {
        log.info("getById userId={}", id);
        return userClient.getUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("getAllUsers");
        return userClient.getUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id) {
        log.info("deleteUserById userId = {}", id);
        return userClient.deleteUser(id);
    }
}

