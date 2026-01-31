package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :time AND b.end > :time")
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(@Param("bookerId") Long bookerId,
                                                          @Param("time") LocalDateTime time,
                                                          Pageable pageable);

    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :time AND b.end > :time")
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(@Param("ownerId") Long ownerId,
                                                             @Param("time") LocalDateTime time,
                                                             Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status != :status AND b.end < :time ORDER BY b.start DESC")
    List<Booking> findByItemIdAndStatusNotAndEndBefore(@Param("itemId") Long itemId,
                                                       @Param("status") BookingStatus status,
                                                       @Param("time") LocalDateTime time);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status != :status AND b.start > :time ORDER BY b.start ASC")
    List<Booking> findByItemIdAndStatusNotAndStartAfter(@Param("itemId") Long itemId,
                                                        @Param("status") BookingStatus status,
                                                        @Param("time") LocalDateTime time);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.end < :time AND b.status = :status ORDER BY b.end DESC")
    List<Booking> findByItemIdAndEndBeforeAndStatusOrderByEndDesc(@Param("itemId") Long itemId,
                                                                  @Param("time") LocalDateTime time,
                                                                  @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :time AND b.status = :status ORDER BY b.start ASC")
    List<Booking> findByItemIdAndStartAfterAndStatusOrderByStartAsc(@Param("itemId") Long itemId,
                                                                    @Param("time") LocalDateTime time,
                                                                    @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND ((b.start < :end AND b.end > :start))")
    List<Booking> findOverlappingBookings(@Param("itemId") Long itemId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId AND b.end < :time AND b.status = :status")
    List<Booking> findByBookerIdAndItemIdAndEndBeforeAndStatus(@Param("bookerId") Long bookerId,
                                                               @Param("itemId") Long itemId,
                                                               @Param("time") LocalDateTime time,
                                                               @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId AND b.status = :status")
    boolean existsByBookerIdAndItemIdAndStatus(@Param("bookerId") Long bookerId,
                                               @Param("itemId") Long itemId,
                                               @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.end < :time AND b.status = :status")
    Optional<Booking> findFirstByItemIdAndBookerIdAndEndBeforeAndStatus(@Param("itemId") Long itemId,
                                                                        @Param("bookerId") Long bookerId,
                                                                        @Param("time") LocalDateTime time,
                                                                        @Param("status") BookingStatus status,
                                                                        Sort sort);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId AND b.status != :status")
    boolean existsByBookerIdAndItemIdAndStatusNot(@Param("bookerId") Long bookerId,
                                                  @Param("itemId") Long itemId,
                                                  @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED'")
    List<Booking> findApprovedBookings(@Param("userId") Long userId,
                                       @Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    List<Booking> findCompletedApprovedBookings(@Param("userId") Long userId,
                                                @Param("itemId") Long itemId,
                                                @Param("now") LocalDateTime now);
}