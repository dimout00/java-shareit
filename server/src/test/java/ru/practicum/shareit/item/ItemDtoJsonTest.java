package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful", true, 10L);

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id", 1);
        assertThat(result).hasJsonPathStringValue("@.name", "Drill");
        assertThat(result).hasJsonPathStringValue("@.description", "Powerful");
        assertThat(result).hasJsonPathBooleanValue("@.available", true);
        assertThat(result).hasJsonPathNumberValue("@.requestId", 10);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful\",\"available\":true,\"requestId\":10}";

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Powerful");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);
    }
}