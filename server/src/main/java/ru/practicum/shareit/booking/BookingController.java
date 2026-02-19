package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@Valid @RequestBody BookingRequestDto bookingRequestDto,
                                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /bookings - создание бронирования пользователем ID: {}", userId);
        return bookingService.createBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable Long bookingId,
                                             @RequestParam boolean approved,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH /bookings/{}?approved={} - подтверждение/отклонение бронирования пользователем ID: {}",
                bookingId, approved, userId);
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /bookings/{} - получение бронирования по ID пользователем ID: {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(defaultValue = "ALL") BookingState state,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings?state={} - получение бронирований пользователя ID: {} с состоянием {}",
                state, userId, state);
        return bookingService.getUserBookings(userId, state.name(), from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "ALL") BookingState state,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings/owner?state={} - получение бронирований вещей владельца ID: {} с состоянием {}",
                state, userId, state);
        return bookingService.getOwnerBookings(userId, state.name(), from, size);
    }
}