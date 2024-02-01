package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto add(@Valid @RequestBody UserDto userDto) {
        User user = userService.add(UserDtoMapper.toUserMapper(userDto));
        return UserDtoMapper.toDtoMapper(user);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable("userId") long id, @Valid @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, UserDtoMapper.toUserMapper(userDto));
        return UserDtoMapper.toDtoMapper(user);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable("userId") long id) {
        return UserDtoMapper.toDtoMapper(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(UserDtoMapper::toDtoMapper).collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    public String deleteUserById(@PathVariable("userId") long id) {
        userService.deleteUserById(id);
        return String.format("Пользователь с id %d удален", id);
    }
}
