package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    Item add(User user, Item item);

    Item updateItem(long userId, long itemId, Item item);

    Item getById(long userId, long itemId);

    List<Item> getAll(long userId);

    List<Item> search(long userId, String text);
}
