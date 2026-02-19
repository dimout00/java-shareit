package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        User owner = userService.getUserEntity(userId);

        Item item;
        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item = itemMapper.toEntity(itemDto, owner, request);
        } else {
            item = itemMapper.toEntity(itemDto, owner);
        }

        item = itemRepository.save(item);
        log.info("Создана вещь с ID: {}, владелец ID: {}", item.getId(), userId);
        return itemMapper.toDto(item);
    }

    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Только владелец может редактировать вещь");
        }

        itemMapper.updateEntity(item, itemDto);
        Item updatedItem = itemRepository.save(item);
        log.info("Обновлена вещь с ID: {}", itemId);
        return itemMapper.toDto(updatedItem);
    }

    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        // Если пользователь авторизован и является владельцем вещи, добавляем информацию о бронированиях
        if (userId != null && item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository.findLastBookingEndDate(itemId, now);
            nextBooking = bookingRepository.findNextBookingStartDate(itemId, now);
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                lastBooking,
                nextBooking,
                comments
        );
    }

    public List<ItemDto> getUserItems(Long userId) {
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableByText(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    public Item getItemEntity(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Transactional
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Item item = getItemEntity(itemId);
        User user = userService.getUserEntity(userId);

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository.existsCompletedApprovedBooking(userId, itemId, now);
        if (!hasCompletedBooking) {
            throw new ValidationException("У вас нет завершённого подтверждённого бронирования этой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(now);

        comment = commentRepository.save(comment);
        log.info("Комментарий создан с ID: {}", comment.getId());
        return commentMapper.toDto(comment);
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