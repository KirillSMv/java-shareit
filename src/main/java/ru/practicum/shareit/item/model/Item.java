package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Data
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY) //todo CASCADE?
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY) //todo CASCADE?
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    @ToString.Exclude
    private ItemRequest request;
}


// todo private Long daysRented; //сколько дней вещь была в аренде (опциональное поле, если будет целесообразно добавить)
//todo private Set<Review> reviews; //после бронирования можно добавить отзыв (опциональное поле)

