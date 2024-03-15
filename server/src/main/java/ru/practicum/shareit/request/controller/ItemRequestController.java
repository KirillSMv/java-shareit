package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestToUserDto add(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody ItemRequestFromUserDto itemRequestFromUserDto) {
        return itemRequestService.add(itemRequestFromUserDto, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("requestId") long requestId) {
        return itemRequestService.getById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestInfoDto> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAllForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return itemRequestService.getAllFromOtherUsersPageable(userId, from / size, size);
    }

}
