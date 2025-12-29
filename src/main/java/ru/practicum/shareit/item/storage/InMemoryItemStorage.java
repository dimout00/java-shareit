package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> ownerItems = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1L);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(currentId.getAndIncrement());
        }
        items.put(item.getId(), item);

        // Сохраняем связь владелец -> вещи
        ownerItems.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>())
                .add(item.getId());

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        List<Long> itemIds = ownerItems.get(ownerId);
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        return itemIds.stream()
                .map(items::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> containsText(item, lowerText))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }

    private boolean containsText(Item item, String text) {
        return (item.getName() != null && item.getName().toLowerCase().contains(text)) ||
                (item.getDescription() != null && item.getDescription().toLowerCase().contains(text));
    }
}