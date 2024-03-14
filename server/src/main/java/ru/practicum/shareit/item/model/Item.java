package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.request.model.ItemRequest;
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

    @Column
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private Boolean available;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    @ToString.Exclude
    private ItemRequest request;
}