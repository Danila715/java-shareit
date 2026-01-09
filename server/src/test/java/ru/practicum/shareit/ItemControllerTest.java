package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void getItemsByUser_shouldReturn200() throws Exception {
        // given
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");

        when(itemService.getItemsByOwner(1L))
                .thenReturn(List.of(item));

        // when & then
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateItem_shouldReturn200() throws Exception {
        // given
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");

        ItemDto response = new ItemDto();
        response.setId(1L);
        response.setName("Новое имя");
        response.setDescription("Описание");
        response.setAvailable(true);

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void searchItems_shouldReturn200() throws Exception {
        // given
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");

        when(itemService.searchItems("дрель"))
                .thenReturn(List.of(item));

        // when & then
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void searchItems_withEmptyText_shouldReturnEmptyList() throws Exception {
        // given
        when(itemService.searchItems(""))
                .thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addComment_shouldReturn200() throws Exception {
        // given
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная вещь!");

        CommentDto response = new CommentDto();
        response.setId(1L);
        response.setText("Отличная вещь!");
        response.setAuthorName("User");

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}