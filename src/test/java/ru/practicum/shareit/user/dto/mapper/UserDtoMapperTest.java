package ru.practicum.shareit.user.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDtoMapperTest {

    private UserDtoMapper userDtoMapper;

    @BeforeEach
    void setUp() {
        userDtoMapper = new UserDtoMapper();
    }

    @Test
    void toDtoTest_returnDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        UserDto expectedUserDto = new UserDto(1L, "Vladimir", "vladimir@yandex.ru");

        UserDto resultUserDto = userDtoMapper.toDto(user);

        assertEquals(expectedUserDto, resultUserDto);
    }

    @Test
    void toUserTest_returnUser() {
        UserDto userDto = new UserDto(null, "Vladimir", "vladimir@yandex.ru");
        User expectedUser = new User(null, "Vladimir", "vladimir@yandex.ru");

        User resultUser = userDtoMapper.toUser(userDto);

        assertEquals(expectedUser, resultUser);
    }
}