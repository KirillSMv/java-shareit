package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemService {
    Item add(long userId, ItemDtoFromOrToUser itemDtoFromOrToUser);

    Item updateItem(long userId, long itemId, Item item);

    ItemDtoWithComments getWithBookingsById(long userId, long itemId);

    Item getById(long itemId);

    List<ItemDtoWithComments> getAllForUserPageable(long userId, int page, int size);

    List<Item> search(long userId, String text, int page, int size);

    Comment addComment(long userId, long itemId, Comment comment);

    List<Item> findItemsForRequest(long requestId);

    List<Item> findItemsForRequests(List<ItemRequest> requests);

}
