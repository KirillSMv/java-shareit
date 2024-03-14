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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
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
    public UserDto add(@Valid @RequestBody UserDto userDto) {
        User user = userService.add(userDtoMapper.toUser(userDto));
        return userDtoMapper.toDto(user);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id,
                              @Valid @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, userDtoMapper.toUser(userDto));
        return userDtoMapper.toDto(user);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id) {
        return userDtoMapper.toDto(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(userDtoMapper::toDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    public String deleteUserById(@PathVariable("userId") @Positive(message = "id не может быть меньше 1") long id) {
        userService.deleteUserById(id);
        return String.format("User with id %d deleted", id);
    }
}
