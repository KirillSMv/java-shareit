package ru.practicum.shareit;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceTest {
    private Item item;
    private UserDto userDto;
    private ItemDto itemDto;
    private User user;

    private HttpClient client;
    private static final String URL = "http://localhost:8080";
    private static final Gson gson = new Gson();

    private final ItemService itemService;

    {
        client = HttpClient.newHttpClient();

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("name@email.ru");
        user = UserDtoMapper.toUserMapper(userDto);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/users"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userDto, UserDto.class)))
                .build();

        itemDto = new ItemDto();
        itemDto.setName("Отвертка");
        itemDto.setDescription("Аккумуляторная отвертка");
        itemDto.setAvailable(true);
        item = ItemDtoMapper.toItemMapper(itemDto);

        HttpRequest httpRequestForUser = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/items"))
                .headers("Content-Type", "application/json")
                .headers("X-Sharer-User-Id", "1")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(itemDto, ItemDto.class)))
                .build();
        try {
            client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            client.send(httpRequestForUser, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUpdateItemShouldBeOk() {
        item.setName("updatedName");
        Item updatedItem = itemService.updateItem(1, 1, item);
        assertEquals("updatedName", updatedItem.getName());
    }

    @Test
    public void testGetItemByIdShouldBeOk() throws IOException, InterruptedException {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setId(3L);

        HttpRequest httpRequestForUser = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/items"))
                .headers("Content-Type", "application/json")
                .headers("X-Sharer-User-Id", "1")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(itemDto, ItemDto.class)))
                .build();
        client.send(httpRequestForUser, HttpResponse.BodyHandlers.ofString());

        Item savedItem = itemService.getById(1, 3);
        Item initialItem = ItemDtoMapper.toItemMapper(itemDto);
        initialItem.setOwner(user);
        assertEquals(initialItem, savedItem);
    }

    @Test
    public void testGetAllItemsShouldBeOk() {
        List<Item> items = itemService.getAll(1);
        assertEquals(3, items.size());
    }

    @Test
    public void testSerachForItemShouldBeOk() {
        List<Item> items = itemService.search(1, "аккУМУляторная");
        assertEquals(2, items.size());
    }
}

