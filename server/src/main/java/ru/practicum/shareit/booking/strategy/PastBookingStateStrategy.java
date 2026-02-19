package ru.practicum.shareit.booking.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Component("PAST")
@RequiredArgsConstructor
public class PastBookingStateStrategy implements BookingStateFetchStrategy {
    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> getBookings(Long userId, LocalDateTime now, Pageable pageable) {
        return bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable);
    }
}