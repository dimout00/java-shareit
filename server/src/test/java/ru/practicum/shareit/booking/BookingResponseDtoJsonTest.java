package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto.BookerDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 10, 0);
        BookerDto booker = new BookerDto(2L, "Booker", "b@mail.com");
        BookingResponseDto dto = new BookingResponseDto(1L, start, end, null, booker, BookingStatus.WAITING);

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id", 1);
        assertThat(result).hasJsonPathStringValue("@.start", "2025-01-01T10:00:00");
        assertThat(result).hasJsonPathStringValue("@.end", "2025-01-02T10:00:00");
        assertThat(result).hasJsonPathStringValue("@.status", "WAITING");
        assertThat(result).hasJsonPathNumberValue("@.booker.id", 2);
        assertThat(result).hasJsonPathStringValue("@.booker.name", "Booker");
        assertThat(result).hasJsonPathStringValue("@.booker.email", "b@mail.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"start\":\"2025-01-01T10:00:00\",\"end\":\"2025-01-02T10:00:00\",\"status\":\"WAITING\",\"booker\":{\"id\":2,\"name\":\"Booker\",\"email\":\"b@mail.com\"}}";

        BookingResponseDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Booker");
        assertThat(dto.getBooker().getEmail()).isEqualTo("b@mail.com");
    }
}