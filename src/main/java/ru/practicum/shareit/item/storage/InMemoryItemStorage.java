package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
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
    public List<Item> search(long userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void checkIfItemExistsByName(String name) {
        if (items.values().stream().anyMatch(item -> item.getName().equals(name))) {
            log.error("Вещь с таким именем - {} уже добавлена", name);
            throw new ObjectAlreadyExistsException(String.format("Вещь с таким именем - %s уже добавлена", name));
        }
    }

    @Override
    public void checkIfExistsById(long itemId) {
        if (items.values().stream().noneMatch(item -> item.getId().equals(itemId))) {
            log.error("Вещи с id = {} не существует", itemId);
            throw new ObjectNotFoundException(String.format("Вещи с id = %d не существует", itemId));
        }
    }

    @Override
    public void checkIfItemWithNameAlreadyExists(long itemId, Item item) {
        if (items.values().stream().filter(it -> !it.getId().equals(itemId))
                .anyMatch(it -> it.getName().equals(item.getName()))) {
            log.error("Вещь с названием - {} уже существует", item.getName());
            throw new ObjectAlreadyExistsException(String.format("Вещь с названием - %s уже существует",
                    item.getName()));
        }
    }

    private long getUniqueId() {
        return ++uniqueId;
    }
}