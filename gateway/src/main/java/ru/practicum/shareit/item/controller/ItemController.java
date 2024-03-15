package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoFromUser;
import ru.practicum.shareit.item.dto.ItemDtoFromOrToUser;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.validationGroups.OnCreate;
import ru.practicum.shareit.validationGroups.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @Validated(OnCreate.class)
    @PostMapping
    public ResponseEntity<Object> add(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @Valid @RequestBody ItemDtoFromOrToUser itemDtoFromOrToUser) {
        return itemClient.postItem(userId, itemDtoFromOrToUser);
    }

    @Validated(OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody ItemDtoFromOrToUser itemDtoFromOrToUser) {
        System.out.println("GATEWAY");
        return itemClient.updateItem(userId, itemId, itemDtoFromOrToUser);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId) {
        System.out.println("GATEWAY");
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        System.out.println("GATEWAY");
        return itemClient.getItemsForUser(userId, from, size);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> search(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam("text") String text,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        System.out.println("GATEWAY");
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("itemId") @Positive(message = "id не может быть меньше 1") long itemId,
            @Valid @RequestBody CommentDtoFromUser commentDtoFromUser) {
        System.out.println("GATEWAY");
        return itemClient.addComment(userId, itemId, commentDtoFromUser);
    }
}
