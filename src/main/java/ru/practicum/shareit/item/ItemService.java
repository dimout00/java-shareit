package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
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

    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        var owner = userService.getUserEntity(userId);

        Item item = ItemMapper.toItem(itemDto, owner);
        item = itemRepository.save(item);

        log.info("Создана вещь с ID: {}, владелец ID: {}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

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

        Item updatedItem = itemRepository.save(item);
        log.info("Обновлена вещь с ID: {}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                lastBooking,
                nextBooking,
                comments
        );
    }

    public List<ItemDto> getUserItems(Long userId) {
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
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

        var item = getItemEntity(itemId);
        User user = userService.getUserEntity(userId);

        LocalDateTime now = LocalDateTime.now();

        // Используем exists-запрос вместо получения всех записей
        boolean hasCompletedBooking = bookingRepository.existsCompletedApprovedBooking(userId, itemId, now);

        if (!hasCompletedBooking) {
            throw new ValidationException("У вас нет завершенного подтвержденного бронирования этой вещи");
        }

        // Проверяем последнее бронирование для уточнения времени
        List<Booking> approvedBookings = bookingRepository.findApprovedBookings(userId, itemId);
        Booking latestBooking = approvedBookings.stream()
                .max((b1, b2) -> b1.getEnd().compareTo(b2.getEnd()))
                .orElse(null);

        if (latestBooking != null) {
            // Проверяем разницу во времени
            long secondsBetween = java.time.Duration.between(latestBooking.getEnd(), now).getSeconds();

            // Разрешаем комментарий если прошло больше 2 секунд с момента окончания бронирования
            if (secondsBetween >= 2) {
                return createComment(commentDto, item, user);
            }

            // Проверяем, не в будущем ли бронирование
            long secondsUntilEnd = java.time.Duration.between(now, latestBooking.getEnd()).getSeconds();
            if (secondsUntilEnd > 0) {
                throw new ValidationException("Нельзя оставить комментарий к незавершенному бронированию");
            }

            // Если разница небольшая (0-1 секунда), тоже разрешаем для надежности
            return createComment(commentDto, item, user);
        }

        return createComment(commentDto, item, user);
    }

    private CommentDto createComment(CommentDto commentDto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Комментарий создан с ID: {}", comment.getId());
        return CommentMapper.toCommentDto(comment);
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