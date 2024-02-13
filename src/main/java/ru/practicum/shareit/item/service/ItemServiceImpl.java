package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.errorHandler.AuthorizationException;
import ru.practicum.shareit.exceptions.ObjectAlreadyExistsException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Item add(long userId, Item item) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
        checkIfItemExistsByName(item.getName());
        item.setOwner(user);
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        Item savedItem = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId)));
        checkIfDifferentItemWithNameExists(itemId, item.getName());
        checkIfOwnerUpdates(userId, savedItem);
        updateFields(savedItem, item);
        return savedItem;
    }

    @Override
    public Item getById(long userId, long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException(String.format("Вещи с id %d не найдено", itemId)));
    }

    @Override
    public List<Item> getAllForUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId)));
        return itemRepository.findAllByOwner(user);
    }

    @Override
    public List<Item> search(long userId, String text) {
        if (text.isBlank()) {
            log.error("text содержит пустую строку");
            return new ArrayList<>();
        }
        return itemRepository.findAllContainingTextAndAvailable("%" + text + "%");
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

    public void checkIfItemExistsByName(String name) {
        if (itemRepository.findByName(name).isPresent()) {
            log.error("Вещь с именем {} уже существует", name);
            throw new ObjectAlreadyExistsException(String.format("Вещь с именем %s уже существует", name));
        }
    }

    private void checkIfDifferentItemWithNameExists(long itemId, String name) {
        if (itemRepository.findByName(name).stream().anyMatch(it -> it.getId() != itemId)) {
            log.error("Вещь с именем {} уже существует", name);
            throw new ObjectAlreadyExistsException(String.format("Вещь с именем %s уже существует", name));
        }
    }
}
