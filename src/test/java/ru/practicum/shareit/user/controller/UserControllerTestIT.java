package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTestIT {

    @MockBean
    private UserService userService;
    @MockBean
    private UserDtoMapper userDtoMapper;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private User user;
    private User anotherUser;
    private UserDto userDto;
    private UserDto anotherUserDto;
    private final Long requesterId = 1L;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        anotherUser = new User(2L, "Ilya", "ilya@yandex.ru");
        userDto = new UserDto(1L, "Vladimir", "vladimir@yandex.ru");
        anotherUserDto = new UserDto(2L, "Ilya", "ilya@yandex.ru");
    }

    @Test
    @SneakyThrows
    void addTest_whenUserDtoIsValid_thenReturnUser() {
        when(userService.add((user))).thenReturn(user);
        when(userDtoMapper.toUser(userDto)).thenReturn(user);
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    @SneakyThrows
    void addTest_whenUserDtoNameIsBlank_thenStatusIsBadRequest() {
        UserDto userDto = new UserDto(1L, "", "vladimir@yandex.ru");

        when(userService.add((user))).thenReturn(user);
        when(userDtoMapper.toUser(userDto)).thenReturn(user);
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).add(user);
    }

    @SneakyThrows
    @Test
    void updateUserTest_whenUserDtoEmailIsIncorrect_thenStatusIsBadRequest() {
        UserDto userDto = new UserDto(1L, "Vladimir", "vladimirrr");

        when(userService.updateUser(requesterId, user)).thenReturn(user);
        when(userDtoMapper.toUser(userDto)).thenReturn(user);
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", requesterId)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).add(user);

    }

    @Test
    @SneakyThrows
    void getByIdTest_whenExists_thenReturnUser() {
        long userId = 1L;
        when(userService.getById(userId)).thenReturn(user);
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    @SneakyThrows
    void getAllTest_whenUserExists_thenReturnListOfUser() {
        when(userService.getAll()).thenReturn(List.of(user, anotherUser));
        when(userDtoMapper.toDto(user)).thenReturn(userDto);
        when(userDtoMapper.toDto(anotherUser)).thenReturn(anotherUserDto);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(anotherUserDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(anotherUserDto.getName())))
                .andExpect(jsonPath("$[1].email", is(anotherUserDto.getEmail())));
    }

    @Test
    @SneakyThrows
    void deleteUserByIdTest_UserDeleted() {
        Mockito.doNothing().when(userService).deleteUserById(anyLong());

        mvc.perform(delete("/users/{userId}", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("User with id 1 deleted")));
    }
}