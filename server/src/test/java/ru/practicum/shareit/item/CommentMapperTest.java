package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper commentMapper = new CommentMapper();

    @Test
    void toCommentDto_shouldMapFields() {
        User author = new User(1L, "John", "j@mail.com");
        Item item = new Item(2L, "Item", "Desc", true, author, null);
        Comment comment = new Comment(3L, "Great!", item, author, LocalDateTime.now());

        CommentDto dto = commentMapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getText()).isEqualTo("Great!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getCreated()).isEqualTo(comment.getCreated().toString());
    }
}