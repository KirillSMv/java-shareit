package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(
        properties = {"db.name=shareItTest", "spring.sql.init.schema-locations=classpath:schema_test.sql"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTestIT {

    private final UserServiceImpl userService;
    private final EntityManager entityManager;

    @Test
    void add() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");

        userService.add(user);

        TypedQuery<User> query = entityManager.createQuery("select u from User u where u.email = :email", User.class);
        User savedUser = query
                .setParameter("email", user.getEmail())
                .getSingleResult();

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo(user.getName());
        assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getByIdTest_whenUserFound_thenReturnUser() {
        User expectedUser = new User();
        expectedUser.setName("Ilya");
        expectedUser.setEmail("ilya@yandex.ru");
        entityManager.persist(expectedUser);

        User savedUser = userService.getById(1L);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo(expectedUser.getName());
        assertThat(savedUser.getEmail()).isEqualTo(expectedUser.getEmail());
    }

    @Test
    void getByIdTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        assertThatThrownBy(() -> {
            userService.getById(1L);
        }).hasMessage("Пользователь с id 1 не найден").isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void getAllTest_returnListOfUsers() {
        User expectedUser = new User();
        expectedUser.setName("Ilya");
        expectedUser.setEmail("ilya@yandex.ru");
        entityManager.persist(expectedUser);

        List<User> users = userService.getAll();

        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0)).isEqualTo(expectedUser);
    }

    @Test
    void updateUserTest_whenUserFound_thenUpdateUserFields() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User newUser = new User();
        newUser.setName("Igor");
        newUser.setEmail("igor@yandex.ru");
        User userAfterUpdate = userService.updateUser(user.getId(), newUser);

        assertThat(userAfterUpdate.getId()).isNotNull();
        assertThat(userAfterUpdate.getName()).isEqualTo(newUser.getName());
        assertThat(userAfterUpdate.getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    void updateUserTest_whenUserNoFound_thenThrowObjectNotFoundException() {
        User newUser = new User();
        newUser.setName("Ilya");
        newUser.setEmail("ilya@yandex.ru");

        assertThatThrownBy(() -> {
            userService.updateUser(1L, newUser);
        }).hasMessage("Пользователь с id 1 не найден").isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void deleteUserById_whenUserFound_thenUserDeleted() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        userService.deleteUserById(user.getId());
        TypedQuery<User> query = entityManager.createQuery("select u from User u where u.email = :email", User.class);
        List<User> users = query.setParameter("email", user.getEmail()).getResultList();

        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    void deleteUserById_whenUserNotFound_thenThrowObjectNotFoundException() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        assertThatThrownBy(() -> {
            userService.deleteUserById(1L);
        }).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Пользователь с id 1 не найден");
    }
}