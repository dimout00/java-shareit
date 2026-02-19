package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createItem_shouldSaveAndReturn() {
        User owner = new User(null, "Owner", "owner@example.com");
        em.persist(owner);
        em.flush();

        ItemDto dto = new ItemDto(null, "Drill", "Powerful drill", true, null);
        ItemDto saved = itemService.createItem(dto, owner.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Drill");
        assertThat(saved.getRequestId()).isNull();

        Item found = em.find(Item.class, saved.getId());
        assertThat(found.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void updateItem_shouldChangeFields() {
        User owner = new User(null, "Owner", "owner@example.com");
        em.persist(owner);
        Item item = new Item(null, "Old", "Old desc", true, owner, null);
        em.persist(item);
        em.flush();

        ItemDto updateDto = new ItemDto(null, "New", "New desc", false, null);
        ItemDto updated = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("New desc");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void getItemById_shouldIncludeBookingsAndComments() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        LocalDateTime now = LocalDateTime.now();
        Booking past = new Booking(null, now.minusDays(10), now.minusDays(5), item, booker, BookingStatus.APPROVED);
        Booking future = new Booking(null, now.plusDays(5), now.plusDays(10), item, booker, BookingStatus.APPROVED);
        em.persist(past);
        em.persist(future);
        Comment comment = new Comment(null, "Great!", item, booker, now.minusDays(1));
        em.persist(comment);
        em.flush();

        ItemWithBookingsDto found = itemService.getItemById(item.getId(), owner.getId());

        assertThat(found.getId()).isEqualTo(item.getId());
        assertThat(found.getLastBooking()).isEqualToIgnoringNanos(past.getEnd());
        assertThat(found.getNextBooking()).isEqualToIgnoringNanos(future.getStart());
        assertThat(found.getComments()).hasSize(1);
        assertThat(found.getComments().get(0).getText()).isEqualTo("Great!");
    }

    @Test
    void getUserItems_shouldReturnListWithBookings() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item1 = new Item(null, "Item1", "Desc1", true, owner, null);
        Item item2 = new Item(null, "Item2", "Desc2", true, owner, null);
        em.persist(item1);
        em.persist(item2);
        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = new Booking(null, now.minusDays(5), now.minusDays(1), item1, booker, BookingStatus.APPROVED);
        Booking booking2 = new Booking(null, now.plusDays(1), now.plusDays(5), item1, booker, BookingStatus.APPROVED);
        em.persist(booking1);
        em.persist(booking2);
        em.flush();

        List<ItemDto> items = itemService.getUserItems(owner.getId());
        assertThat(items).hasSize(2);

        ItemWithBookingsDto dto1 = itemService.getItemById(item1.getId(), owner.getId());
        assertThat(dto1.getLastBooking()).isEqualToIgnoringNanos(booking1.getEnd());
        assertThat(dto1.getNextBooking()).isEqualToIgnoringNanos(booking2.getStart());
    }

    @Test
    void searchItems_shouldFindByText() {
        User owner = new User(null, "Owner", "owner@example.com");
        em.persist(owner);
        Item item = new Item(null, "Hammer", "Heavy hammer", true, owner, null);
        em.persist(item);
        em.flush();

        List<ItemDto> result = itemService.searchItems("hammer", owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hammer");
    }

    @Test
    void addComment_shouldSave() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        LocalDateTime now = LocalDateTime.now();
        Booking finished = new Booking(null, now.minusDays(10), now.minusDays(5), item, booker, BookingStatus.APPROVED);
        em.persist(finished);
        em.flush();

        CommentDto commentDto = new CommentDto(null, "Perfect!", null, null);
        CommentDto saved = itemService.addComment(item.getId(), commentDto, booker.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Perfect!");
        assertThat(saved.getAuthorName()).isEqualTo("Booker");

        Comment found = em.find(Comment.class, saved.getId());
        assertThat(found).isNotNull();
    }

    @Test
    void addComment_withoutBooking_shouldThrow() {
        User owner = new User(null, "Owner", "owner@example.com");
        User user = new User(null, "User", "user@example.com");
        em.persist(owner);
        em.persist(user);
        Item item = new Item(null, "Item", "Desc", true, owner, null);
        em.persist(item);
        em.flush();

        CommentDto commentDto = new CommentDto(null, "Bad", null, null);

        assertThatThrownBy(() -> itemService.addComment(item.getId(), commentDto, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("У вас нет завершённого подтверждённого бронирования этой вещи");
    }
}