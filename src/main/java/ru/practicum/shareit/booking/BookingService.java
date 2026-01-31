package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        // Проверяем, что пользователь существует
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        // Проверяем, что вещь существует
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + bookingRequestDto.getItemId() + " не найдена"));

        // Проверяем, что вещь доступна
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с ID " + item.getId() + " недоступна для бронирования");
        }

        // Проверяем, что пользователь не владелец вещи
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        // Проверяем даты бронирования
        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        validateBookingDates(start, end);

        // Проверяем, что нет пересекающихся бронирований
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                item.getId(), start, end);
        if (!overlappingBookings.isEmpty()) {
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);

        log.info("Создано бронирование с ID: {}", booking.getId());
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверяем, что пользователь является владельцем вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Только владелец вещи может подтверждать или отклонять бронирование");
        }

        // Проверяем, что бронирование еще не обработано
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        log.info("Бронирование с ID: {} {}", bookingId, approved ? "подтверждено" : "отклонено");
        return BookingMapper.toBookingResponseDto(booking);
    }

    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверяем, что пользователь имеет отношение к бронированию
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Просмотр бронирования доступно только арендатору или владельцу вещи");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartAfter(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwnerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable).stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    // Вспомогательный метод для ItemService
    public List<Booking> getCompletedApprovedBookings(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findCompletedApprovedBookings(userId, itemId, now);
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Даты начала и окончания бронирования обязательны");
        }

        if (start.isAfter(end) || start.equals(end)) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты окончания");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }
    }
}