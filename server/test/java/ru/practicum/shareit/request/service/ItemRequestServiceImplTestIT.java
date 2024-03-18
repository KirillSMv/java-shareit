package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTestIT {

    private final EntityManager entityManager;
    private final ItemRequestServiceImpl itemRequestService;

    @Test
    void addTest_returnItemRequest() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");

        entityManager.persist(user);
        ItemRequestFromUserDto itemDtoFromOrToUser = new ItemRequestFromUserDto("описание");

        ItemRequestToUserDto savedItemRequest = itemRequestService.add(itemDtoFromOrToUser, user.getId());

        TypedQuery<ItemRequest> query = entityManager.createQuery("select ir from ItemRequest ir where ir.id = :id", ItemRequest.class);
        ItemRequest savedItemRequestFromDb = query
                .setParameter("id", savedItemRequest.getId())
                .getSingleResult();

        Assertions.assertThat(savedItemRequestFromDb.getId()).isNotNull();
        Assertions.assertThat(savedItemRequest.getDescription()).isEqualTo(savedItemRequestFromDb.getDescription());
        Assertions.assertThat(savedItemRequest.getCreated()).isEqualTo(savedItemRequestFromDb.getCreated());
    }

    @Test
    void getByIdTest_whenItemRequestFound_thenReturnItemRequest() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setRequester(user);
        entityManager.persist(itemRequest);

        ItemRequestInfoDto itemRequestInfoDto = itemRequestService.getById(itemRequest.getId(), user.getId());

        Assertions.assertThat(itemRequest.getId()).isEqualTo(itemRequestInfoDto.getId());
        Assertions.assertThat(itemRequest.getDescription()).isEqualTo(itemRequestInfoDto.getDescription());
        Assertions.assertThat(itemRequest.getCreated()).isEqualTo(itemRequestInfoDto.getCreated());
    }

    @Test
    void updateItemRequest_whenItemRequestFound_thenReturnUpdatedItemRequest() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setRequester(user);
        entityManager.persist(itemRequest);

        List<ItemRequestInfoDto> itemRequests = itemRequestService.getAllForUser(user.getId());

        Assertions.assertThat(itemRequests.size()).isEqualTo(1);
        Assertions.assertThat(itemRequests.get(0).getId()).isNotNull();
        Assertions.assertThat(itemRequests.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        Assertions.assertThat(itemRequests.get(0).getCreated()).isEqualTo(itemRequest.getCreated());
        Assertions.assertThat(itemRequests.get(0).getItems().size()).isEqualTo(0);
    }


    @Test
    void getAllForUserPageable_returnListOfItemRequestInfoDto() {
        int page = 0;
        int size = 1;

        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User requester = new User();
        requester.setName("Vladimir");
        requester.setEmail("vladimir@yandex.ru");
        entityManager.persist(requester);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setRequester(requester);
        entityManager.persist(itemRequest);

        List<ItemRequestInfoDto> itemRequests = itemRequestService.getAllFromOtherUsersPageable(user.getId(), page, size);

        Assertions.assertThat(itemRequests.size()).isEqualTo(1);
        Assertions.assertThat(itemRequests.get(0).getId()).isNotNull();
        Assertions.assertThat(itemRequests.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        Assertions.assertThat(itemRequests.get(0).getCreated()).isEqualTo(itemRequest.getCreated());
        Assertions.assertThat(itemRequests.get(0).getItems().size()).isEqualTo(0);
    }


}



