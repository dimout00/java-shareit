package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
    void createItem_shouldReturnOk() throws Exception {
        ItemDto input = new ItemDto(null, "Drill", "Desc", true, null);
        ItemDto output = new ItemDto(1L, "Drill", "Desc", true, null);
        when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(output);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void updateItem_shouldReturnUpdated() throws Exception {
        ItemDto input = new ItemDto(null, "NewName", "NewDesc", false, null);
        ItemDto output = new ItemDto(1L, "NewName", "NewDesc", false, null);
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L))).thenReturn(output);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItemById_shouldReturnItemWithBookings() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto(1L, "Item", "Desc", true, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), List.of());
        when(itemService.getItemById(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lastBooking").exists());
    }

    @Test
    void getUserItems_shouldReturnList() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Item1", "Desc", true, null)
        );
        when(itemService.getUserItems(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchItems_shouldReturnList() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Hammer", "Heavy", true, null)
        );
        when(itemService.searchItems(eq("hammer"), anyLong())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "hammer")
                        .header("X-Sharer-User-Id", 1L)) // добавлен заголовок
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        CommentDto input = new CommentDto(null, "Great!", null, null);
        CommentDto output = new CommentDto(1L, "Great!", "Booker", LocalDateTime.now());
        when(itemService.addComment(eq(1L), any(CommentDto.class), eq(1L))).thenReturn(output);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.authorName").value("Booker"));
    }
}