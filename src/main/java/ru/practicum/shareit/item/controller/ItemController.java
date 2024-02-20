package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.dto.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.dto.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @Validated(OnCreate.class)
    @PostMapping
    public ItemDtoFromUser add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @Valid @RequestBody ItemDtoFromUser itemDtoFromUser) {
        Item item = itemService.add(userId, ItemDtoMapper.toItem(itemDtoFromUser));
        return ItemDtoMapper.toDto(item);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDtoFromUser updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody ItemDtoFromUser itemDtoFromUser) {
        Item item = itemService.updateItem(userId, itemId, ItemDtoMapper.toItem(itemDtoFromUser));
        return ItemDtoMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithComments getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId) {
        return itemService.getWithBookingsById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoWithComments> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        return itemService.getAllForUser(userId);
    }

    @GetMapping(value = "/search", params = "text")
    public List<ItemDtoFromUser> search(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam("text") String text) {
        return itemService.search(userId, text).stream().map(ItemDtoMapper::toDto).collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody CommentDtoFromUser commentDtoFromUser) {
        Comment comment = new Comment();
        comment.setText(commentDtoFromUser.getText());
        return CommentDtoMapper.toCommentDto(itemService.addComment(userId, itemId, comment));
    }
}
