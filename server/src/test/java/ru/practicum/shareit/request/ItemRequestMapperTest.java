package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    void toItemRequestDto_shouldMapFields() {
        User requester = new User(1L, "Requester", "r@mail.com");
        ItemRequest request = new ItemRequest(2L, "Need drill", requester, LocalDateTime.now(), new ArrayList<>());

        ItemRequestDto dto = mapper.toDto(request);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getItems()).isNull();
    }
}