package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner(User owner, Pageable pageable);

    @Query("select it " +
            "from Item as it " +
            "where it.available = true " +
            "and (lower(it.description) like lower(CONCAT('%', ?1, '%')) " +
            "or lower(it.name) like lower(CONCAT('%', ?1, '%')))")
    List<Item> findAllContainingTextWithAvailableStatus(String text, Pageable pageable);

    List<Item> findAllByRequest(ItemRequest itemRequest);

    List<Item> findAllByRequestIn(List<ItemRequest> itemRequests);
}