package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@Transactional
@SpringBootTest(
        properties = {"db.name=shareItTest", "spring.sql.init.schema-locations=classpath:schema_test.sql"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTestIT {

    private final EntityManager entityManager;
    private final ItemServiceImpl itemService;

    @Test
    void addTest_thenReturnItem() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser();
        itemDtoFromOrToUser.setName("имя");
        itemDtoFromOrToUser.setDescription("описание");
        itemDtoFromOrToUser.setAvailable(true);
        itemDtoFromOrToUser.setRequestId(null);

        Item item = itemService.add(user.getId(), itemDtoFromOrToUser);

        TypedQuery<Item> query = entityManager.createQuery("select it from Item it where it.id = :id", Item.class);
        Item savedItem = query
                .setParameter("id", item.getId())
                .getSingleResult();

        Assertions.assertThat(savedItem.getId()).isNotNull();
        Assertions.assertThat(itemDtoFromOrToUser.getName()).isEqualTo(savedItem.getName());
        Assertions.assertThat(itemDtoFromOrToUser.getDescription()).isEqualTo(savedItem.getDescription());
        Assertions.assertThat(itemDtoFromOrToUser.getAvailable()).isEqualTo(savedItem.getAvailable());
        Assertions.assertThat(itemDtoFromOrToUser.getRequestId()).isEqualTo(null);
    }
}