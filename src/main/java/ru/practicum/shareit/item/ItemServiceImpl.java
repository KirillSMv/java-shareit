package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    /*
    Скажи, пожалуйста, насколько считается корректным внедрять сторонние классы в сервис (как в данном случае)?
    я это делаю, чтобы при создании вещи в ее поле сразу сохранить владельца - user, но я знаю, что по возможности
    нужно не создавать лишние зависимости/

    И если внедрять, то лучше поддерживать взаимодействие с сервисом - сервис внедрять в сервис как здесь,
    или можно внедрить и dao в dao (dao в сервис)?
     */


    @Override
    public Item add(long userId, Item item) {
        User user = userService.getById(userId);
        return itemStorage.add(user, item);
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        return itemStorage.updateItem(userId, itemId, item);
    }

    @Override
    public Item getById(long userId, long itemId) {
        return itemStorage.getById(userId, itemId);
    }

    @Override
    public List<Item> getAll(long userId) {
        return itemStorage.getAll(userId);
    }

    @Override
    public List<Item> search(long userId, String text) {
        return itemStorage.search(userId, text);
    }
}
