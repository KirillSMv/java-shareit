package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByName(String name);

    List<Item> findAllByOwner(User owner);

    @Query("select it " +
            "from Item as it " +
            "where it.available = true " +
            "and (lower(it.description) like lower(?1) " +
            "or lower(it.name) like lower(?1))")
    List<Item> findAllContainingTextAndAvailable(String text);
}