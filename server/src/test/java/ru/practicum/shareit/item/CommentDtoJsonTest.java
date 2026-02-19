package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws Exception {
        CommentDto dto = new CommentDto(1L, "Great item!", "John", LocalDateTime.now());

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id", 1);
        assertThat(result).hasJsonPathStringValue("@.text", "Great item!");
        assertThat(result).hasJsonPathStringValue("@.authorName", "John");
        assertThat(result).hasJsonPathStringValue("@.created");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"John\",\"created\":\"2025-01-01T12:00:00\"}";

        CommentDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great item!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
    }
}