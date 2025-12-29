package ru.practicum.shareit;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

public class TestData {
    public static UserDto createTestUserDto() {
        return new UserDto(null, "Test User", "test@email.com");
    }

    public static ItemDto createTestItemDto() {
        return new ItemDto(null, "Дрель", "Мощная дрель для бетона", true, null);
    }

    public static ItemDto createTestItemDto2() {
        return new ItemDto(null, "Молоток", "Строительный молоток", true, null);
    }
}