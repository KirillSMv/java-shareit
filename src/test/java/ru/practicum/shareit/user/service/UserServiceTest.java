package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User user;
    private User newUser;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Kirill", "kirill@email.com");
        newUser = new User(1L, "Igor", "igor@email.com");

    }

    @Test
    public void addUserTest_whenUserSaved_thenReturnUser() {
        User user = new User(1L, "Kirill", "kirill@email.com");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.add(user);

        assertEquals(user, savedUser);
        verify(userRepository, times(1))
                .save(any(User.class));
    }

    @Test
    public void addUserTest_whenEmailUsed_thenThrowValidationException() {
        when(userRepository.save(user))
                .thenThrow(new ValidationException("пользователь с такой электронной почтой уже есть"));

        final ValidationException exception = assertThrows(ValidationException.class, () ->
                userService.add(user));

        assertEquals("пользователь с такой электронной почтой уже есть", exception.getMessage());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserTest_whenUserFound_ThenUpdateNotNullFields() {
        when(this.userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.updateUser(1, newUser);

        verify(userRepository).save(userArgumentCaptor.capture());
        User updatedUser = userArgumentCaptor.getValue();
        assertEquals(newUser, updatedUser);
    }

    @Test
    void updateUserTest_WhenNoUserById_ThenThrowObjectNotFoundException() {
        when(this.userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.updateUser(1, newUser));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void updateUserTest_WhenNullEmail_thenReturnUserWithUpdatedName() {
        User newUser = new User(1L, "newName", null);
        User expectedUserAfterUpdate = new User(1L, "newName", user.getEmail());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(expectedUserAfterUpdate)).thenReturn(expectedUserAfterUpdate);

        User updatedUser = userService.updateUser(1, newUser);

        assertEquals(expectedUserAfterUpdate, updatedUser);


    }

    @Test
    void getByIdTest_ReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User savedUser = userService.getById(1);

        assertEquals(user, savedUser);
    }

    @Test
    void getByIdTest_WhenNoUserById_ThenThrowException() {
        when(this.userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.getById(1));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getAll_ReturnUsersList() {
        when(userRepository.findAll()).thenReturn(List.of(user, newUser));

        List<User> users = userService.getAll();

        assertEquals(List.of(user, newUser), users);
    }

    @Test
    void getAll_WhenNoUsersSaved_ReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> users = userService.getAll();

        assertEquals(Collections.emptyList(), users);
    }

    @Test
    void deleteUserByIdTest_whenUserDeleted_thenUserDeleted() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deleteUserById(1);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserByIdTest_whenUserNotFoundById_ThenThrowObjectNotFoundException() {
        when(this.userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.deleteUserById(1));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }
}
