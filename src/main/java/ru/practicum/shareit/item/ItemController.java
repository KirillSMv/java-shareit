package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;


    @PostMapping
    public ItemDto add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @Valid @RequestBody ItemDto itemDto) {
        Item item = itemService.add(userId, ItemDtoMapper.toItemMapper(itemDto));
        return ItemDtoMapper.toDtoMapper(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") long itemId,
            @RequestBody ItemDto itemDto) {
        Item item = itemService.updateItem(userId, itemId, ItemDtoMapper.toItemMapper(itemDto));
        return ItemDtoMapper.toDtoMapper(item);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") long itemId) {
        return ItemDtoMapper.toDtoMapper(itemService.getById(userId, itemId));
    }

    @GetMapping
    public List<ItemDto> getAll(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        return itemService.getAll(userId).stream().map(ItemDtoMapper::toDtoMapper).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam("text") String text) {
        return itemService.search(userId, text).stream().map(ItemDtoMapper::toDtoMapper).collect(Collectors.toList());
    }
}
