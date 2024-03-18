package ru.practicum.shareit.user.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(new User(1L, "Kirill", "kirill@email.com"));
    }

    @Test
    void findByEmailTest_whenUserFound_thenReturnUserWithThisEmail() {
        Optional<User> savedUser = userRepository.findByEmail("kirill@email.com");

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("Kirill");
        assertThat(savedUser.get().getId()).isEqualTo(1L);
    }
}