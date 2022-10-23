package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    private final Long defaultBookingId = 1L;
    private final Long defaultItemId = 1L;
    private final Long defaultUserId = 1L;

    private final LocalDateTime defaultStart = LocalDateTime.of(2023, 1, 1, 12, 0);
    private final LocalDateTime defaultEnd = LocalDateTime.of(2023, 2, 1, 12, 0);

    private final BookingDto bookingDto = new BookingDto(defaultBookingId, defaultStart, defaultEnd,
            defaultItemId, defaultUserId, BookingStatus.APPROVED);

    private final BookingInfoDto.Item bookingInfoDtoItem =
            new BookingInfoDto.Item(defaultItemId, "Item", "Item description", true);
    private final BookingInfoDto.User bookingInfoDtoUser =
            new BookingInfoDto.User(defaultUserId, "Test User", "user@mail.com");

    private final BookingInfoDto bookingInfoDto = new BookingInfoDto(defaultBookingId, defaultStart, defaultEnd,
            BookingStatus.APPROVED, bookingInfoDtoItem, bookingInfoDtoUser);

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(any(), anyLong())).thenReturn(bookingInfoDto);

        mvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.containsString(bookingInfoDto.getStart().toString())))
                .andExpect(jsonPath("$.end", Matchers.containsString(bookingInfoDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingInfoDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingInfoDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingInfoDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingInfoDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingInfoDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.booker.id", is(bookingInfoDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingInfoDto.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingInfoDto.getBooker().getEmail())));

        verify(bookingService, times(1)).createBooking(bookingDto, defaultUserId);
    }

    @Test
    void updateBookingTest() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingInfoDto);

        mvc.perform(MockMvcRequestBuilders.patch("/bookings/" + defaultBookingId + "?approved=true")
                        .header("X-Sharer-User-Id", defaultUserId)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.containsString(bookingInfoDto.getStart().toString())))
                .andExpect(jsonPath("$.end", Matchers.containsString(bookingInfoDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingInfoDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingInfoDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingInfoDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingInfoDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingInfoDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.booker.id", is(bookingInfoDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingInfoDto.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingInfoDto.getBooker().getEmail())));

        verify(bookingService, times(1))
                .approveBooking(defaultUserId, defaultItemId, true);
    }

    @Test
    void getBookingTest() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(bookingInfoDto);

        mvc.perform(MockMvcRequestBuilders.get("/bookings/" + defaultBookingId)
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.containsString(bookingInfoDto.getStart().toString())))
                .andExpect(jsonPath("$.end", Matchers.containsString(bookingInfoDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingInfoDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingInfoDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingInfoDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingInfoDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingInfoDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.booker.id", is(bookingInfoDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingInfoDto.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingInfoDto.getBooker().getEmail())));

        verify(bookingService, times(1)).getBooking(defaultUserId, defaultBookingId);
    }

    @Test
    void getBookingsTest() throws Exception {
        when(bookingService.getBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingService, times(1))
                .getBookings(defaultUserId, BookingState.ALL, new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void getBookingsIllegalStateTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/bookings?state=ILLEGAL")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForOwner() throws Exception {
        when(bookingService.getBookingsForOwner(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingService, times(1))
                .getBookingsForOwner(defaultUserId, BookingState.ALL,
                        new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void getBookingsForOwnerIllegalStateTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner?state=ILLEGAL")
                        .header("X-Sharer-User-Id", defaultUserId))
                .andExpect(status().isBadRequest());
    }
}
