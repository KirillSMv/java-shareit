package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;

    private static final long requesterUserId = 1L;
    private static final long requestId = 1;

    @Test
    void addTest_returnItemRequestToUserDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestFromUserDto itemRequestFromUserDto = new ItemRequestFromUserDto("описание");
        ItemRequestToUserDto expectedItemRequestToUserDto = new ItemRequestToUserDto(1L, "описание", itemRequest.getCreated());
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(itemRequestFromUserDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.toItemRequestForUserDto(itemRequest)).thenReturn(expectedItemRequestToUserDto);

        ItemRequestToUserDto savedItemRequestDto = itemRequestService.add(itemRequestFromUserDto, requesterUserId);

        verify(itemRequestRepository, times(1)).save(itemRequest);
        assertEquals(expectedItemRequestToUserDto, savedItemRequestDto);
    }

    @Test
    void addTest_whenUserNotFoundById_thenReturnObjectNotFoundException() {
        ItemRequestFromUserDto itemRequestFromUserDto = new ItemRequestFromUserDto("описание");
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemRequestService.add(itemRequestFromUserDto, 1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getByIdTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        final ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemRequestService.getById(requestId, requesterUserId));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getByIdTest_whenItemRequestNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));

        final ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemRequestService.getById(requestId, requesterUserId));

        assertEquals("Запроса с id 1 не найдено", exception.getMessage());
    }

    @Test
    void getByIdTest_whenUserAndItemRequestWereFound_thenReturnItemRequestInfoDto() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestInfoDto.ItemInfoDto itemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), List.of(itemInfoDto));
        Item item = new Item(1L, "имя", "описание", true, owner, itemRequest);

        long requestId = 1;
        when(itemService.findItemsForRequest(requestId)).thenReturn(List.of(item));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequestInfoDto(itemRequest, List.of(item))).thenReturn(expectedItemRequestInfoDto);

        ItemRequestInfoDto savedItemRequestInfoDto = itemRequestService.getById(requestId, requesterUserId);

        assertEquals(expectedItemRequestInfoDto, savedItemRequestInfoDto);
    }

    @Test
    void getByIdTest_whenNoResponsesFoundForItemRequest_thenReturnItemRequestInfoDtoWithEmptyItemsList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, owner, itemRequest);
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), Collections.emptyList());

        when(itemService.findItemsForRequest(requestId)).thenReturn(List.of(item));
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toItemRequestInfoDto(any(ItemRequest.class), anyList())).thenReturn(expectedItemRequestInfoDto);

        ItemRequestInfoDto savedItemRequestInfoDto = itemRequestService.getById(requestId, requesterUserId);

        assertTrue(savedItemRequestInfoDto.getItems().isEmpty());
        assertEquals(expectedItemRequestInfoDto, savedItemRequestInfoDto);
    }

    @Test
    void getAllForUserTest_whenNoUserFound_thenThrowObjectNotFoundException() {
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemRequestService.getAllForUser(requesterUserId));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getAllForUserTest_whenEmptyItemRequestsList_thenReturnEmptyList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterOrderByCreatedDesc(user)).thenReturn(Collections.emptyList());

        List<ItemRequestInfoDto> resultList = itemRequestService.getAllForUser(requesterUserId);

        assertTrue(resultList.isEmpty());
    }

    @Test
    void getAllForUserTest_whenNoItemsResponseForThisRequest_thenReturnItemRequestInfoDtoWithEmptyItemsList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), Collections.emptyList());
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterOrderByCreatedDesc(user)).thenReturn(List.of(itemRequest));
        when(itemService.findItemsForRequests(List.of(itemRequest))).thenReturn(Collections.emptyList());
        when(itemRequestMapper.toItemRequestInfoDto(itemRequest, Collections.emptyList())).thenReturn(expectedItemRequestInfoDto);

        List<ItemRequestInfoDto> resultList = itemRequestService.getAllForUser(requesterUserId);

        assertEquals(List.of(expectedItemRequestInfoDto), resultList);
    }

    @Test
    void getAllForUserTest_whenUserAndItemRequestAndItemsResponsesWereFound_thenReturnItemRequestInfoDtoWithItemsList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestInfoDto.ItemInfoDto itemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), List.of(itemInfoDto));
        Item item = new Item(1L, "имя", "описание", true, owner, itemRequest);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterOrderByCreatedDesc(user)).thenReturn(List.of(itemRequest));
        when(itemService.findItemsForRequests(List.of(itemRequest))).thenReturn(List.of(item));
        when(itemRequestMapper.toItemRequestInfoDto(itemRequest, List.of(item))).thenReturn(expectedItemRequestInfoDto);

        List<ItemRequestInfoDto> resultList = itemRequestService.getAllForUser(requesterUserId);

        assertEquals(List.of(expectedItemRequestInfoDto), resultList);
    }

    @Test
    void getAllFromOtherUsersPageableTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        long userId = requesterUserId;
        int page = 0;
        int size = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemRequestService.getAllFromOtherUsersPageable(userId, page, size));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getAllFromOtherUsersPageableTest_whenItemRequestsNotFound_thenReturnEmptyList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        long userId = requesterUserId;
        int page = 0;
        int size = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterNotOrderByCreatedDesc(user, PageRequest.of(page, size))).thenReturn(Collections.emptyList());

        List<ItemRequestInfoDto> resultList = itemRequestService.getAllFromOtherUsersPageable(userId, page, size);

        assertTrue(resultList.isEmpty());
    }

    @Test
    void getAllFromOtherUsersPageableTest_whenUserAndItemRequestsWereFound_thenReturnItemRequestInfoDtoList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestInfoDto.ItemInfoDto itemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);
        ItemRequestInfoDto expectedItemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), List.of(itemInfoDto));
        Item item = new Item(1L, "имя", "описание", true, owner, itemRequest);
        long userId = requesterUserId;
        int page = 0;
        int size = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterNotOrderByCreatedDesc(user, PageRequest.of(page, size))).thenReturn(List.of(itemRequest));
        when(itemService.findItemsForRequests(List.of(itemRequest))).thenReturn(List.of(item));
        when(itemRequestMapper.toItemRequestInfoDto(itemRequest, List.of(item))).thenReturn(expectedItemRequestInfoDto);

        List<ItemRequestInfoDto> resultList = itemRequestService.getAllFromOtherUsersPageable(userId, page, size);

        assertEquals(List.of(expectedItemRequestInfoDto), resultList);
    }
}