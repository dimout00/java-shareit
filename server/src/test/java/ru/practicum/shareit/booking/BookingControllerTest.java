package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto.BookerDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_shouldReturnOk() throws Exception {
        BookingRequestDto input = new BookingRequestDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        BookerDto booker = new BookerDto(2L, "Booker", "booker@example.com");
        BookingResponseDto output = new BookingResponseDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null, // item
                booker,
                BookingStatus.WAITING);
        when(bookingService.createBooking(any(BookingRequestDto.class), eq(1L))).thenReturn(output);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_shouldReturnApproved() throws Exception {
        BookingResponseDto output = new BookingResponseDto(1L,
                null,
                null,
                null,
                null,
                BookingStatus.APPROVED);
        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true))).thenReturn(output);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookingResponseDto output = new BookingResponseDto(1L,
                null,
                null,
                null,
                null,
                BookingStatus.WAITING);
        when(bookingService.getBookingById(1L, 1L)).thenReturn(output);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBookingsByBooker_shouldReturnList() throws Exception {
        List<BookingResponseDto> list = List.of(
                new BookingResponseDto(1L, null, null, null, null, BookingStatus.WAITING)
        );
        when(bookingService.getUserBookings(eq(1L), anyString(), eq(0), eq(10)))
                .thenReturn(list);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookingsByOwner_shouldReturnList() throws Exception {
        List<BookingResponseDto> list = List.of(
                new BookingResponseDto(1L, null, null, null, null, BookingStatus.WAITING)
        );
        when(bookingService.getOwnerBookings(eq(1L), anyString(), eq(0), eq(10)))
                .thenReturn(list);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}