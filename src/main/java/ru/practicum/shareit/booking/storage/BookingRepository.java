package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select bk from Booking as bk " +
            "where bk.item = ?1 " +
            "and ((bk.start between ?2 and ?3) " +
            "or (bk.end between ?2 and ?3) " +
            "or (bk.start < ?2 and bk.end > ?3))")
    Optional<Booking> findIfBookingTimeCrossed(Item item, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerOrderByStartDesc(User booker);

    List<Booking> findAllByBookerAndStatus(User booker, Status status);

    List<Booking> findAllByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime currentTime);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime currentTime,
                                                                           LocalDateTime currentTimeSecondParameter);

    List<Booking> findAllByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime currentTime);

    List<Booking> findAllByItemOwnerOrderByStartDesc(User owner);


    List<Booking> findAllByItemOwnerAndStatusOrderByStartDesc(User owner, Status status);


    List<Booking> findAllByItemOwnerAndEndBeforeOrderByStartDesc(User owner, LocalDateTime currentTime);

    List<Booking> findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(User owner, LocalDateTime currentTime,
                                                                              LocalDateTime currentTimeSecondParameter);

    List<Booking> findAllByItemOwnerAndStartAfterOrderByStartDesc(User owner, LocalDateTime currentTime);

    List<Booking> findAllByItemAndStatusNotInAndStartBeforeOrderByStartDesc(Item item,
                                                                            List<Status> statuses,
                                                                            LocalDateTime currentTime);

    List<Booking> findAllByItemAndStatusNotInAndStartAfterOrderByStartAsc(Item item,
                                                                          List<Status> statuses,
                                                                          LocalDateTime currentTime);

    long countByBookerAndItemAndEndBefore(User user, Item item, LocalDateTime currentTime);

/*    @Query(value = "SELECT * " +
            "FROM bookings AS bk " +
            "WHERE bk.item_id IN (?1) " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time < ?2 " +
            "ORDER BY bk.start_time DESC " +
            "LIMIT 1", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartBeforeOrderByStartDesc(List<Item> items, LocalDateTime currentTime);*/

/*    @Query(value = "SELECT bks.item_id, bks.id, bks.start_time, bks.end_time, bks.user_id, bks.status " +
            "FROM items AS it " +
            "INNER JOIN LATERAL " +
            "(SELECT bk.id, bk.item_id, bk.start_time, bk.end_time, bk.user_id, bk.status " +
            "FROM bookings as bk " +
            "WHERE it.id = bk.item_id " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time < ?2 " +
            "ORDER BY bk.start_time DESC " +
            "LIMIT 1) AS bks ON true " +
            "WHERE it.id IN (?1)", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartBeforeOrderByStartDesc(List<Item> items, LocalDateTime currentTime);*/

    @Query(value = "SELECT DISTINCT ON (item_id)" +
            "bk.id, bk.item_id, bk.start_time, bk.end_time, bk.user_id, bk.status " +
            "FROM bookings as bk " +
            "WHERE bk.item_id IN (?1) " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time < ?2 " +
            "ORDER BY bk.item_id, bk.start_time DESC ", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartBeforeOrderByStartDesc(List<Item> items, LocalDateTime currentTime);


/*            "AND id IN " +
            "(SELECT DISTINCT ON bk.id " +
            "FROM bookings as bk " +
            "WHERE bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time < ?2 " +
            "ORDER BY bk.start_time DESC", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartBeforeOrderByStartDesc(List<Item> items, LocalDateTime currentTime);*/

/*    @Query(value = "SELECT * " +
            "FROM bookings AS bk " +
            "WHERE bk.item_id IN (?1) " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time > ?2 " +
            "ORDER BY bk.start_time ASC " +
            "LIMIT 1", nativeQuery = true)
       List<Booking> findAllByItemInAndStatusNotInAndStartAfterOrderByStartAsc(List<Item> items, LocalDateTime currentTime);*/

/*    @Query(value = "SELECT bks.item_id, bks.id, bks.start_time, bks.end_time, bks.user_id, bks.status " +
            "FROM items AS it " +
            "INNER JOIN LATERAL " +
            "(SELECT bk.id, bk.item_id, bk.start_time, bk.end_time, bk.user_id, bk.status " +
            "FROM bookings as bk " +
            "WHERE it.id = bk.item_id " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time > ?2 " +
            "ORDER BY bk.start_time ASC " +
            "LIMIT 1) AS bks ON true " +
            "WHERE it.id IN (?1)", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartAfterOrderByStartAsc(List<Item> items, LocalDateTime currentTime);*/

/*        @Query(value = "SELECT * " +
            "FROM bookings as bks " +
            "WHERE bks.item_id IN (?1) " +
            "AND bks.id IN " +
            "(SELECT bk.id " +
            "FROM bookings as bk " +
            "WHERE bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time > ?2 " +
            "ORDER BY bk.start_time ASC " +
            "LIMIT 1)", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartAfterOrderByStartAsc(List<Item> items, LocalDateTime currentTime);*/

    @Query(value = "SELECT DISTINCT ON (item_id)" +
            "bk.id, bk.item_id, bk.start_time, bk.end_time, bk.user_id, bk.status " +
            "FROM bookings as bk " +
            "WHERE bk.item_id IN (?1) " +
            "AND bk.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bk.start_time > ?2 " +
            "ORDER BY bk.item_id, bk.start_time ASC ", nativeQuery = true)
    List<Booking> findAllByItemInAndStatusNotInAndStartAfterOrderByStartAsc(List<Item> items, LocalDateTime currentTime);
}
