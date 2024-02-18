package ru.practicum.shareit.item.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemDtoMapper {
    public static Item toItem(ItemDtoFromUser itemDtoFromUser) {
        Item item = new Item();
        item.setId(itemDtoFromUser.getId());
        item.setName(itemDtoFromUser.getName());
        item.setDescription(itemDtoFromUser.getDescription());
        item.setAvailable(itemDtoFromUser.getAvailable());
        return item;
    }

    public static ItemDtoFromUser toDto(Item item) {
        ItemDtoFromUser itemDtoFromUser = new ItemDtoFromUser();
        itemDtoFromUser.setId(item.getId());
        itemDtoFromUser.setName(item.getName());
        itemDtoFromUser.setDescription(item.getDescription());
        itemDtoFromUser.setAvailable(item.getAvailable());
        return itemDtoFromUser;
    }

    public static ItemDtoWithComments toItemDtoWithComments(Item item,
                                                            List<CommentDto> comments,
                                                            BookingDto lastBooking,
                                                            BookingDto nextBooking) {
        ItemDtoWithComments itemDtoWithComments = new ItemDtoWithComments();
        itemDtoWithComments.setId(item.getId());
        itemDtoWithComments.setName(item.getName());
        itemDtoWithComments.setDescription(item.getDescription());
        itemDtoWithComments.setAvailable(item.getAvailable());
        itemDtoWithComments.setLastBooking(lastBooking);
        itemDtoWithComments.setNextBooking(nextBooking);
        itemDtoWithComments.setComments(comments);
        return itemDtoWithComments;
    }
}
