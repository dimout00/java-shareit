package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class ClientsConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public BookingClient bookingClient(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/bookings"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
        return new BookingClient(restTemplate);
    }

    @Bean
    public ItemClient itemClient(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/items"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
        return new ItemClient(restTemplate);
    }

    @Bean
    public UserClient userClient(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/users"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
        return new UserClient(restTemplate);
    }

    @Bean
    public ItemRequestClient itemRequestClient(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/requests"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
        return new ItemRequestClient(restTemplate);
    }
}