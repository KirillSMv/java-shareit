package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long uniqueId;

    @Override
    public Item add(User user, Item item) {
        item.setId(getUniqueId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(long itemId, Item itemForUpdate) {
        items.put(itemId, itemForUpdate);
        return itemForUpdate;
    }

    @Override
    public Item getById(long userId, long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getAllForUser(long userId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(userId)).collect(Collectors.toList());
    }

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> search(long userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    private long getUniqueId() {
        return ++uniqueId;
    }
}