package ru.practicum.shareit.booking.strategy;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStateFetchStrategy {
    List<Booking> getBookings(Long userId, LocalDateTime now, Pageable pageable);
}