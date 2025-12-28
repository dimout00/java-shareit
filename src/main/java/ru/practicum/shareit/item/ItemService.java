package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> userItems = new HashMap<>(); // userId -> список itemId
    private Long currentId = 1L;

    public ItemDto createItem(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        // Проверяем существование пользователя
        User owner;
        try {
            owner = userService.getUserEntity(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Item item = ItemMapper.toItem(itemDto, owner);
        item.setId(currentId++);

        items.put(item.getId(), item);

        // Сохраняем связь пользователь -> вещи
        userItems.computeIfAbsent(userId, k -> new ArrayList<>()).add(item.getId());

        log.info("Создана вещь с ID: {}, владелец ID: {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с ID " + itemId + " не найдена");
        }

        // Проверяем, что пользователь является владельцем
        if (item.getOwner() == null || !item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        log.info("Обновлена вещь с ID: {}", itemId);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с ID " + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getUserItems(Long userId) {
        List<Long> itemIds = userItems.get(userId);
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        return itemIds.stream()
                .map(items::get)
                .filter(Objects::nonNull)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> containsText(item, lowerText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Item getItemEntity(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с ID " + itemId + " не найдена");
        }
        return item;
    }

    private boolean containsText(Item item, String text) {
        return (item.getName() != null && item.getName().toLowerCase().contains(text)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(text));
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }
    }
}