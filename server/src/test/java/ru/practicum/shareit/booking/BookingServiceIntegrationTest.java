package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createBooking_shouldSave() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        em.flush();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto request = new BookingRequestDto(item.getId(), start, end);

        BookingResponseDto created = bookingService.createBooking(request, booker.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());

        Booking found = em.find(Booking.class, created.getId());
        assertThat(found.getStart()).isEqualTo(start);
        assertThat(found.getEnd()).isEqualTo(end);
    }

    @Test
    void approveBooking_shouldChangeStatus() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        BookingResponseDto approved = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Booking updated = em.find(Booking.class, booking.getId());
        assertThat(updated.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getBookingById_shouldReturn() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        BookingResponseDto found = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingsByBooker_shouldReturnFiltered() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        LocalDateTime now = LocalDateTime.now();
        Booking past = new Booking(null, now.minusDays(5), now.minusDays(2), item, booker, BookingStatus.APPROVED);
        Booking future = new Booking(null, now.plusDays(1), now.plusDays(3), item, booker, BookingStatus.WAITING);
        em.persist(past);
        em.persist(future);
        em.flush();

        List<BookingResponseDto> all = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);
        assertThat(all).hasSize(2);

        List<BookingResponseDto> pastList = bookingService.getUserBookings(booker.getId(), "PAST", 0, 10);
        assertThat(pastList).hasSize(1);
        assertThat(pastList.get(0).getId()).isEqualTo(past.getId());
    }

    @Test
    void getBookingsByOwner_shouldReturnFiltered() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        LocalDateTime now = LocalDateTime.now();
        Booking future = new Booking(null, now.plusDays(1), now.plusDays(3), item, booker, BookingStatus.WAITING);
        em.persist(future);
        em.flush();

        List<BookingResponseDto> all = bookingService.getOwnerBookings(owner.getId(), "ALL", 0, 10);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo(future.getId());
    }
}