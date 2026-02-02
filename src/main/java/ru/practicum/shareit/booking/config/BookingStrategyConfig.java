package ru.practicum.shareit.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.booking.strategy.BookingStateFetchStrategy;

import java.util.Map;
import java.util.Set;

@Configuration
public class BookingStrategyConfig {

    @Bean
    public Map<String, BookingStateFetchStrategy> bookingStrategyMap(Set<BookingStateFetchStrategy> strategies) {
        return strategies.stream()
                .collect(java.util.stream.Collectors.toMap(
                        strategy -> strategy.getClass().getAnnotation(org.springframework.stereotype.Component.class).value(),
                        strategy -> strategy
                ));
    }
}