package ru.practicum.shareit.item.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CommentDto {
    private Long id;
    @NotBlank(message = "Не может быть пустым")
    private String text;
    private String authorName;
    private String created;
}