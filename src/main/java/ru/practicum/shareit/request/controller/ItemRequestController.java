package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
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
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestBody @Valid ItemRequestFromUserDto itemRequestFromUserDto) {
        return itemRequestService.add(itemRequestFromUserDto, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestInfoDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @PathVariable("requestId") long requestId) {
        return itemRequestService.getById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestInfoDto> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId) {
        return itemRequestService.getAllForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestInfoDto> getAllForUser(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "id не может быть меньше 1") long userId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return itemRequestService.getAllFromOtherUsersPageable(userId, from / size, size);
    }

}
