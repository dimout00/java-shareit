package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto dto = new UserDto(1L, "John", "john@example.com");

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id", 1);
        assertThat(result).hasJsonPathStringValue("@.name", "John");
        assertThat(result).hasJsonPathStringValue("@.email", "john@example.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"John\",\"email\":\"john@example.com\"}";

        UserDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }
}