package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    Item add(User user, Item item);

    Item updateItem(long itemId, Item item);

    Item getById(long userId, long itemId);

    List<Item> getAllForUser(long userId);

    List<Item> getAll();

    List<Item> search(long userId, String text);
}
