package apap.ti._5.flight_2306211660_be.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.AddFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.UpdateFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restcontroller.flight.FlightRestController;
import apap.ti._5.flight_2306211660_be.restservice.flight.FlightRestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // not used but kept for parity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FlightRestControllerTest {

    @Mock
    private FlightRestService flightRestService;

    @InjectMocks
    private FlightRestController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        controller = new FlightRestController();
        ReflectionTestUtils.setField(controller, "flightRestService", flightRestService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private FlightResponseDTO sampleFlight(String id) {
        return FlightResponseDTO.builder()
                .id(id)
                .airlineId("AL-1")
                .airplaneId("AP-1")
                .originAirportCode("CGK")
                .destinationAirportCode("DPS")
                .departureTime(LocalDateTime.now().plusHours(1))
                .arrivalTime(LocalDateTime.now().plusHours(2))
                .terminal("T1")
                .gate("G1")
                .baggageAllowance(20)
                .facilities("WiFi")
                .status(1)
                .isDeleted(false)
                .classes(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/flight/all without filters -> 200")
    void getAllFlights_ok_noFilters() throws Exception {
        when(flightRestService.getAllFlightsWithFilters(null, null, null, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/flight/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(flightRestService).getAllFlightsWithFilters(null, null, null, null, null);
    }

    @Test
    @DisplayName("GET /api/flight/all with filters -> 200 and calls service with params")
    void getAllFlights_ok_withFilters() throws Exception {
        when(flightRestService.getAllFlightsWithFilters("CGK", "DPS", "AL-1", 3, true))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/flight/all")
                        .param("originAirportCode", "CGK")
                        .param("destinationAirportCode", "DPS")
                        .param("airlineId", "AL-1")
                        .param("status", "3")
                        .param("includeDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(flightRestService).getAllFlightsWithFilters("CGK", "DPS", "AL-1", 3, true);
    }

    @Test
    @DisplayName("GET /api/flight/all -> 500 on service exception")
    void getAllFlights_internalError() throws Exception {
        when(flightRestService.getAllFlightsWithFilters(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/flight/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("GET /api/flight/{id} found -> 200")
    void getFlight_found() throws Exception {
        var dto = sampleFlight("F-1");
        when(flightRestService.getFlightDetail("F-1")).thenReturn(dto);

        mockMvc.perform(get("/api/flight/F-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("F-1"));
    }

    @Test
    @DisplayName("GET /api/flight/{id} not found -> 404")
    void getFlight_notFound() throws Exception {
        when(flightRestService.getFlightDetail("NF")).thenReturn(null);

        mockMvc.perform(get("/api/flight/NF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/flight/create valid -> 201")
    void createFlight_valid() throws Exception {
        var now = LocalDateTime.now();
        var req = AddFlightRequestDTO.builder()
                .airlineId("AL-1")
                .airplaneId("AP-1")
                .originAirportCode("CGK")
                .destinationAirportCode("DPS")
                .departureTime(now.plusHours(1))
                .arrivalTime(now.plusHours(2))
                .terminal("T1")
                .gate("G1")
                .baggageAllowance(20)
                .classes(List.of(
                        AddClassFlightRequestDTO.builder().classType("economy").seatCapacity(2).build()
                ))
                .build();

        var resp = sampleFlight("NEW-1");
        when(flightRestService.createFlight(any(AddFlightRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/flight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("NEW-1"));
    }

    @Test
    @DisplayName("POST /api/flight/create invalid binding -> 400")
    void createFlight_invalidBinding() throws Exception {
        // Missing airlineId, airplaneId, times etc.
        var req = AddFlightRequestDTO.builder()
                .airlineId("") // NotBlank violation
                .airplaneId("")
                .originAirportCode("")
                .destinationAirportCode("")
                .terminal("")
                .gate("")
                .baggageAllowance(null) // NotNull
                .classes(Collections.emptyList()) // NotEmpty
                .build();

        mockMvc.perform(post("/api/flight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/flight/create service throws IllegalArgumentException -> 400")
    void createFlight_illegalArgument() throws Exception {
        var now = LocalDateTime.now();
        var req = AddFlightRequestDTO.builder()
                .airlineId("AL-1")
                .airplaneId("AP-1")
                .originAirportCode("CGK")
                .destinationAirportCode("DPS")
                .departureTime(now.plusHours(1))
                .arrivalTime(now.plusHours(2))
                .terminal("T1")
                .gate("G1")
                .baggageAllowance(20)
                .classes(List.of(
                        AddClassFlightRequestDTO.builder().classType("economy").seatCapacity(2).build()
                ))
                .build();

        when(flightRestService.createFlight(any(AddFlightRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(post("/api/flight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/flight/create service throws Exception -> 500")
    void createFlight_internalError() throws Exception {
        var now = LocalDateTime.now();
        var req = AddFlightRequestDTO.builder()
                .airlineId("AL-1")
                .airplaneId("AP-1")
                .originAirportCode("CGK")
                .destinationAirportCode("DPS")
                .departureTime(now.plusHours(1))
                .arrivalTime(now.plusHours(2))
                .terminal("T1")
                .gate("G1")
                .baggageAllowance(20)
                .classes(List.of(
                        AddClassFlightRequestDTO.builder().classType("economy").seatCapacity(2).build()
                ))
                .build();

        when(flightRestService.createFlight(any(AddFlightRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/flight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("PUT /api/flight/update valid -> 200")
    void updateFlight_valid() throws Exception {
        var now = LocalDateTime.now();
        var req = UpdateFlightRequestDTO.builder()
                .id("F-1")
                .departureTime(now.plusHours(2))
                .arrivalTime(now.plusHours(3))
                .terminal("T2")
                .gate("G2")
                .baggageAllowance(25)
                .facilities("WiFi")
                .classes(Collections.emptyList())
                .build();

        var resp = sampleFlight("F-1");
        when(flightRestService.updateFlight(any(UpdateFlightRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/flight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("F-1"));
    }

    @Test
    @DisplayName("PUT /api/flight/update invalid binding -> 400")
    void updateFlight_invalidBinding() throws Exception {
        var req = UpdateFlightRequestDTO.builder()
                .id("") // NotBlank
                .terminal("") // NotBlank
                .gate("") // NotBlank
                .baggageAllowance(null) // NotNull
                .build();

        mockMvc.perform(put("/api/flight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/flight/update not found -> 404")
    void updateFlight_notFound() throws Exception {
        var now = LocalDateTime.now();
        var req = UpdateFlightRequestDTO.builder()
                .id("NF")
                .departureTime(now.plusHours(2))
                .arrivalTime(now.plusHours(3))
                .terminal("T2")
                .gate("G2")
                .baggageAllowance(25)
                .build();

        when(flightRestService.updateFlight(any(UpdateFlightRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/flight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/flight/update IllegalArgument/State -> 400")
    void updateFlight_illegalArgsOrState() throws Exception {
        var now = LocalDateTime.now();
        var req = UpdateFlightRequestDTO.builder()
                .id("F-ERR")
                .departureTime(now.plusHours(2))
                .arrivalTime(now.plusHours(3))
                .terminal("T2")
                .gate("G2")
                .baggageAllowance(25)
                .build();

        when(flightRestService.updateFlight(any(UpdateFlightRequestDTO.class)))
                .thenThrow(new IllegalStateException("bad"));

        mockMvc.perform(put("/api/flight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/flight/update Exception -> 500")
    void updateFlight_internalError() throws Exception {
        var now = LocalDateTime.now();
        var req = UpdateFlightRequestDTO.builder()
                .id("F-ERR2")
                .departureTime(now.plusHours(2))
                .arrivalTime(now.plusHours(3))
                .terminal("T2")
                .gate("G2")
                .baggageAllowance(25)
                .build();

        when(flightRestService.updateFlight(any(UpdateFlightRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/flight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("POST /api/flight/delete/{id} valid -> 200")
    void deleteFlight_valid() throws Exception {
        var resp = sampleFlight("DEL-1");
        when(flightRestService.deleteFlight("DEL-1")).thenReturn(resp);

        mockMvc.perform(post("/api/flight/delete/DEL-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("DEL-1"));
    }

    @Test
    @DisplayName("POST /api/flight/delete/{id} not found -> 404")
    void deleteFlight_notFound() throws Exception {
        when(flightRestService.deleteFlight("NF")).thenReturn(null);

        mockMvc.perform(post("/api/flight/delete/NF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/flight/delete/{id} IllegalState -> 400")
    void deleteFlight_illegalState() throws Exception {
        when(flightRestService.deleteFlight("ERR")).thenThrow(new IllegalStateException("nope"));

        mockMvc.perform(post("/api/flight/delete/ERR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/flight/delete/{id} Exception -> 500")
    void deleteFlight_internalError() throws Exception {
        when(flightRestService.deleteFlight("ERR2")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/flight/delete/ERR2"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
