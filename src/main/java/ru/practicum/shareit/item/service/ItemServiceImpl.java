package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.AuthorizationException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    //здесь добавляется очередная зависимость на репозиторий, но вот прямо не знаю, как без них обойтись
    //доменный объект в себе содержит зависимости и плюс DTO для пользователя требуют дополнительные сущности
    //переносить создание DTO в контроллер и добавлять там нужные зависимости кажется совсем плохой идеей)

    private final ItemDtoMapper itemDtoMapper;
    private final CommentDtoMapper commentDtoMapper;
    private final BookingDtoMapper bookingDtoMapper;

    @Transactional
    @Override
    public Item add(long userId, ItemDtoFromOrToUser itemDtoFromOrToUser) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        Item item = itemDtoMapper.toItem(itemDtoFromOrToUser);
        item.setOwner(user);

        Long requestId = itemDtoFromOrToUser.getRequestId();
        if (requestId != null) {
            item.setRequest(itemRequestRepository.findById(requestId).orElseThrow(() -> {
                        log.error("Запрос с id {} не найден", requestId);
                        return new ObjectNotFoundException(String.format("Запрос с id %d не найден", requestId));
                    }
            ));
        }
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        Item savedItem = itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещи с id {} не найдено", itemId);
            return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
        });
        checkIfOwnerUpdates(userId, savedItem);
        updateFields(savedItem, item);
        itemRepository.save(savedItem);
        return savedItem;
    }

    @Override
    public ItemDtoWithComments getWithBookingsById(long userId, long itemId) {
        userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещи с id {} не найдено", itemId);
            return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
        });
        if (userId == item.getOwner().getId()) {
            return itemDtoMapper.toItemDtoWithComments(item,
                    commentDtoMapper.toCommentDtoList(commentRepository.findAllByItem(item)),
                    bookingDtoMapper.toBookingDtoWithBookerId(bookingService.getLastOrNextBooking(item, true)),
                    bookingDtoMapper.toBookingDtoWithBookerId(bookingService.getLastOrNextBooking(item, false)));
        } else {
            return itemDtoMapper.toItemDtoWithComments(item,
                    commentDtoMapper.toCommentDtoList(commentRepository.findAllByItem(item)), null, null);
        }
    }

    @Override
    public Item getById(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещи с id {} не найдено", itemId);
            return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
        });
    }

    @Override
    public List<ItemDtoWithComments> getAllForUserPageable(long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));

        List<Item> items = itemRepository.findAllByOwner(user, PageRequest.of(page, size));
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<Comment>> commentsMap = commentRepository.findAllByItemIn(items).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<Booking> bookings = bookingService.findAllLastAndNextBookingsForItems(items, page, size);
        Map<Long, Booking> lastBookings = new HashMap<>();
        Map<Long, Booking> nextBookings = new HashMap<>();

        if (!bookings.isEmpty()) {
            Map<Long, List<Booking>> bookingsMap = bookings.stream()
                    .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
            for (Long longId : bookingsMap.keySet()) {
                List<Booking> savedList = bookingsMap.get(longId);

                Optional<Booking> lastBooking = savedList.stream()
                        .filter(element -> element.getStart().isBefore(LocalDateTime.now())).findFirst();
                lastBooking.ifPresent(booking -> lastBookings.put(longId, booking));

                Optional<Booking> nextBooking = savedList.stream()
                        .filter(element -> element.getStart().isAfter(LocalDateTime.now())).findFirst();
                nextBooking.ifPresent(booking -> nextBookings.put(longId, booking));
            }
        }
        return items.stream()
                .map(item -> itemDtoMapper.toItemDtoWithComments(item,
                        commentDtoMapper.toCommentDtoList(commentsMap.getOrDefault(item.getId(), Collections.emptyList())),
                        bookingDtoMapper.toBookingDtoWithBookerId(lastBookings.getOrDefault(item.getId(), null)),
                        bookingDtoMapper.toBookingDtoWithBookerId(nextBookings.getOrDefault(item.getId(), null))))
                .sorted(Comparator.comparingLong(ItemDtoWithComments::getId))
                .collect(Collectors.toList());

       /* Map<Long, Booking> lastBookingsListsMap = new HashMap<>();
        for (Booking booking : bookingService.findAllLastBookingsForItems(items, page, size)) {
            lastBookingsListsMap.put(booking.getItem().getId(), booking);
        }

        Map<Long, Booking> nextBookingsListMap = new HashMap<>();
        for (Booking booking : bookingService.findAllNextBookingsForItems(items, page, size)) {
            nextBookingsListMap.put(booking.getItem().getId(), booking);
        }

        return items.stream()
                .map(item -> itemDtoMapper.toItemDtoWithComments(item,
                        commentDtoMapper.toCommentDtoList(commentsMap.getOrDefault(item.getId(), Collections.emptyList())),
                        bookingDtoMapper.toBookingDtoWithBookerId(lastBookingsListsMap.getOrDefault(item.getId(), null)),
                        bookingDtoMapper.toBookingDtoWithBookerId(nextBookingsListMap.getOrDefault(item.getId(), null))))
                .sorted(Comparator.comparingLong(ItemDtoWithComments::getId))
                .collect(Collectors.toList()); */
    }

    @Transactional
    @Override
    public Comment addComment(long userId, long itemId, Comment comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещи с id {} не найдено", itemId);
                    return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
                });
        checkIfUserRentedItem(user, item);
        comment.setAuthor(user);
        comment.setItem(item);
        return commentRepository.save(comment);
    }

    @Override
    public List<Item> search(long userId, String text, int page, int size) {
        if (text.isBlank()) {
            log.error("поле text содержит пустую строку");
            return new ArrayList<>();
        }
        return itemRepository.findAllContainingTextWithAvailableStatus(text, PageRequest.of(page, size));
    }

    @Override
    public List<Item> findItemsForRequest(long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).get();
        return itemRepository.findAllByRequest(itemRequest);
    }

    @Override
    public List<Item> findItemsForRequests(List<ItemRequest> requests) {
        return itemRepository.findAllByRequestIn(requests);
    }

    private void checkIfOwnerUpdates(long userId, Item item) {
        if (item.getOwner().getId() != userId) {
            log.error("Обновить данные вещи может только ее владелец, userId = {}, ownerId = {}", userId, item.getOwner().getId());
            throw new AuthorizationException(String.format("Обновить данные вещи может только ее владелец, userId = %d, ownerId = %d",
                    userId, item.getOwner().getId()));
        }
    }

    private void checkIfUserRentedItem(User user, Item item) {
        if (!bookingService.checkIfUserRentedItem(user, item)) {
            log.error("Для добавления отзыва нужно завершить аренду вещи");
            throw new IllegalArgumentException("Для добавления отзыва нужно завершить аренду вещи");
        }
    }

    private void updateFields(Item savedItem, Item newItem) {
        if (newItem.getName() != null) {
            savedItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            savedItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            savedItem.setAvailable(newItem.getAvailable());
        }
    }
}
