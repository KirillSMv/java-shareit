package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemDtoMapper {
    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        return itemDto;
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
