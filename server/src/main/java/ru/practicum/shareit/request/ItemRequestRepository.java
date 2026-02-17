package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Запросы конкретного пользователя, отсортированные по убыванию даты создания
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    // Запросы всех пользователей, кроме указанного, с пагинацией
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requester.id != :userId ORDER BY ir.created DESC")
    Page<ItemRequest> findAllExceptUser(@Param("userId") Long userId, Pageable pageable);

    // Запрос с подгрузкой списка вещей
    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.items WHERE ir.id = :requestId")
    ItemRequest findByIdWithItems(@Param("requestId") Long requestId);
}