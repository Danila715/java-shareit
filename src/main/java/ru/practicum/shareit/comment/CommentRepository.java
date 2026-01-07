package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.item.id = :itemId")
    List<Comment> findAllByItemId(@Param("itemId") Long itemId);

    @Query("SELECT c FROM Comment c WHERE c.item.id IN :itemIds")
    List<Comment> findAllByItemIdIn(@Param("itemIds") List<Long> itemIds);
}