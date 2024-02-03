package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item add(long userId, Item item);

    Item updateItem(long userId, long itemId, Item item);

    Item getById(long userId, long itemId);

    List<Item> getAllForUser(long userId);

    List<Item> search(long userId, String text);
}
