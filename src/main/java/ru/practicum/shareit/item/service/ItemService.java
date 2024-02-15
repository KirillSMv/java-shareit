package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item add(long userId, Item item);

    Item updateItem(long userId, long itemId, Item item);

    ItemDtoWithComments getWithBookingsById(long userId, long itemId);

    Item getById(long itemId);

    List<ItemDtoWithComments> getAllForUser(long userId);

    List<Item> search(long userId, String text);

    Comment addComment(long userId, long itemId, Comment comment);
}
