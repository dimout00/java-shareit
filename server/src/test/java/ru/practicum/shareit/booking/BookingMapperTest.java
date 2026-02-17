package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBookingResponseDto_shouldMapAllFields() {
        User booker = new User(2L, "Booker", "b@mail.com");
        Item item = new Item(3L, "Item", "Desc", true, new User(1L, "Owner", "o@mail.com"), null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                item, booker, BookingStatus.WAITING);

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getItem().getId()).isEqualTo(3L);
    }
}