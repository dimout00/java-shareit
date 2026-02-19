package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {
    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDto.BookingItemDto itemDto = new BookingResponseDto.BookingItemDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );

        BookingResponseDto.BookerDto bookerDto = new BookingResponseDto.BookerDto(
                booking.getBooker().getId(),
                booking.getBooker().getName(),
                booking.getBooker().getEmail()
        );

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemDto,
                bookerDto,
                booking.getStatus()
        );
    }
}