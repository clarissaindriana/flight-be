package apap.ti._5.flight_2306211660_be.restcontroller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.flight_2306211660_be.restcontroller.bookingPassenger.BookingPassengerRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.AddBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.UpdateBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger.BookingPassengerResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.bookingPassenger.BookingPassengerRestService;


@ExtendWith(MockitoExtension.class)
class BookingPassengerControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private BookingPassengerRestService bookingPassengerRestService;

    @BeforeEach
    void setup() {
        // Controller under test
        var controller = new BookingPassengerRestController();
        ReflectionTestUtils.setField(controller, "bookingPassengerRestService", bookingPassengerRestService);

        // Configure ObjectMapper with JavaTime support
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    private BookingPassengerResponseDTO sample(String bookingId, UUID passengerId) {
        return BookingPassengerResponseDTO.builder()
                .bookingId(bookingId)
                .passengerId(passengerId)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("GET /api/booking-passenger returns 200 with list")
    void getAll_ok() throws Exception {
        var b1 = sample("FL-001-001", UUID.randomUUID());
        var b2 = sample("FL-001-002", UUID.randomUUID());
        when(bookingPassengerRestService.getAllBookingPassengers()).thenReturn(Arrays.asList(b1, b2));

        mockMvc.perform(get("/api/booking-passenger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].bookingId").value("FL-001-001"))
                .andExpect(jsonPath("$.data[1].bookingId").value("FL-001-002"));
    }

    @Test
    @DisplayName("GET /api/booking-passenger/{bookingId}/{passengerId} 200 when found")
    void getById_found() throws Exception {
        String bookId = "BK-123";
        UUID pid = UUID.randomUUID();
        when(bookingPassengerRestService.getBookingPassenger(bookId, pid)).thenReturn(sample(bookId, pid));

        mockMvc.perform(get("/api/booking-passenger/{bookingId}/{passengerId}", bookId, pid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingId").value(bookId))
                .andExpect(jsonPath("$.data.passengerId").value(pid.toString()));
    }

    @Test
    @DisplayName("GET /api/booking-passenger/{bookingId}/{passengerId} 404 when not found")
    void getById_notFound() throws Exception {
        String bookId = "BK-404";
        UUID pid = UUID.randomUUID();
        when(bookingPassengerRestService.getBookingPassenger(bookId, pid)).thenReturn(null);

        mockMvc.perform(get("/api/booking-passenger/{bookingId}/{passengerId}", bookId, pid.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/booking-passenger/create 201 when valid")
    void create_valid() throws Exception {
        String bookId = "BK-201";
        UUID pid = UUID.randomUUID();
        var request = AddBookingPassengerRequestDTO.builder()
                .bookingId(bookId)
                .passengerId(pid)
                .build();
        when(bookingPassengerRestService.createBookingPassenger(any(AddBookingPassengerRequestDTO.class)))
                .thenReturn(sample(bookId, pid));

        mockMvc.perform(post("/api/booking-passenger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.bookingId").value(bookId))
                .andExpect(jsonPath("$.data.passengerId").value(pid.toString()));
    }

    @Test
    @DisplayName("POST /api/booking-passenger/create 400 when invalid payload")
    void create_invalid() throws Exception {
        // Assume DTO has @NotBlank and @NotNull, send invalid
        var invalid = AddBookingPassengerRequestDTO.builder()
                .bookingId("")          // NotBlank
                .passengerId(null)      // NotNull
                .build();

        mockMvc.perform(post("/api/booking-passenger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(bookingPassengerRestService, never()).createBookingPassenger(any());
    }

    @Test
    @DisplayName("POST /api/booking-passenger/create 500 when service throws")
    void create_exception() throws Exception {
        String bookId = "BK-500";
        UUID pid = UUID.randomUUID();
        var request = AddBookingPassengerRequestDTO.builder()
                .bookingId(bookId)
                .passengerId(pid)
                .build();
        when(bookingPassengerRestService.createBookingPassenger(any(AddBookingPassengerRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/booking-passenger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("PUT /api/booking-passenger/update 200 when valid")
    void update_valid() throws Exception {
        String bookId = "BK-200";
        UUID pid = UUID.randomUUID();
        var request = UpdateBookingPassengerRequestDTO.builder()
                .bookingId(bookId)
                .passengerId(pid)
                .build();
        when(bookingPassengerRestService.updateBookingPassenger(any(UpdateBookingPassengerRequestDTO.class)))
                .thenReturn(sample(bookId, pid));

        mockMvc.perform(put("/api/booking-passenger/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingId").value(bookId))
                .andExpect(jsonPath("$.data.passengerId").value(pid.toString()));
    }

    @Test
    @DisplayName("PUT /api/booking-passenger/update 400 when invalid payload")
    void update_invalid() throws Exception {
        var invalid = UpdateBookingPassengerRequestDTO.builder()
                .bookingId("")   // NotBlank
                .passengerId(null) // NotNull
                .build();

        mockMvc.perform(put("/api/booking-passenger/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(bookingPassengerRestService, never()).updateBookingPassenger(any());
    }

    @Test
    @DisplayName("PUT /api/booking-passenger/update 404 when not found")
    void update_notFound() throws Exception {
        String bookId = "BK-404";
        UUID pid = UUID.randomUUID();
        var request = UpdateBookingPassengerRequestDTO.builder()
                .bookingId(bookId)
                .passengerId(pid)
                .build();
        when(bookingPassengerRestService.updateBookingPassenger(any(UpdateBookingPassengerRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/booking-passenger/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/booking-passenger/update 500 when service throws")
    void update_exception() throws Exception {
        var request = UpdateBookingPassengerRequestDTO.builder()
                .bookingId("BK-EX")
                .passengerId(UUID.randomUUID())
                .build();

        when(bookingPassengerRestService.updateBookingPassenger(any(UpdateBookingPassengerRequestDTO.class)))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(put("/api/booking-passenger/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("fail")));
    }

    @Test
    @DisplayName("POST /api/booking-passenger/delete/{bookingId}/{passengerId} 200 when found")
    void delete_found() throws Exception {
        String bookId = "BK-DEL";
        UUID pid = UUID.randomUUID();
        when(bookingPassengerRestService.deleteBookingPassenger(bookId, pid)).thenReturn(sample(bookId, pid));

        mockMvc.perform(post("/api/booking-passenger/delete/{bookingId}/{passengerId}", bookId, pid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingId").value(bookId))
                .andExpect(jsonPath("$.data.passengerId").value(pid.toString()));
    }

    @Test
    @DisplayName("POST /api/booking-passenger/delete/{bookingId}/{passengerId} 404 when not found")
    void delete_notFound() throws Exception {
        String bookId = "BK-404";
        UUID pid = UUID.randomUUID();
        when(bookingPassengerRestService.deleteBookingPassenger(bookId, pid)).thenReturn(null);

        mockMvc.perform(post("/api/booking-passenger/delete/{bookingId}/{passengerId}", bookId, pid.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/booking-passenger/delete/{bookingId}/{passengerId} 500 when service throws")
    void delete_exception() throws Exception {
        String bookId = "BK-ERR";
        UUID pid = UUID.randomUUID();
        when(bookingPassengerRestService.deleteBookingPassenger(bookId, pid))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/booking-passenger/delete/{bookingId}/{passengerId}", bookId, pid.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }
}
