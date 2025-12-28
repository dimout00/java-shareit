package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя: {}", userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody UserDto userDto) { // Убрали @Valid для PATCH
        log.info("PATCH /users/{} - обновление пользователя: {}", userId, userDto);
        return userService.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("GET /users - получение всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.deleteUser(userId);
    }
}