package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Set<String> emails = ConcurrentHashMap.newKeySet();
    private final AtomicLong currentId = new AtomicLong(1L);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(currentId.getAndIncrement());
        }
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        User existing = users.get(user.getId());
        if (existing != null && !existing.getEmail().equals(user.getEmail())) {
            emails.remove(existing.getEmail());
            emails.add(user.getEmail());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        User user = users.remove(id);
        if (user != null) {
            emails.remove(user.getEmail());
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return emails.contains(email);
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}