package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

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
@Validated
public class ItemController {
    private final ItemService itemService;

    @Validated(OnCreate.class)
    @PostMapping
    public ItemDto add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @Valid @RequestBody ItemDto itemDto) {
        Item item = itemService.add(userId, ItemDtoMapper.toItem(itemDto));
        return ItemDtoMapper.toDto(item);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody ItemDto itemDto) {
        Item item = itemService.updateItem(userId, itemId, ItemDtoMapper.toItem(itemDto));
        return ItemDtoMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId) {
        return ItemDtoMapper.toDto(itemService.getById(userId, itemId));
    }

    @GetMapping
    public List<ItemDto> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        return itemService.getAllForUser(userId).stream().map(ItemDtoMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam("text") String text) {
        return itemService.search(userId, text).stream().map(ItemDtoMapper::toDto).collect(Collectors.toList());
    }
}
