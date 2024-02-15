package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.booking.Review;
import ru.practicum.shareit.user.model.User;

import java.util.Set;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available; //доступна ли для аренды
    private User owner;
    private Long daysRented; //сколько дней вещь была в аренде (опциональное поле, если будет целесообразно добавить)
    private Set<Review> reviews; //после бронирования можно добавить отзыв (опциональное поле)
}
