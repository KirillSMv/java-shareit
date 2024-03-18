package ru.practicum.shareit.request.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryIT {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Kirill");
        user.setEmail("kirill@yandex.ru");
        testEntityManager.persist(user);

        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "описание", user, LocalDateTime.now()));
    }

    @Test
    void findAllByRequesterOrderByCreatedDescTest_whenItemRequestExist_thenReturnListOfItemRequest() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterOrderByCreatedDesc(user);

        assertThat(itemRequests.size()).isEqualTo(1);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(itemRequests.get(0).getCreated()).isEqualTo(itemRequest.getCreated());
        assertThat(itemRequests.get(0).getId()).isEqualTo(itemRequest.getId());

    }

    @Test
    void findAllByRequesterNotOrderByCreatedDescTest_whenItemRequestExist_thenReturnListOfItemRequest() {
        int page = 0;
        int size = 1;
        User anotherUser = new User();
        anotherUser.setName("Ilya");
        anotherUser.setEmail("ilya@yandex.ru");
        testEntityManager.persist(anotherUser);

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterNotOrderByCreatedDesc(anotherUser, PageRequest.of(page, size));

        assertThat(itemRequests.size()).isEqualTo(1);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(itemRequests.get(0).getCreated()).isEqualTo(itemRequest.getCreated());
        assertThat(itemRequests.get(0).getId()).isEqualTo(itemRequest.getId());
    }
}