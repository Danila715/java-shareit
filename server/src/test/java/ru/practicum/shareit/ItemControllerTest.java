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
    void addItem_shouldReturn200() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Мощная");
        dto.setAvailable(true);

        ItemDto response = new ItemDto();
        response.setId(1L);

        when(itemService.addItem(eq(1L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateItem_shouldReturn200() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Обновлённая дрель");

        ItemDto response = new ItemDto();
        response.setId(1L);

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getItem_shouldReturn200() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(1L);

        when(itemService.getItemById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getItemsByUser_shouldReturn200() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(1L);

        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void searchItems_shouldReturn200() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(1L);

        when(itemService.searchItems("дрель")).thenReturn(List.of(response));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addComment_shouldReturn200() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setText("Хорошая вещь");

        CommentDto response = new CommentDto();
        response.setId(1L);

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}