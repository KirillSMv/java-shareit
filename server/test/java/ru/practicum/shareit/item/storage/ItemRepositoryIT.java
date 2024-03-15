package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"db.name=shareItTest", "spring.sql.init.schema-locations=classpath:schema_test.sql"})
class ItemRepositoryIT {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private User owner;
    private Item item;

    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Kirill");
        user.setEmail("kirill@yandex.ru");
        testEntityManager.persist(user);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(user);
        testEntityManager.persist(itemRequest);


        owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        testEntityManager.persist(owner);


        item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setRequest(itemRequest);
        item.setOwner(owner);
        item.setAvailable(true);
        itemRepository.save(item);
    }

    @Test
    void findAllByOwnerTest_whenItemExists_thenReturnItem() {
        int page = 0;
        int size = 1;
        List<Item> items = itemRepository.findAllByOwner(owner, PageRequest.of(page, size));

        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getDescription()).isEqualTo(item.getDescription());
        assertThat(items.get(0).getName()).isEqualTo(item.getName());
        assertThat(items.get(0).getAvailable()).isEqualTo(item.getAvailable());
        assertThat(items.get(0).getOwner()).isEqualTo(item.getOwner());
        assertThat(items.get(0).getRequest()).isEqualTo(item.getRequest());
    }

    @Test
    void findAllContainingTextWithAvailableStatusTest_whenItemExists_thenReturnListOfItem() {
        int page = 0;
        int size = 1;
        List<Item> items = itemRepository.findAllContainingTextWithAvailableStatus("имя", PageRequest.of(page, size));

        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getDescription()).isEqualTo(item.getDescription());
        assertThat(items.get(0).getName()).isEqualTo(item.getName());
        assertThat(items.get(0).getAvailable()).isEqualTo(item.getAvailable());
        assertThat(items.get(0).getOwner()).isEqualTo(item.getOwner());
        assertThat(items.get(0).getRequest()).isEqualTo(item.getRequest());
    }

    @Test
    void findAllByRequestTest_whenItemExists_thenReturnListOfItem() {
        List<Item> items = itemRepository.findAllByRequest(itemRequest);

        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getDescription()).isEqualTo(item.getDescription());
        assertThat(items.get(0).getName()).isEqualTo(item.getName());
        assertThat(items.get(0).getAvailable()).isEqualTo(item.getAvailable());
        assertThat(items.get(0).getOwner()).isEqualTo(item.getOwner());
        assertThat(items.get(0).getRequest()).isEqualTo(item.getRequest());
    }

    @Test
    void findAllByRequestInTest_whenItemExists_thenReturnListOfItem() {

        List<Item> items = itemRepository.findAllByRequestIn(List.of(itemRequest));

        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).getId()).isEqualTo(item.getId());
        assertThat(items.get(0).getDescription()).isEqualTo(item.getDescription());
        assertThat(items.get(0).getName()).isEqualTo(item.getName());
        assertThat(items.get(0).getAvailable()).isEqualTo(item.getAvailable());
        assertThat(items.get(0).getOwner()).isEqualTo(item.getOwner());
        assertThat(items.get(0).getRequest()).isEqualTo(item.getRequest());
    }
}