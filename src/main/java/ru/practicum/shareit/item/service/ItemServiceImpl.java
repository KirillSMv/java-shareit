package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.AuthorizationException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.dto.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.dto.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

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

    @Transactional
    @Override
    public Item add(long userId, Item item) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {}} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        item.setOwner(user);
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        Item savedItem = itemRepository.findByIdJoinFetchOwner(itemId).orElseThrow(() -> {
            log.error("Вещи с id {} не найдено", itemId);
            return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
        });
        checkIfOwnerUpdates(userId, savedItem);
        updateFields(savedItem, item);
        return savedItem;
    }

    @Override
    public ItemDtoWithComments getWithBookingsById(long userId, long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Вещи с id {} не найдено", itemId);
            return new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId));
        });
        if (userId == item.getOwner().getId()) {
            return ItemDtoMapper.toItemDtoWithComments(item,
                    CommentDtoMapper.toCommentDtoList(commentRepository.findAllByItem(item)),
                    BookingDtoMapper.toBookingDtoWithBookerId(bookingService.getLastOrNextBooking(item, true)),
                    BookingDtoMapper.toBookingDtoWithBookerId(bookingService.getLastOrNextBooking(item, false)));
        } else {
            return ItemDtoMapper.toItemDtoWithComments(item,
                    CommentDtoMapper.toCommentDtoList(commentRepository.findAllByItem(item)), null, null);
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
    public List<ItemDtoWithComments> getAllForUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
        List<Item> items = itemRepository.findAllByOwner(user);

        Map<Long, List<Comment>> commentsMap = commentRepository.findAllByItemIn(items).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        Map<Long, Booking> lastBookingsListsMap = new HashMap<>();
        for (Booking booking : bookingService.findAllLastBookingsForItems(items)) {
            lastBookingsListsMap.put(booking.getItem().getId(), booking);
        }

        Map<Long, Booking> nextBookingsListMap = new HashMap<>();
        for (Booking booking : bookingService.findAllNextBookingsForItems(items)) {
            nextBookingsListMap.put(booking.getItem().getId(), booking);
        }

        return items.stream()
                .map(item -> ItemDtoMapper.toItemDtoWithComments(item,
                        CommentDtoMapper.toCommentDtoList(commentsMap.getOrDefault(item.getId(), Collections.emptyList())),
                        BookingDtoMapper.toBookingDtoWithBookerId(lastBookingsListsMap.getOrDefault(item.getId(), null)),
                        BookingDtoMapper.toBookingDtoWithBookerId(nextBookingsListMap.getOrDefault(item.getId(), null))))
                .sorted(Comparator.comparingLong(ItemDtoWithComments::getId))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Comment addComment(long userId, long itemId, Comment comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {}} не найден", userId);
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
    public List<Item> search(long userId, String text) {
        if (text.isBlank()) {
            log.error("поле text содержит пустую строку");
            return new ArrayList<>();
        }
        return itemRepository.findAllContainingTextWithAvailableStatus(text);
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
