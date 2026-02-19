package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void toItemDto_shouldMapFields() {
        User owner = new User(1L, "Owner", "o@mail.com");
        Item item = new Item(2L, "Hammer", "Heavy", true, owner, null);

        ItemDto dto = itemMapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Hammer");
        assertThat(dto.getDescription()).isEqualTo("Heavy");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void toItem_shouldMapFields() {
        ItemDto dto = new ItemDto(null, "Hammer", "Heavy", true, 5L);
        User owner = new User(1L, "Owner", "o@mail.com");

        Item item = itemMapper.toEntity(dto, owner, null);

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Hammer");
        assertThat(item.getDescription()).isEqualTo("Heavy");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isNull();
    }
}