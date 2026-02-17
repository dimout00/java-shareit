package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequest toEntity(ItemRequestDto dto) {
        if (dto == null) return null;
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        // requester и created устанавливаются в сервисе
        return request;
    }

    public ItemRequestDto toDto(ItemRequest request) {
        if (request == null) return null;
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public ItemRequestDto toDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = toDto(request);
        if (request.getItems() != null) {
            List<ItemRequestDto.ItemDto> itemDtos = request.getItems().stream()
                    .map(item -> {
                        ItemRequestDto.ItemDto itemDto = new ItemRequestDto.ItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setName(item.getName());
                        itemDto.setOwnerId(item.getOwner().getId()); // предполагаем, что у Item есть владелец
                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }
}