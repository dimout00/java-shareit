package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestDto.ItemDto item = new ItemRequestDto.ItemDto();
        item.setId(10L);
        item.setName("Drill");
        item.setOwnerId(5L);
        ItemRequestDto dto = new ItemRequestDto(1L, "Need drill", null, LocalDateTime.of(2025, 1, 1, 12, 0), List.of(item));

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id", 1);
        assertThat(result).hasJsonPathStringValue("@.description", "Need drill");
        assertThat(result).hasJsonPathStringValue("@.created", "2025-01-01T12:00:00");
        assertThat(result).hasJsonPathArrayValue("@.items");
        assertThat(result).hasJsonPathNumberValue("@.items[0].id", 10);
        assertThat(result).hasJsonPathStringValue("@.items[0].name", "Drill");
        assertThat(result).hasJsonPathNumberValue("@.items[0].ownerId", 5);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"description\":\"Need drill\",\"created\":\"2025-01-01T12:00:00\",\"items\":[{\"id\":10,\"name\":\"Drill\",\"ownerId\":5}]}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(5L);
    }
}