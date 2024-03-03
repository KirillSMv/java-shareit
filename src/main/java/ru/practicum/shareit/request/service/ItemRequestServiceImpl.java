package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestToUserDto add(ItemRequestFromUserDto itemRequestFromUserDto, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestFromUserDto);
        itemRequest.setRequester(user);
        return itemRequestMapper.toItemRequestForUserDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public ItemRequestInfoDto getById(long requestId, long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
                    log.error("Запроса с id {} не найдено", requestId);
                    return new ObjectNotFoundException(String.format("Запроса с id %d не найдено", requestId));
                }
        );
        var items = itemService.findItemsForRequest(requestId);
        return itemRequestMapper.toItemRequestInfoDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestInfoDto> getAllForUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        var itemRequests = itemRequestRepository.findAllByRequesterOrderByCreatedDesc(user);
        if (itemRequests.isEmpty()) {
            return new ArrayList<>();
        }
        return getItemRequestsDtoWithItemsResponse(itemRequests);
    }

    @Override
    public List<ItemRequestInfoDto> getAllFromOtherUsersPageable(
            long userId,
            int page,
            int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        var itemRequests = itemRequestRepository.findAllByRequesterNotOrderByCreatedDesc(user,
                PageRequest.of(page, size));
        if (itemRequests.isEmpty()) {
            return new ArrayList<>();
        }
        return getItemRequestsDtoWithItemsResponse(itemRequests);
    }

    private List<ItemRequestInfoDto> getItemRequestsDtoWithItemsResponse(List<ItemRequest> itemRequests) {
        var items = itemService.findItemsForRequests(itemRequests);

        Map<Long, List<Item>> requestItemsMap = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        List<ItemRequestInfoDto> resultList = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            resultList.add(itemRequestMapper.toItemRequestInfoDto(itemRequest,
                    requestItemsMap.getOrDefault(itemRequest.getId(), Collections.emptyList())));
        }
        return resultList;

    }
}
