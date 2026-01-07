package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId,
                                     @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId,
                                       @Param("now") LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :ownerId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByItemOwnerId(@Param("ownerId") Long ownerId,
                                           @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :ownerId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByItemOwnerId(@Param("ownerId") Long ownerId,
                                        @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :ownerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByItemOwnerId(@Param("ownerId") Long ownerId,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.owner = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    Booking findLastBookingForItem(@Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    Booking findNextBookingForItem(@Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);

    // ✅ КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ ЗДЕСЬ
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.bookerId = :bookerId " +
            "AND b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end <= :now")
    boolean existsCompletedBookingByBookerAndItem(@Param("bookerId") Long bookerId,
                                                  @Param("itemId") Long itemId,
                                                  @Param("now") LocalDateTime now);
}
