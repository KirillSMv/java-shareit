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

    List<Item> findAllByOwner(User owner);

    @Query("select it " +
            "from Item as it " +
            "join fetch it.owner " +
            "where it.available = true " +
            "and (lower(it.description) like lower(?1) " +
            "or lower(it.name) like lower(?1))")
    List<Item> findAllContainingTextWithAvailableStatus(String text);

    @Query("select it from Item it " +
            "join fetch it.owner " +
            "where it.id = ?1")
    Optional<Item> findByIdJoinFetchOwner(long itemId);
}