package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.ErrorHandler.AuthorizationException;
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
        checkParameters(item);
        checkIfExistsByName(item.getName());

        item.setId(calculate());
        items.put(item.getId(), item);
        item.setOwner(user);
        return item;
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        checkIfExistsById(itemId);
        Item savedItem = items.get(itemId);
        checkIfOwnerUpdates(userId, savedItem);
        checkIfPropertiesNotBlank(item);
        checkIfItemWithNameAlreadyExists(itemId, item);

        updateFields(savedItem, item);
        items.put(itemId, savedItem);
        return savedItem;
    }

    @Override
    public Item getById(long userId, long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getAll(long userId) {
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

    private void checkIfItemWithNameAlreadyExists(long itemId, Item item) {
        if (items.values().stream().anyMatch(it -> it.getName().equals(item.getName()))
                && !items.get(itemId).getName().equals(item.getName())) {
            log.error("Вещь с названием - {} уже существует", item.getName());
            throw new ObjectAlreadyExistsException(String.format("Вещь с названием - %s уже существует",
                    item.getName()));
        }
    }

    private void checkIfOwnerUpdates(long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Обновить данные вещи может только ее владелец");
            throw new AuthorizationException("Обновить данные вещи может только ее владелец");
        }
    }

    private void updateFields(Item savedItem, Item updatedItem) {
        if (updatedItem.getName() != null) {
            savedItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            savedItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            savedItem.setAvailable(updatedItem.getAvailable());
        }
    }

    private long calculate() {
        return ++uniqueId;
    }

    private void checkParameters(Item item) {
        if (item.getName() == null || item.getDescription() == null || item.getAvailable() == null) {
            log.error("Для добавления вещи в шеринг необходимо указать название, " + "добавить описание и статус");
            throw new IllegalArgumentException("Для добавления вещи в шеринг необходимо указать название, " +
                    "добавить описание и статус");
        }
        checkIfPropertiesNotBlank(item);
    }

    private void checkIfPropertiesNotBlank(Item item) {
        if (item.getName() != null) {
            if (item.getName().isBlank()) {
                log.error("Для добавления вещи в шеринг необходимо указать не пустое название");
                throw new IllegalArgumentException("Для добавления вещи в шеринг " +
                        "необходимо указать не пустое название");
            }
        }
        if (item.getDescription() != null) {
            if (item.getDescription().isBlank()) {
                log.error("Для добавления вещи в шеринг необходимо указать не пустое описание");
                throw new IllegalArgumentException("Для добавления вещи в шеринг " +
                        "необходимо указать не пустое описание");
            }
        }
    }

    private void checkIfExistsByName(String name) {
        if (items.values().stream().anyMatch(item -> item.getName().equals(name))) {
            log.error("Вещь с таким именем - {} уже добавлена", name);
            throw new ObjectAlreadyExistsException(String.format("Вещь с таким именем - %s уже добавлена", name));
        }
    }

    private void checkIfExistsById(long id) {
        if (items.get(id) == null) {
            log.error("Вещи с id = {} не существует", id);
            throw new ObjectNotFoundException(String.format("Вещи с id = %d не существует", id));
        }
    }
}
