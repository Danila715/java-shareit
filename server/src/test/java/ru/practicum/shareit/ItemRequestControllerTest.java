package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestResponseDto;
import ru.practicum.shareit.request.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createRequest_shouldReturn201() throws Exception {
        // given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(1L);
        response.setDescription("Нужна дрель");
        response.setCreated(LocalDateTime.now());
        response.setItems(Collections.emptyList());

        when(requestService.createRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void getOwnRequests_shouldReturn200() throws Exception {
        // given
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(1L);
        response.setDescription("Нужна дрель");
        response.setCreated(LocalDateTime.now());
        response.setItems(Collections.emptyList());

        when(requestService.getOwnRequests(1L))
                .thenReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAllRequests_shouldReturn200() throws Exception {
        // given
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(2L);
        response.setDescription("Нужна пила");
        response.setCreated(LocalDateTime.now());
        response.setItems(Collections.emptyList());

        when(requestService.getAllRequests(1L))
                .thenReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getRequestById_shouldReturn200() throws Exception {
        // given
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(1L);
        response.setDescription("Нужна дрель");
        response.setCreated(LocalDateTime.now());
        response.setItems(Collections.emptyList());

        when(requestService.getRequestById(1L, 1L))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}
