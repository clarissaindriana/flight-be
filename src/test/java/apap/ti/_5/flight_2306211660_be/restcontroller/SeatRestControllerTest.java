package apap.ti._5.flight_2306211660_be.restcontroller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import apap.ti._5.flight_2306211660_be.restcontroller.seat.SeatRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.UpdateSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;

@ExtendWith(MockitoExtension.class)
class SeatRestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SeatRestService seatRestService;

    @BeforeEach
    void setup() {
        var controller = new SeatRestController();
        ReflectionTestUtils.setField(controller, "seatRestService", seatRestService);

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

    private SeatResponseDTO sample(Integer id, Integer classFlightId, UUID passengerId, String code, boolean isBooked) {
        return SeatResponseDTO.builder()
                .id(id)
                .classFlightId(classFlightId)
                .passengerId(passengerId)
                .seatCode(code)
                .isBooked(isBooked)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // GET /api/seat (no filters) => getAllSeats()
    @Test
    @DisplayName("GET /api/seat returns 200 with all seats when no filters")
    void getAll_noFilters() throws Exception {
        var s1 = sample(1, 10, null, "EC001", false);
        var s2 = sample(2, 11, UUID.randomUUID(), "BU001", true);
        when(seatRestService.getAllSeats()).thenReturn(Arrays.asList(s1, s2));

        mockMvc.perform(get("/api/seat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].seatCode").value("EC001"))
                .andExpect(jsonPath("$.data[1].seatCode").value("BU001"));

        verify(seatRestService).getAllSeats();
        verify(seatRestService, never()).getSeatsByClassFlight(any());
        verify(seatRestService, never()).getSeatsByFlight(anyString());
    }

    // GET /api/seat?classFlightId=... => getSeatsByClassFlight()
    @Test
    @DisplayName("GET /api/seat with classFlightId filter returns 200")
    void getAll_withClassFlightFilter() throws Exception {
        var s = sample(3, 99, null, "EC009", false);
        when(seatRestService.getSeatsByClassFlight(99)).thenReturn(Collections.singletonList(s));

        mockMvc.perform(get("/api/seat").param("classFlightId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].classFlightId").value(99));

        verify(seatRestService).getSeatsByClassFlight(99);
        verify(seatRestService, never()).getAllSeats();
        verify(seatRestService, never()).getSeatsByFlight(anyString());
    }

    // GET /api/seat?flightId=... => getSeatsByFlight()
    @Test
    @DisplayName("GET /api/seat with flightId filter returns 200")
    void getAll_withFlightFilter() throws Exception {
        var s = sample(4, 100, null, "BU010", false);
        when(seatRestService.getSeatsByFlight("FL-1")).thenReturn(Collections.singletonList(s));

        mockMvc.perform(get("/api/seat").param("flightId", "FL-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].seatCode").value("BU010"));

        verify(seatRestService).getSeatsByFlight("FL-1");
        verify(seatRestService, never()).getAllSeats();
        verify(seatRestService, never()).getSeatsByClassFlight(any());
    }

    // GET /api/seat/{id} 200/404
    @Test
    @DisplayName("GET /api/seat/{id} returns 200 when found")
    void getSeat_found() throws Exception {
        var s = sample(7, 10, null, "EC007", false);
        when(seatRestService.getSeat(7)).thenReturn(s);

        mockMvc.perform(get("/api/seat/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(7));
    }

    @Test
    @DisplayName("GET /api/seat/{id} returns 404 when not found")
    void getSeat_notFound() throws Exception {
        when(seatRestService.getSeat(404)).thenReturn(null);

        mockMvc.perform(get("/api/seat/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // POST /api/seat/create 201/400/500
    @Test
    @DisplayName("POST /api/seat/create returns 201 when valid")
    void create_valid() throws Exception {
        var req = AddSeatRequestDTO.builder()
                .classFlightId(10)
                .seatCode("EC001")
                .build();
        var resp = sample(1, 10, null, "EC001", false);
        when(seatRestService.createSeat(any(AddSeatRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/seat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /api/seat/create returns 400 when invalid payload")
    void create_invalid() throws Exception {
        var invalid = AddSeatRequestDTO.builder()
                .classFlightId(null) // @NotNull expected
                .seatCode("")        // @NotBlank expected
                .build();

        mockMvc.perform(post("/api/seat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(seatRestService, never()).createSeat(any());
    }

    @Test
    @DisplayName("POST /api/seat/create returns 500 when service throws")
    void create_exception() throws Exception {
        var req = AddSeatRequestDTO.builder()
                .classFlightId(10)
                .seatCode("EC001")
                .build();

        when(seatRestService.createSeat(any(AddSeatRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/seat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    // PUT /api/seat/update 200/400/404/500
    @Test
    @DisplayName("PUT /api/seat/update returns 200 when valid")
    void update_valid() throws Exception {
        var req = UpdateSeatRequestDTO.builder()
                .id(33)
                .passengerId(UUID.randomUUID())
                .build();
        var resp = sample(33, 11, req.getPassengerId(), "EC011", true);
        when(seatRestService.updateSeat(any(UpdateSeatRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/seat/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(33));
    }

    @Test
    @DisplayName("PUT /api/seat/update returns 400 when invalid payload")
    void update_invalid() throws Exception {
        var invalid = UpdateSeatRequestDTO.builder()
                .id(null)              // @NotNull expected
                .passengerId(null)     // allowed but keeping invalid id to trigger error
                .build();

        mockMvc.perform(put("/api/seat/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(seatRestService, never()).updateSeat(any());
    }

    @Test
    @DisplayName("PUT /api/seat/update returns 404 when not found")
    void update_notFound() throws Exception {
        var req = UpdateSeatRequestDTO.builder()
                .id(77)
                .passengerId(UUID.randomUUID())
                .build();

        when(seatRestService.updateSeat(any(UpdateSeatRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/seat/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/seat/update returns 500 when service throws")
    void update_exception() throws Exception {
        var req = UpdateSeatRequestDTO.builder()
                .id(44)
                .passengerId(UUID.randomUUID())
                .build();

        when(seatRestService.updateSeat(any(UpdateSeatRequestDTO.class)))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(put("/api/seat/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("fail")));
    }

    // POST /api/seat/delete/{id} 200/404/500
    @Test
    @DisplayName("POST /api/seat/delete/{id} returns 200 when found")
    void delete_found() throws Exception {
        var resp = sample(9, 10, null, "EC009", false);
        when(seatRestService.deleteSeat(9)).thenReturn(resp);

        mockMvc.perform(post("/api/seat/delete/{id}", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(9));
    }

    @Test
    @DisplayName("POST /api/seat/delete/{id} returns 404 when not found")
    void delete_notFound() throws Exception {
        when(seatRestService.deleteSeat(404)).thenReturn(null);

        mockMvc.perform(post("/api/seat/delete/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/seat/delete/{id} returns 500 when service throws")
    void delete_exception() throws Exception {
        when(seatRestService.deleteSeat(6)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/api/seat/delete/{id}", 6))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("error")));
    }
}
