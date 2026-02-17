package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createRequest_shouldSave() {
        User requester = new User(null, "Requester", "req@example.com");
        em.persist(requester);
        em.flush();

        ItemRequestDto dto = new ItemRequestDto(null, "Need a drill", null, null, null);
        ItemRequestDto saved = requestService.createRequest(requester.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Need a drill");
        assertThat(saved.getCreated()).isNotNull();

        ItemRequest found = em.find(ItemRequest.class, saved.getId());
        assertThat(found.getRequester().getId()).isEqualTo(requester.getId());
    }

    @Test
    void getUserRequests_shouldReturnListWithItems() {
        User requester = new User(null, "Requester", "req@example.com");
        User owner = new User(null, "Owner", "owner@example.com");
        em.persist(requester);
        em.persist(owner);
        ItemRequest request = new ItemRequest(null, "Need hammer", requester, LocalDateTime.now(), new ArrayList<>());
        em.persist(request);
        Item item = new Item(null, "Hammer", "Heavy hammer", true, owner, request);
        em.persist(item);
        em.flush();
        em.refresh(request); // Обновляем request, чтобы загрузить items

        List<ItemRequestDto> result = requestService.getUserRequests(requester.getId());

        assertThat(result).hasSize(1);
        ItemRequestDto dto = result.get(0);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Hammer");
    }

    @Test
    void getAllRequests_shouldReturnRequestsOfOtherUsers() {
        User requester1 = new User(null, "Req1", "req1@example.com");
        User requester2 = new User(null, "Req2", "req2@example.com");
        User currentUser = new User(null, "Current", "cur@example.com");
        em.persist(requester1);
        em.persist(requester2);
        em.persist(currentUser);
        ItemRequest request1 = new ItemRequest(null, "Request1", requester1, LocalDateTime.now().minusHours(2), new ArrayList<>());
        ItemRequest request2 = new ItemRequest(null, "Request2", requester2, LocalDateTime.now().minusHours(1), new ArrayList<>());
        em.persist(request1);
        em.persist(request2);
        em.flush();

        List<ItemRequestDto> result = requestService.getAllRequests(currentUser.getId(), 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Request2"); // sorted by created desc
    }

    @Test
    void getRequestById_shouldIncludeItems() {
        User requester = new User(null, "Req", "req@example.com");
        User owner = new User(null, "Owner", "owner@example.com");
        em.persist(requester);
        em.persist(owner);
        ItemRequest request = new ItemRequest(null, "Need screwdriver", requester, LocalDateTime.now(), new ArrayList<>());
        em.persist(request);
        Item item = new Item(null, "Screwdriver", "Electric", true, owner, request);
        em.persist(item);
        em.flush();
        em.refresh(request);

        ItemRequestDto found = requestService.getRequestById(requester.getId(), request.getId());

        assertThat(found.getDescription()).isEqualTo("Need screwdriver");
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Screwdriver");
    }
}