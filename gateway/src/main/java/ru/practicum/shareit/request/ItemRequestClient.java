package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getUserRequests(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getRequestById(long userId, long requestId) {
        return get("/" + requestId, userId);
    }
}