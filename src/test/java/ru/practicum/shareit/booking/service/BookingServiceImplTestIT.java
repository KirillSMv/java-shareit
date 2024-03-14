package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest(
        properties = {"db.name=shareItTest", "spring.sql.init.schema-locations=classpath:schema_test.sql"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTestIT {

    private final BookingServiceImpl bookingService;
    private final EntityManager entityManager;

    @Test
    void processBooking() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        entityManager.persist(booking);

        Booking bookingAfterProcessing = bookingService.processBooking(owner.getId(), booking.getId(), true);

        Assertions.assertThat(bookingAfterProcessing.getId()).isNotNull();
        Assertions.assertThat(bookingAfterProcessing.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void getBookingsForUserTest_thenAllRequested_thenReturnAllBookingsForUser() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.ALL;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    void getBookingsForUserTest_whenWaitingRequested_thenReturnListOfBookingsWithWaitingStatus() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.WAITING;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    void getBookingsForUserTest_wheRejectedRequested_thenReturnListOfBookingsWithRejectedStatus() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.REJECTED);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.REJECTED;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    void getBookingsForUserTest_whenCurrentRequested_thenReturnListOfCurrentBookings() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2023, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.REJECTED);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.CURRENT;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    void getBookingsForUserTest_whenFutureRequested_thenReturnListOfFutureBookings() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.REJECTED);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.FUTURE;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    void getBookingsForUserTest_whenPastRequested_thenReturnListOfPastBookings() {
        User user = new User();
        user.setName("Ilya");
        user.setEmail("ilya@yandex.ru");
        entityManager.persist(user);

        User owner = new User();
        owner.setName("Igor");
        owner.setEmail("igor@yandex.ru");
        entityManager.persist(owner);

        Item item = new Item();
        item.setName("имя");
        item.setDescription("описание");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(null);
        entityManager.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2023, 10, 20, 10, 10, 10));
        booking.setEnd(LocalDateTime.of(2023, 10, 20, 11, 10, 10));
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.REJECTED);
        entityManager.persist(booking);

        int page = 0;
        int size = 1;
        BookingState bookingState = BookingState.PAST;

        List<Booking> bookings = bookingService.getBookingsForUser(user.getId(), bookingState, page, size);

        Assertions.assertThat(bookings.size()).isEqualTo(1);
        Assertions.assertThat(bookings.get(0).getId()).isNotNull();
        Assertions.assertThat(bookings.get(0).getItem()).isEqualTo(booking.getItem());
        Assertions.assertThat(bookings.get(0).getStart()).isEqualTo(booking.getStart());
        Assertions.assertThat(bookings.get(0).getEnd()).isEqualTo(booking.getEnd());
        Assertions.assertThat(bookings.get(0).getStatus()).isEqualTo(booking.getStatus());
        Assertions.assertThat(bookings.get(0).getBooker()).isEqualTo(booking.getBooker());
    }
}