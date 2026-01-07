package ru.practicum.shareit.comment;

import java.time.LocalDateTime;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, String authorName) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(authorName);
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public static Comment toComment(CommentDto commentDto, Long itemId, Long authorId) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(authorId);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}