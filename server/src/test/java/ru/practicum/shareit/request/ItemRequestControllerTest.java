package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void createRequest_shouldReturnOk() throws Exception {
        ItemRequestDto input = new ItemRequestDto(null, "Need drill", null, null, null);
        ItemRequestDto output = new ItemRequestDto(1L, "Need drill", null, LocalDateTime.now(), null);
        when(requestService.createRequest(eq(1L), any(ItemRequestDto.class))).thenReturn(output);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need drill"));
    }

    @Test
    void getUserRequests_shouldReturnList() throws Exception {
        List<ItemRequestDto> list = List.of(
                new ItemRequestDto(1L, "Drill", null, LocalDateTime.now(), List.of())
        );
        when(requestService.getUserRequests(1L)).thenReturn(list);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        List<ItemRequestDto> list = List.of(
                new ItemRequestDto(1L, "Drill", null, LocalDateTime.now(), List.of())
        );
        when(requestService.getAllRequests(eq(1L), eq(0), eq(10))).thenReturn(list);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        ItemRequestDto output = new ItemRequestDto(1L, "Drill", null, LocalDateTime.now(), List.of());
        when(requestService.getRequestById(1L, 1L)).thenReturn(output);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}