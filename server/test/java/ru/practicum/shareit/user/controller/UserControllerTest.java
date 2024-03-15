package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserDtoMapper userDtoMapper;
    @InjectMocks
    private UserController userController;

    private ObjectMapper mapper;

    private MockMvc mvc;


    private User user;
    private User anotherUser;
    private UserDto userDto;
    private UserDto anotherUserDto;
    private final Long requesterId = 1L;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(userController).build();
        mapper = new ObjectMapper();

        user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        anotherUser = new User(2L, "Ilya", "ilya@yandex.ru");
        userDto = new UserDto(1L, "Vladimir", "vladimir@yandex.ru");
        anotherUserDto = new UserDto(2L, "Ilya", "ilya@yandex.ru");
    }

    @Test
    @SneakyThrows
    void addTest_returnUser() {
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

    @SneakyThrows
    @Test
    void updateUserTest_returnUpdatedUser() {
        when(userService.updateUser(requesterId, user)).thenReturn(user);
        when(userDtoMapper.toUser(userDto)).thenReturn(user);
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", requesterId)
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
                .andExpect(status().isOk());
    }
}