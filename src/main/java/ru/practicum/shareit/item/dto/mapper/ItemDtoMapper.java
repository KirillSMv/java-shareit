package ru.practicum.shareit.item.dto.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Component
public class ItemDtoMapper {
    public Item toItem(ItemDtoFromOrToUser itemDtoFromOrToUser) {
        Item item = new Item();
        item.setId(itemDtoFromOrToUser.getId());
        item.setName(itemDtoFromOrToUser.getName());
        item.setDescription(itemDtoFromOrToUser.getDescription());
        item.setAvailable(itemDtoFromOrToUser.getAvailable());
        return item;
    }

    public ItemDtoFromOrToUser toDto(Item item) {
        ItemDtoFromOrToUser itemDtoFromOrToUser = new ItemDtoFromOrToUser();
        itemDtoFromOrToUser.setId(item.getId());
        itemDtoFromOrToUser.setName(item.getName());
        itemDtoFromOrToUser.setDescription(item.getDescription());
        itemDtoFromOrToUser.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDtoFromOrToUser.setRequestId(item.getRequest().getId());
        }
        return itemDtoFromOrToUser;
    }

    public ItemDtoWithComments toItemDtoWithComments(Item item,
                                                     List<CommentDto> comments,
                                                     BookingDto lastBookingDto,
                                                     BookingDto nextBookingDto) {
        ItemDtoWithComments itemDtoWithComments = new ItemDtoWithComments();
        itemDtoWithComments.setId(item.getId());
        itemDtoWithComments.setName(item.getName());
        itemDtoWithComments.setDescription(item.getDescription());
        itemDtoWithComments.setAvailable(item.getAvailable());
        itemDtoWithComments.setLastBooking(lastBookingDto);
        itemDtoWithComments.setNextBooking(nextBookingDto);
        itemDtoWithComments.setComments(comments);
        return itemDtoWithComments;
    }
}
