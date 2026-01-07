package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);

    // Текущие бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now);

    // Прошлые бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId,
                                     @Param("now") LocalDateTime now);

    // Будущие бронирования пользователя
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId,
                                       @Param("now") LocalDateTime now);

    // Бронирования по статусу
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                             @Param("status") BookingStatus status);

    // Все бронирования для вещей владельца
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerEntity.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId);

    // Текущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerEntity.id = :ownerId " +
            "AND b.start <= :now " +
            "AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByItemOwnerId(@Param("ownerId") Long ownerId,
                                           @Param("now") LocalDateTime now);

    // Прошлые бронирования для вещей владельца
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerEntity.id = :ownerId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByItemOwnerId(@Param("ownerId") Long ownerId,
                                        @Param("now") LocalDateTime now);

    // Будущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerEntity.id = :ownerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByItemOwnerId(@Param("ownerId") Long ownerId,
                                          @Param("now") LocalDateTime now);

    // Бронирования по статусу для вещей владельца
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerEntity.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                @Param("status") BookingStatus status);

    // Последнее завершённое бронирование для вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Booking findLastBookingForItem(@Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);

    // Ближайшее будущее бронирование для вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Booking findNextBookingForItem(@Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);

    // Проверка, брал ли пользователь вещь в аренду
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsCompletedBookingByBookerAndItem(@Param("bookerId") Long bookerId,
                                                  @Param("itemId") Long itemId,
                                                  @Param("now") LocalDateTime now);
}
