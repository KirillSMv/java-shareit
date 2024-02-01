/*
package ru.practicum.shareit;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceTest {
    private UserDto userDto;
    private User user;

    private HttpClient client;
    private static final String URL = "http://localhost:8080";
    private static final Gson gson = new Gson();

    private final UserService userService;


    @BeforeEach
    public void init() {
        client = HttpClient.newHttpClient();

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("name@email.ru");
        user = UserDtoMapper.toUserMapper(userDto);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/users"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userDto, UserDto.class)))
                .build();
        try {
            client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addUserShouldBeOk() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setName("newUser");
        newUser.setEmail("newName@email.ru");
        User initialUser = userService.add(newUser);
        assertEquals(newUser, initialUser);
    }

    @Test
    public void updateUserShouldBeOk() {
        user.setName("updatedName");
        user.setEmail("newEmail@email.com");
        User updatedUser = userService.updateUser(2, user);
        assertEquals("updatedName", updatedUser.getName());
    }

    @Test
    public void testGetUserByIdShouldBeOk() throws IOException, InterruptedException {
        UserDto newUser = new UserDto();
        newUser.setName("Name");
        newUser.setEmail("email@email.com");
        newUser.setId(4L);

        HttpRequest httpRequestForUser = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/users"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newUser, UserDto.class)))
                .build();
        client.send(httpRequestForUser, HttpResponse.BodyHandlers.ofString());

        User savedUser = userService.getById(4);
        User initialUser = UserDtoMapper.toUserMapper(newUser);
        assertEquals(initialUser, savedUser);

        System.out.println(userService.getAll());
    }

    @Test
    public void testGetAllUsersShouldBeOk() {
        List<User> users = userService.getAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testDeleteUserByIdShouldBeOk() {
        userService.deleteUserById(1);
        assertThrows(ObjectNotFoundException.class, () -> userService.getById(1));
    }
}
*/
