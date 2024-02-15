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
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.id  = ?1")
    Optional<Booking> findByIdJoinFetch(long bookingId);

    @Query("select bk from Booking as bk " +
            "where bk.item = ?1 " +
            "and ((bk.start between ?2 and ?3) " +
            "or (bk.end between ?2 and ?3) " +
            "or (bk.start < ?2 and bk.end > ?3))")
    Optional<Booking> findIfBookingTimeCrossed(Item item, LocalDateTime start, LocalDateTime end);


    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.booker.id = ?1 " +
            "order by bk.start DESC")
    List<Booking> findAllByBooker(long bookerId);

    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.booker = ?1 " +
            "and bk.status = ?2")
    List<Booking> findAllByBookerAndStatusEquals(User booker, Status status);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.booker = ?1 " +
            "and bk.end < CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllPastBookingsForUser(User booker);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.booker = ?1 " +
            "and bk.start < CURRENT_TIMESTAMP " +
            "and bk.end > CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllCurrentBookingsForUser(User booker);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.booker = ?1 " +
            "and bk.start > CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllFutureBookingsForUser(User booker);


    //Owner requests
    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item.owner = ?1 " +
            "order by bk.start DESC")
    List<Booking> findAllBookingsForUserItems(User owner);

    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item.owner = ?1 " +
            "and bk.status = ?2 " +
            "order by bk.start DESC")
    List<Booking> findAllBookingsForUserItemsAndStatusEquals(User owner, Status status);

    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item.owner = ?1 " +
            "and bk.end < CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllPastBookingsForUserItems(User owner);


    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item.owner = ?1 " +
            "and bk.start < CURRENT_TIMESTAMP " +
            "and bk.end > CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllCurrentBookingsForUserItems(User owner);

    @Query("select bk from Booking as bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item.owner = ?1 " +
            "and bk.start > CURRENT_TIMESTAMP " +
            "order by bk.start DESC")
    List<Booking> findAllFutureBookingsForUserItems(User owner);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item = ?1 " +
            "and bk.status <> 'REJECTED' " +
            "and bk.status <> 'CANCELLED' " +
            "and bk.start < CURRENT_TIMESTAMP " +
            "order by bk.start desc")
    List<Booking> findAllLastBookingsForItem(Item item);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item = ?1 " +
            "and bk.status <> 'REJECTED' " +
            "and bk.status <> 'CANCELLED' " +
            "and bk.start > CURRENT_TIMESTAMP " +
            "order by bk.start asc")
    List<Booking> findAllNextBookingsForItem(Item item);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item IN ?1 " +
            "and bk.status <> 'REJECTED' " +
            "and bk.status <> 'CANCELLED' " +
            "and bk.start < CURRENT_TIMESTAMP " +
            "order by bk.start desc")
    List<Booking> findAllLastBookingsForItems(List<Item> items);

    @Query("select bk from Booking bk " +
            "join fetch bk.item " +
            "join fetch bk.booker " +
            "where bk.item IN ?1 " +
            "and bk.status <> 'REJECTED' " +
            "and bk.status <> 'CANCELLED' " +
            "and bk.start > CURRENT_TIMESTAMP " +
            "order by bk.start asc")
    List<Booking> findAllNextBookingsForItems(List<Item> items);

    @Query("select bk from Booking bk " +
            "where bk.booker = ?1 " +
            "and bk.item = ?2 " +
            "and bk.end < CURRENT_TIMESTAMP")
    List<Booking> findAllForUserAndItemInThePast(User user, Item item);
}
