package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void createUser_shouldSaveAndReturnUser() {
        UserDto dto = new UserDto(null, "John", "john@example.com");

        UserDto saved = userService.createUser(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");

        User found = em.find(User.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void createUser_duplicateEmail_shouldThrowConflictException() {
        User user = new User(null, "Existing", "duplicate@example.com");
        em.persist(user);
        em.flush();

        UserDto dto = new UserDto(null, "John", "duplicate@example.com");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void updateUser_shouldUpdateFields() {
        User user = new User(null, "OldName", "old@example.com");
        em.persist(user);
        em.flush();

        UserDto updateDto = new UserDto(null, "NewName", "new@example.com");
        UserDto updated = userService.updateUser(user.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void getUserById_existingUser_shouldReturnUser() {
        User user = new User(null, "Test", "test@example.com");
        em.persist(user);
        em.flush();

        UserDto found = userService.getUserById(user.getId());

        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_notFound_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User user = new User(null, "ToDelete", "delete@example.com");
        em.persist(user);
        em.flush();

        userService.deleteUser(user.getId());

        User found = em.find(User.class, user.getId());
        assertThat(found).isNull();
    }
}