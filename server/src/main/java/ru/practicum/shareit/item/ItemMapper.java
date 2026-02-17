package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        if (item == null) return null;
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);
        return dto;
    }

    public Item toEntity(ItemDto dto, User owner, ItemRequest request) {
        if (dto == null) return null;
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    public Item toEntity(ItemDto dto, User owner) {
        return toEntity(dto, owner, null);
    }

    public void updateEntity(Item existingItem, ItemDto dto) {
        if (dto.getName() != null) {
            existingItem.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existingItem.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existingItem.setAvailable(dto.getAvailable());
        }
    }
}