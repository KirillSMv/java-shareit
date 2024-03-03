package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"db.name=shareItTest", "spring.sql.init.schema-locations=classpath:schema_test.sql"})
class BookingRepositoryIT {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private User owner;
    private Item item;
    private ItemRequest itemRequest;
    private Booking booking;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Kirill");
        user.setEmail("kirill@yandex.ru");
        testEntityManager.persist(user);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(user);
        testEntityManager.persist(itemRequest);


        owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        testEntityManager.persist(owner);


        item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setRequest(itemRequest);
        item.setOwner(owner);
        item.setAvailable(true);
        testEntityManager.persist(item);

        booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);
    }


    @Test
    void findIfBookingTimeCrossed() {
        LocalDateTime start = LocalDateTime.of(2025, 10, 20, 10, 20, 10);
        LocalDateTime end = LocalDateTime.of(2025, 10, 20, 11, 20, 10);

        Optional<Booking> bookingOptional = bookingRepository.findIfBookingTimeCrossed(item, start, end);

        assertThat(bookingOptional).isPresent();
        assertThat(bookingOptional.get().getItem()).isEqualTo(booking.getItem());
    }


    @Test
    void findAllByBookerOrderByStartDesc() {
        int page = 0;
        int size = 1;

        List<Booking> bookings = bookingRepository.findAllByBookerOrderByStartDesc(user, PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByBookerAndStatus() {
        int page = 0;
        int size = 1;

        List<Booking> bookings = bookingRepository.findAllByBookerAndStatus(user, Status.WAITING, PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByBookerAndEndBeforeOrderByStartDesc() {
        int page = 0;
        int size = 1;

        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2022, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc() {
        int page = 0;
        int size = 1;

        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user,
                LocalDateTime.now(),
                LocalDateTime.now(),
                PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByBookerAndStartAfterOrderByStartDesc() {
        int page = 0;
        int size = 1;
        List<Booking> bookings = bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(user,
                LocalDateTime.now(),
                PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByItemOwnerOrderByStartDesc() {
        int page = 0;
        int size = 1;
        List<Booking> bookings = bookingRepository.findAllByItemOwnerOrderByStartDesc(owner,
                PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByItemOwnerAndStatusOrderByStartDesc() {
        int page = 0;
        int size = 1;
        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(owner, Status.WAITING,
                PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByItemOwnerAndEndBeforeOrderByStartDesc() {
        int page = 0;
        int size = 1;
        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2023, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(owner, LocalDateTime.now(),
                PageRequest.of(page, size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc() {
        int page = 0;
        int size = 1;
        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner,
                LocalDateTime.now(),
                LocalDateTime.now(),
                PageRequest.of(page,
                        size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findAllByItemOwnerAndStartAfterOrderByStartDesc() {
        int page = 0;
        int size = 1;
        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(owner,
                LocalDateTime.now(),
                PageRequest.of(page,
                        size));

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }

    @Test
    void findFirstByItemAndStatusNotInAndStartBeforeOrderByStartDesc() {
        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        Booking resultBooking = bookingRepository.findFirstByItemAndStatusNotInAndStartBeforeOrderByStartDesc(item,
                List.of(Status.REJECTED),
                LocalDateTime.now());

        assertThat(resultBooking.getId()).isNotNull();
        assertThat(resultBooking.getBooker()).isEqualTo(booking.getBooker());
        assertThat(resultBooking.getItem()).isEqualTo(booking.getItem());
        assertThat(resultBooking.getStart()).isEqualTo(booking.getStart().toString());
        assertThat(resultBooking.getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(resultBooking.getStatus()).isEqualTo(booking.getStatus());
    }

    @Test
    void findFirstByItemAndStatusNotInAndStartAfterOrderByStartAsc() {
        Booking resultBooking = bookingRepository.findFirstByItemAndStatusNotInAndStartAfterOrderByStartAsc(item,
                List.of(Status.REJECTED),
                LocalDateTime.now());

        assertThat(resultBooking.getId()).isNotNull();
        assertThat(resultBooking.getBooker()).isEqualTo(booking.getBooker());
        assertThat(resultBooking.getItem()).isEqualTo(booking.getItem());
        assertThat(resultBooking.getStart()).isEqualTo(booking.getStart().toString());
        assertThat(resultBooking.getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(resultBooking.getStatus()).isEqualTo(booking.getStatus());
    }

    @Test
    void countByBookerAndItemAndEndBefore() {
        booking = new Booking();
        booking.setStart(LocalDateTime.of(2022, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2023, 10, 20, 11, 10, 10));
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingRepository.save(booking);

        long count = bookingRepository.countByBookerAndItemAndEndBefore(user, item,
                LocalDateTime.now());

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findAllUnion() {
        int page = 0;
        int size = 1;
        List<Booking> bookings = bookingRepository.findAllUnion(List.of(item),
                LocalDateTime.now(),
                PageRequest.of(page,
                        size));

        assertThat(bookings.get(0).getId()).isNotNull();
        assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart().toString());
        assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd().toString());
        assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
        assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
    }
}