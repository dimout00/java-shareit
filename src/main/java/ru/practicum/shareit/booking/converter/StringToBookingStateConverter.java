package ru.practicum.shareit.booking.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingState;

@Component
public class StringToBookingStateConverter implements Converter<String, BookingState> {

    @Override
    public BookingState convert(String source) {
        try {
            return BookingState.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Возвращаем ALL как значение по умолчанию при некорректном значении
            return BookingState.ALL;
        }
    }
}