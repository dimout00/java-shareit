package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final UserService userService;
    private final ItemStorage itemStorage;

    public ItemDto createItem(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        // Используем метод сервиса напрямую - исключение пробросится автоматически
        var owner = userService.getUserEntity(userId);

        Item item = ItemMapper.toItem(itemDto, owner);
        item = itemStorage.save(item);

        log.info("Создана вещь с ID: {}, владелец ID: {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

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

        Item updatedItem = itemStorage.update(item);
        log.info("Обновлена вещь с ID: {}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        return itemStorage.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    public List<ItemDto> getUserItems(Long userId) {
        return itemStorage.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text, Long userId) {
        return itemStorage.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Item getItemEntity(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
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