package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    @Validated(OnCreate.class)
    @PostMapping
    public UserDto add(@RequestBody UserDto userDto) {
        User user = userService.add(userDtoMapper.toUser(userDto));
        return userDtoMapper.toDto(user);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable("userId") long id,
                              @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, userDtoMapper.toUser(userDto));
        return userDtoMapper.toDto(user);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable("userId") long id) {
        return userDtoMapper.toDto(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(userDtoMapper::toDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") long id) {
        userService.deleteUserById(id);
    }
}
