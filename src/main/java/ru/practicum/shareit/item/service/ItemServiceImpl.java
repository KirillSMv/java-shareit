package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errorHandler.AuthorizationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public Item add(long userId, Item item) {
        userService.getById(userId);
        itemStorage.checkIfItemExistsByName(item.getName());
        User user = userService.getById(userId);
        item.setOwner(user);
        return itemStorage.add(user, item);
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        itemStorage.checkIfExistsById(itemId);
        itemStorage.checkIfItemWithNameAlreadyExists(itemId, item);

        Item itemForUpdate = getById(userId, itemId);
        checkIfOwnerUpdates(userId, itemForUpdate);
        updateFields(itemForUpdate, item);

        return itemStorage.updateItem(itemId, itemForUpdate);
    }

    @Override
    public Item getById(long userId, long itemId) {
        return itemStorage.getById(userId, itemId);
    }

    @Override
    public List<Item> getAllForUser(long userId) {
        return itemStorage.getAllForUser(userId);
    }

    @Override
    public List<Item> search(long userId, String text) {
        return itemStorage.search(userId, text);
    }

    private void checkIfOwnerUpdates(long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Обновить данные вещи может только ее владелец");
            throw new AuthorizationException("Обновить данные вещи может только ее владелец");
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
