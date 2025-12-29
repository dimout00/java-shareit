package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserDto createUser(UserDto userDto) {
        validateUser(userDto);

        // Проверяем уникальность email с помощью семантического метода
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user = userStorage.save(user);

        log.info("Создан пользователь с ID: {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            // Используем семантический метод existsByEmail
            if (userStorage.existsByEmail(userDto.getEmail())) {
                throw new ConflictException("Email " + userDto.getEmail() + " уже используется другим пользователем");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        User updatedUser = userStorage.update(existingUser);
        log.info("Обновлен пользователь с ID: {}", userId);
        return UserMapper.toUserDto(updatedUser);
    }

    public UserDto getUserById(Long userId) {
        return userStorage.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public List<UserDto> getAllUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        userStorage.deleteById(userId);
        log.info("Удален пользователь с ID: {}", userId);
    }

    public User getUserEntity(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public boolean userExists(Long userId) {
        return userStorage.existsById(userId);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }
    }
}