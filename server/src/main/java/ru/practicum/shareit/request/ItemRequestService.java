package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto);
    List<ItemRequestDto> getUserRequests(Long userId);
    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);
    ItemRequestDto getRequestById(Long userId, Long requestId);
}