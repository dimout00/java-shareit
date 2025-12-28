package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;
    private final Set<String> emails = new HashSet<>();

    public UserDto createUser(UserDto userDto) {
        validateUser(userDto);

        if (emails.contains(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(currentId++);

        users.put(user.getId(), user);
        emails.add(user.getEmail());

        log.info("Создан пользователь с ID: {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (emails.contains(userDto.getEmail())) {
                throw new ConflictException("Email " + userDto.getEmail() + " уже используется другим пользователем");
            }
            emails.remove(user.getEmail());
            emails.add(userDto.getEmail());
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        log.info("Обновлен пользователь с ID: {}", userId);
        return UserMapper.toUserDto(user);
    }

    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        User user = users.remove(userId);
        if (user != null) {
            emails.remove(user.getEmail());
        }
        log.info("Удален пользователь с ID: {}", userId);
    }

    public User getUserEntity(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }

    public boolean userExists(Long userId) {
        return users.containsKey(userId);
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