package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.AuthorizationException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.dto.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.dto.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @Mock
    private BookingDtoMapper bookingDtoMapper;
    @Mock
    private CommentDtoMapper commentDtoMapper;
    @InjectMocks
    private ItemServiceImpl itemServiceImpl;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;


    private static final long requesterUserId = 1L;
    private static final long itemId = 1L;


/*    В тестах сервисов я в начале сделал инициализацию переменных в методе с аннотацией @BeforeEach, потом подумал, что наверное нагляднее
      сделать в каждом методе переменные, которые используются. Хотя тогда получается дублирование кода и дольше..
      Как лучше делать в целом в тестах?
*/


/*
    private User user;
    private ItemRequest itemRequest;
    private ItemRequestFromUserDto itemRequestFromUserDto;
    private ItemRequestToUserDto itemRequestToUserDto;
    private ItemRequestInfoDto itemRequestInfoDto;
    private Item item;
    private ItemDtoFromUser itemDtoFromUser;


    @BeforeEach
    void setUp() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        ItemRequestFromUserDto itemRequestFromUserDto = new ItemRequestFromUserDto("описание");
        ItemRequestToUserDto itemRequestToUserDto = new ItemRequestToUserDto(1L, "описание", itemRequest.getCreated());
        ItemRequestInfoDto.ItemInfoDto itemInfoDto = new ItemRequestInfoDto.ItemInfoDto(1L, "имя", "описание", itemRequest.getId(), true);
        ItemRequestInfoDto itemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), List.of(itemInfoDto));
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
    }*/

    @Test
    void addTest_whenNoUserFound_thenThrowObjectNotFoundException() {
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", true, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.add(requesterUserId, itemDtoFromOrToUser));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void addTest_whenRequestIdForItemIsNull_thenReturnItemWithRequestNull() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item expectedItem = new Item(1L, "имя", "описание", true, null, null);
        Item itemForSave = new Item(1L, "имя", "описание", true, user, null);
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", true, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemDtoMapper.toItem(itemDtoFromOrToUser)).thenReturn(expectedItem);
        when(itemRepository.save(itemForSave)).thenReturn(itemForSave);

        Item savedItem = itemServiceImpl.add(requesterUserId, itemDtoFromOrToUser);

        assertEquals(expectedItem, savedItem);
    }

    @Test
    void addTest_whenRequestIdForItemNotNullAndNoItemFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", true, itemRequest.getId());
        Long requestId = 1L;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemDtoMapper.toItem(itemDtoFromOrToUser)).thenReturn(item);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.add(requesterUserId, itemDtoFromOrToUser));

        assertEquals("Запрос с id 1 не найден", exception.getMessage());
    }

    @Test
    void addTest_whenRequestIdForItemNotNullAndItemFound_thenReturnItem() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, null, null);
        Item expectedItem = new Item(1L, "имя", "описание", true, user, itemRequest);
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser(1L, "имя", "описание", true, itemRequest.getId());
        Long requestId = 1L;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemDtoMapper.toItem(itemDtoFromOrToUser)).thenReturn(item);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(expectedItem)).thenReturn(expectedItem);

        Item savedItem = itemServiceImpl.add(requesterUserId, itemDtoFromOrToUser);

        verify(itemRepository, times(1)).save(expectedItem);
        assertEquals(expectedItem, savedItem);
    }

    @Test
    void updateItemTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.updateItem(requesterUserId, itemId, item));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void updateItemTest_whenUserFoundAndItemNotFound_henThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.updateItem(requesterUserId, itemId, item));

        assertEquals("Вещи с id 1 не найдено", exception.getMessage());
    }

    @Test
    void updateItemTest_whenUserFoundAndItemFoundAndNotOwnerUpdates_thenThrowAuthorizationExceptionException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, owner, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));


        AuthorizationException exception = assertThrows(AuthorizationException.class, () ->
                itemServiceImpl.updateItem(requesterUserId, itemId, item));

        assertEquals("Обновить данные вещи может только ее владелец, userId = 1, ownerId = 2", exception.getMessage());
    }

    @Test
    void updateItemTest_whenUpdateNameOnly_thenItemNameIsUpdated() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item newItem = new Item(1L, "новое имя", null, null, null, null);
        Item expectedItemAfterUpdate = new Item(1L, "новое имя", "описание", true, user, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedItemAfterUpdate));
        when(itemRepository.save(expectedItemAfterUpdate)).thenReturn(expectedItemAfterUpdate);

        Item updatedItem = itemServiceImpl.updateItem(requesterUserId, itemId, newItem);

        assertEquals(expectedItemAfterUpdate, updatedItem);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        updatedItem = itemArgumentCaptor.getValue();
        assertEquals(expectedItemAfterUpdate, updatedItem);
    }

    @Test
    void updateItemTest_whenUpdateDescriptionOnly_thenItemDescriptionIsUpdated() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item newItem = new Item(1L, null, "новое описание", null, null, null);
        Item expectedItemAfterUpdate = new Item(1L, "имя", "новое описание", true, user, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedItemAfterUpdate));
        when(itemRepository.save(expectedItemAfterUpdate)).thenReturn(expectedItemAfterUpdate);

        Item updatedItem = itemServiceImpl.updateItem(requesterUserId, itemId, newItem);

        assertEquals(expectedItemAfterUpdate, updatedItem);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        updatedItem = itemArgumentCaptor.getValue();
        assertEquals(expectedItemAfterUpdate, updatedItem);
    }

    @Test
    void updateItemTest_whenUpdateAvailableOnly_thenItemAvailableIsUpdated() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item newItem = new Item(1L, null, null, false, null, null);
        Item expectedItemAfterUpdate = new Item(1L, "имя", "описание", false, user, null);
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedItemAfterUpdate));
        when(itemRepository.save(expectedItemAfterUpdate)).thenReturn(expectedItemAfterUpdate);

        Item updatedItem = itemServiceImpl.updateItem(requesterUserId, itemId, newItem);

        assertEquals(expectedItemAfterUpdate, updatedItem);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        updatedItem = itemArgumentCaptor.getValue();
        assertEquals(expectedItemAfterUpdate, updatedItem);
    }

    @Test
    void getWithBookingsByIdTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.getWithBookingsById(requesterUserId, itemId));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getWithBookingsByIdTest_whenItemNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.getWithBookingsById(requesterUserId, itemId));

        assertEquals("Вещи с id 1 не найдено", exception.getMessage());
    }

    @Test
    void getWithBookingsByIdTest_whenOwnerRequestsAndCommentsEmpty_thenReturnItemDtoWithEmptyCommentsList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Booking lastBooking = new Booking(1L, LocalDateTime.of(2020, 10, 20, 10, 10, 10), LocalDateTime.of(2021, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        Booking nextBooking = new Booking(1L, LocalDateTime.of(2050, 10, 20, 10, 10, 10), LocalDateTime.of(2051, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        BookingDto lastBookingDto = new BookingDto(1L, lastBooking.getStart(), lastBooking.getEnd(), item, 1L);
        BookingDto nextBookingDto = new BookingDto(1L, nextBooking.getStart(), nextBooking.getEnd(), item, 1L);
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, lastBookingDto, nextBookingDto, Collections.emptyList());

        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemDtoMapper.toItemDtoWithComments(item, Collections.emptyList(), lastBookingDto, nextBookingDto)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(anyList())).thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItem(item)).thenReturn(Collections.emptyList());
        when(bookingService.getLastOrNextBooking(item, true)).thenReturn(lastBooking);
        when(bookingService.getLastOrNextBooking(item, false)).thenReturn(nextBooking);
        when(bookingDtoMapper.toBookingDtoWithBookerId(lastBooking)).thenReturn(lastBookingDto);
        when(bookingDtoMapper.toBookingDtoWithBookerId(nextBooking)).thenReturn(nextBookingDto);

        ItemDtoWithComments savedItemDtoWithComments = itemServiceImpl.getWithBookingsById(requesterUserId, itemId);

        assertEquals(expectedItemDtoWithComments, savedItemDtoWithComments);
    }

    @Test
    void getWithBookingsByIdTest_whenOwnerRequestsAndNoBookings_thenReturnItemDtoWithCommentsListAndNullBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        CommentDto commentDto = new CommentDto(1L, "текст", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, null, null, List.of(commentDto));

        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemDtoMapper.toItemDtoWithComments(item, List.of(commentDto), null, null)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of(comment));
        when(bookingService.getLastOrNextBooking(item, true)).thenReturn(null);
        when(bookingService.getLastOrNextBooking(item, false)).thenReturn(null);
        when(bookingDtoMapper.toBookingDtoWithBookerId(null)).thenReturn(null);
        when(bookingDtoMapper.toBookingDtoWithBookerId(null)).thenReturn(null);

        ItemDtoWithComments savedItemDtoWithComments = itemServiceImpl.getWithBookingsById(requesterUserId, itemId);

        assertEquals(expectedItemDtoWithComments, savedItemDtoWithComments);
    }

    @Test
    void getWithBookingsByIdTest_whenOwnerRequests_thenReturnItemDtoWithCommentsListAndBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Booking lastBooking = new Booking(1L, LocalDateTime.of(2020, 10, 20, 10, 10, 10), LocalDateTime.of(2021, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        Booking nextBooking = new Booking(1L, LocalDateTime.of(2050, 10, 20, 10, 10, 10), LocalDateTime.of(2051, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        BookingDto lastBookingDto = new BookingDto(1L, lastBooking.getStart(), lastBooking.getEnd(), item, 1L);
        BookingDto nextBookingDto = new BookingDto(1L, nextBooking.getStart(), nextBooking.getEnd(), item, 1L);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        CommentDto commentDto = new CommentDto(1L, "текст", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, lastBookingDto, nextBookingDto, List.of(commentDto));

        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemDtoMapper.toItemDtoWithComments(item, List.of(commentDto), lastBookingDto, nextBookingDto)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of(comment));
        when(bookingService.getLastOrNextBooking(item, true)).thenReturn(lastBooking);
        when(bookingService.getLastOrNextBooking(item, false)).thenReturn(nextBooking);
        when(bookingDtoMapper.toBookingDtoWithBookerId(lastBooking)).thenReturn(lastBookingDto);
        when(bookingDtoMapper.toBookingDtoWithBookerId(nextBooking)).thenReturn(nextBookingDto);

        ItemDtoWithComments savedItemDtoWithComments = itemServiceImpl.getWithBookingsById(requesterUserId, itemId);

        assertEquals(expectedItemDtoWithComments, savedItemDtoWithComments);
    }

    @Test
    void getWithBookingsByIdTest_whenNotOwnerRequests_thenReturnItemDtoWithCommentsListAndNullBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        CommentDto commentDto = new CommentDto(1L, "текст", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, null, null, List.of(commentDto));
        long requesterUserId = 2L;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemDtoMapper.toItemDtoWithComments(item, List.of(commentDto), null, null)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(anyList())).thenReturn(List.of(commentDto));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of(comment));

        ItemDtoWithComments savedItemDtoWithComments = itemServiceImpl.getWithBookingsById(requesterUserId, itemId);

        assertEquals(expectedItemDtoWithComments, savedItemDtoWithComments);
    }

    @Test
    void addCommentTest_whenUserNotFound_thenThrowObjectNotFoundException() {
        Comment comment = new Comment(1L, "текст", null, null, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.addComment(requesterUserId, itemId, comment));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void addCommentTest_whenItemNotFound_thenThrowObjectNotFoundException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Comment comment = new Comment(1L, "текст", null, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.addComment(requesterUserId, itemId, comment));

        assertEquals("Вещи с id 1 не найдено", exception.getMessage());
    }

    @Test
    void addCommentTest_whenItemWasNotRented_thenThrowIllegalArgumentException() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));

        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingService.checkIfUserRentedItem(user, item)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemServiceImpl.addComment(requesterUserId, itemId, comment));

        assertEquals("Для добавления отзыва нужно завершить аренду вещи", exception.getMessage());
    }

    @Test
    void addCommentTest_whenUserAndItemWereFoundAndItemWasRented_thenReturnComment() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item item = new Item(1L, "имя", "описание", true, user, itemRequest);
        Comment comment = new Comment(1L, "текст", null, null, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        Comment expectedComment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));

        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingService.checkIfUserRentedItem(user, item)).thenReturn(true);
        when(commentRepository.save(expectedComment)).thenReturn(expectedComment);

        Comment resultComment = itemServiceImpl.addComment(requesterUserId, itemId, comment);

        assertEquals(expectedComment, resultComment);

        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment savedComment = commentArgumentCaptor.getValue();
        assertEquals(expectedComment, savedComment);
    }

    @Test
    void searchTest_whenTestIsBlank_thenReturnEmptyList() {
        List<Item> itemsList = itemServiceImpl.search(requesterUserId, "", 0, 1);
        assertTrue(itemsList.isEmpty());
    }

    @Test
    void searchTest_whenItemFound_thenReturnListOfItem() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item expectedItem = new Item(1L, "имя", "описание", true, user, null);
        String text = "text";
        int page = 0;
        int size = 1;
        when(itemRepository.findAllContainingTextWithAvailableStatus(text, PageRequest.of(page, size))).thenReturn(List.of(expectedItem));

        List<Item> items = itemServiceImpl.search(requesterUserId, "text", 0, 1);

        assertEquals(List.of(expectedItem), items);
    }


    @Test
    void findItemsForRequestTest_whenItemsFound_thenReturnListOfItems() {
        long requestId = 1;
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item expectedItem = new Item(1L, "имя", "описание", true, user, itemRequest);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest(itemRequest)).thenReturn(List.of(expectedItem));

        List<Item> items = itemServiceImpl.findItemsForRequest(requestId);

        assertEquals(List.of(expectedItem), items);
    }

    @Test
    void findItemsForRequestsTest_whenItemsFound_thenReturnListOfItems() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        Item expectedItem = new Item(1L, "имя", "описание", true, user, itemRequest);
        when(itemRepository.findAllByRequestIn(anyList())).thenReturn(List.of(expectedItem));

        List<Item> items = itemServiceImpl.findItemsForRequests(List.of(itemRequest));

        assertEquals(List.of(expectedItem), items);
    }

    @Test
    void getByIdTest_whenItemNotFound_thenThrowObjectNotFoundException() {
        long itemId = 1;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.getById(itemId));

        assertEquals("Вещи с id 1 не найдено", exception.getMessage());
    }

    @Test
    void getByIdTest_whenItemFound_thenReturnItem() {
        long itemId = 1;
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item expectedItem = new Item(1L, "имя", "описание", true, user, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedItem));

        Item returnedItem = itemServiceImpl.getById(itemId);

        assertEquals(expectedItem, returnedItem);
    }

    @Test
    void getAllForUserPageableTest_whenUserNotFound_ThenReturnEmptyList() {
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                itemServiceImpl.getAllForUserPageable(requesterUserId, 0, 1));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void getAllForUserPageableTest_whenItemsNotFound_ThenReturnEmptyList() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        int page = 0;
        int size = 1;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user, PageRequest.of(page, size))).thenReturn(Collections.emptyList());

        List<ItemDtoWithComments> resultList = itemServiceImpl.getAllForUserPageable(requesterUserId, 0, 1);

        assertTrue(resultList.isEmpty());
    }

    @Test
    void getAllForUserPageableTest_whenBookingsNotFound_ThenReturnItemDtoWithNullBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        CommentDto commentDto = new CommentDto(1L, "текст", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, null, null, List.of(commentDto));
        int page = 0;
        int size = 1;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user, PageRequest.of(page, size))).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIn(List.of(item))).thenReturn(List.of(comment));
        when(bookingService.findAllLastAndNextBookingsForItems(List.of(item), page, size)).thenReturn(Collections.emptyList());
        when(itemDtoMapper.toItemDtoWithComments(item, List.of(commentDto), null, null)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(List.of(comment))).thenReturn(List.of(commentDto));
        when(bookingDtoMapper.toBookingDtoWithBookerId(null)).thenReturn(null);

        List<ItemDtoWithComments> resultList = itemServiceImpl.getAllForUserPageable(requesterUserId, 0, 1);

        assertEquals(List.of(expectedItemDtoWithComments), resultList);
    }

    @Test
    void getAllForUserPageableTest_whenBookingsFound_ThenReturnItemDtoWithBookings() {
        User user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        Item item = new Item(1L, "имя", "описание", true, user, null);
        Comment comment = new Comment(1L, "текст", item, user, LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        CommentDto commentDto = new CommentDto(1L, "текст", user.getName(), LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        Booking lastBooking = new Booking(1L, LocalDateTime.of(2020, 10, 20, 10, 10, 10), LocalDateTime.of(2021, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        Booking nextBooking = new Booking(1L, LocalDateTime.of(2050, 10, 20, 10, 10, 10), LocalDateTime.of(2051, 10, 20, 10, 10, 10), item, user, Status.WAITING);
        BookingDto lastBookingDto = new BookingDto(1L, lastBooking.getStart(), lastBooking.getEnd(), item, 1L);
        BookingDto nextBookingDto = new BookingDto(1L, nextBooking.getStart(), nextBooking.getEnd(), item, 1L);
        ItemDtoWithComments expectedItemDtoWithComments = new ItemDtoWithComments(1L, "имя", "описание", true, lastBookingDto, nextBookingDto, List.of(commentDto));
        int page = 0;
        int size = 1;
        when(userRepository.findById(requesterUserId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user, PageRequest.of(page, size))).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIn(List.of(item))).thenReturn(List.of(comment));
        when(bookingService.findAllLastAndNextBookingsForItems(List.of(item), page, size)).thenReturn(List.of(lastBooking, nextBooking));
        when(itemDtoMapper.toItemDtoWithComments(item, List.of(commentDto), lastBookingDto, nextBookingDto)).thenReturn(expectedItemDtoWithComments);
        when(commentDtoMapper.toCommentDtoList(List.of(comment))).thenReturn(List.of(commentDto));
        when(bookingDtoMapper.toBookingDtoWithBookerId(lastBooking)).thenReturn(lastBookingDto);
        when(bookingDtoMapper.toBookingDtoWithBookerId(nextBooking)).thenReturn(nextBookingDto);

        List<ItemDtoWithComments> resultList = itemServiceImpl.getAllForUserPageable(requesterUserId, 0, 1);

        assertEquals(List.of(expectedItemDtoWithComments), resultList);
    }
}