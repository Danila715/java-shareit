package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner(Long ownerId);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%')))")
    List<Item> searchByText(@Param("text") String text);
}