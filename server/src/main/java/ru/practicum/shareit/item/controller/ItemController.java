package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.dto.ItemDtoWithComments;
import ru.practicum.shareit.item.dto.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.dto.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final ItemDtoMapper itemDtoMapper;
    private final CommentDtoMapper commentDtoMapper;

    @Validated(OnCreate.class)
    @PostMapping
    public ItemDtoFromOrToUser add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @Valid @RequestBody ItemDtoFromOrToUser itemDtoFromOrToUser) {
        Item item = itemService.add(userId, itemDtoFromOrToUser);
        return itemDtoMapper.toDto(item);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDtoFromOrToUser updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody ItemDtoFromOrToUser itemDtoFromOrToUser) {
        Item item = itemService.updateItem(userId, itemId, itemDtoMapper.toItem(itemDtoFromOrToUser));
        return itemDtoMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithComments getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId) {
        return itemService.getWithBookingsById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoWithComments> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {

        return itemService.getAllForUserPageable(userId, from / size, size);
    }

    @GetMapping(value = "/search", params = "text")
    public List<ItemDtoFromOrToUser> search(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam("text") String text,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return itemService.search(userId, text, from / size, size).stream().map(itemDtoMapper::toDto).collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody CommentDtoFromUser commentDtoFromUser) {
        Comment comment = new Comment();
        comment.setText(commentDtoFromUser.getText());
        return commentDtoMapper.toCommentDto(itemService.addComment(userId, itemId, comment));
    }
}
