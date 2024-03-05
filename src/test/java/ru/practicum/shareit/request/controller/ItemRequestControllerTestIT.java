package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestFromUserDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.dto.ItemRequestToUserDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTestIT {
    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private final Long requesterId = 1L;
    private ItemRequestToUserDto itemRequestToUserDto;
    private ItemRequest itemRequest;
    private User user;
    private User owner;
    private ItemRequestInfoDto itemRequestInfoDto;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        itemRequestToUserDto = new ItemRequestToUserDto(1L, "описание", LocalDateTime.now());
        user = new User(1L, "Vladimir", "vladimir@yandex.ru");
        owner = new User(1L, "Ilya", "ilya@yandex.ru");
        itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        itemRequestInfoDto = new ItemRequestInfoDto(1L, "описание", itemRequest.getCreated(), Collections.emptyList());
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    }

    @Test
    void addTest() throws Exception {
        ItemRequestFromUserDto itemRequestFromUserDto = new ItemRequestFromUserDto("описание");

        when(itemRequestService.add(itemRequestFromUserDto, requesterId)).thenReturn(itemRequestToUserDto);

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requesterId)
                        .content(objectMapper.writeValueAsString(itemRequestFromUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestToUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestToUserDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestToUserDto.getCreated().format(formatter))));
    }

    @Test
    void getByIdTest() throws Exception {
        when(itemRequestService.getById(itemRequest.getId(), requesterId)).thenReturn(itemRequestInfoDto);

        mvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestInfoDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestToUserDto.getCreated().format(formatter))));
    }

    @Test
    void getAllForUserTest() throws Exception {
        when(itemRequestService.getAllForUser(requesterId)).thenReturn(List.of(itemRequestInfoDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestInfoDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestToUserDto.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items", is(Collections.emptyList())));
    }

    @Test
    void testGetAllForUser() throws Exception {
        int from = 0;
        int size = 1;
        when(itemRequestService.getAllFromOtherUsersPageable(owner.getId(), from / size, size)).thenReturn(List.of(itemRequestInfoDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestInfoDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestToUserDto.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items", is(Collections.emptyList())));
    }
}