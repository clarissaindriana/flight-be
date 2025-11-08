package apap.ti._5.flight_2306211660_be.restcontroller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

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
import com.fasterxml.jackson.databind.SerializationFeature; // not used but reserved
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.flight_2306211660_be.restcontroller.classFlight.ClassFlightRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.classFlight.ClassFlightRestService;

@ExtendWith(MockitoExtension.class)
class ClassFlightRestControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ClassFlightRestService classFlightRestService;

    @BeforeEach
    void setup() {
        var controller = new ClassFlightRestController();
        ReflectionTestUtils.setField(controller, "classFlightRestService", classFlightRestService);

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

    private ClassFlightResponseDTO sample(Integer id, String flightId, String classType, int seatCap, int available, BigDecimal price) {
        return ClassFlightResponseDTO.builder()
                .id(id)
                .flightId(flightId)
                .classType(classType)
                .seatCapacity(seatCap)
                .availableSeats(available)
                .price(price)
                .build();
    }

    @Test
    @DisplayName("GET /api/classFlight returns 200 with all list when no flightId filter")
    void getAll_noFilter() throws Exception {
        var c1 = sample(1, "FL-1", "economy", 10, 10, new BigDecimal("1000000"));
        var c2 = sample(2, "FL-2", "business", 5, 5, new BigDecimal("2500000"));
        when(classFlightRestService.getAllClassFlights()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/classFlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));

        verify(classFlightRestService).getAllClassFlights();
        verify(classFlightRestService, never()).getClassFlightsByFlight(anyString());
    }

    @Test
    @DisplayName("GET /api/classFlight with flightId filter returns 200 with filtered list")
    void getAll_withFilter() throws Exception {
        var c1 = sample(3, "FL-X", "first", 2, 2, new BigDecimal("5000000"));
        when(classFlightRestService.getClassFlightsByFlight("FL-X")).thenReturn(Collections.singletonList(c1));

        mockMvc.perform(get("/api/classFlight").param("flightId", "FL-X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].flightId").value("FL-X"));

        verify(classFlightRestService).getClassFlightsByFlight("FL-X");
        verify(classFlightRestService, never()).getAllClassFlights();
    }

    @Test
    @DisplayName("GET /api/classFlight/{id} returns 200 when found")
    void getById_found() throws Exception {
        var c = sample(10, "FL-10", "economy", 20, 20, new BigDecimal("1500000"));
        when(classFlightRestService.getClassFlight(10)).thenReturn(c);

        mockMvc.perform(get("/api/classFlight/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("GET /api/classFlight/{id} returns 404 when not found")
    void getById_notFound() throws Exception {
        when(classFlightRestService.getClassFlight(404)).thenReturn(null);

        mockMvc.perform(get("/api/classFlight/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/classFlight/create returns 201 when valid")
    void create_valid() throws Exception {
        var req = AddClassFlightRequestDTO.builder()
                .flightId("FL-NEW")
                .classType("economy")
                .seatCapacity(12)
                .price(new BigDecimal("1234567"))
                .build();
        var resp = sample(55, "FL-NEW", "economy", 12, 12, new BigDecimal("1234567"));
        when(classFlightRestService.createClassFlight(any(AddClassFlightRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/classFlight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(55));
    }

    @Test
    @DisplayName("POST /api/classFlight/create returns 400 when invalid payload (BindingResult has errors)")
    void create_invalid() throws Exception {
        var invalid = AddClassFlightRequestDTO.builder()
                .flightId("") // NotBlank
                .classType("") // NotBlank
                .seatCapacity(null) // NotNull
                .price(null) // NotNull
                .build();

        mockMvc.perform(post("/api/classFlight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(classFlightRestService, never()).createClassFlight(any());
    }

    @Test
    @DisplayName("POST /api/classFlight/create returns 500 when service throws")
    void create_exception() throws Exception {
        var req = AddClassFlightRequestDTO.builder()
                .flightId("FL-EX")
                .classType("business")
                .seatCapacity(8)
                .price(new BigDecimal("3000000"))
                .build();

        when(classFlightRestService.createClassFlight(any(AddClassFlightRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/classFlight/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("PUT /api/classFlight/update returns 200 when valid")
    void update_valid() throws Exception {
        var req = UpdateClassFlightRequestDTO.builder()
                .id(77)
                .seatCapacity(30)
                .price(new BigDecimal("2000000"))
                .build();
        var resp = sample(77, "FL-U", "economy", 30, 30, new BigDecimal("2000000"));
        when(classFlightRestService.updateClassFlight(any(UpdateClassFlightRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/classFlight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(77));
    }

    @Test
    @DisplayName("PUT /api/classFlight/update returns 400 when invalid payload")
    void update_invalid() throws Exception {
        var invalid = UpdateClassFlightRequestDTO.builder()
                .id(null) // NotNull
                .seatCapacity(null) // NotNull
                .price(null) // NotNull
                .build();

        mockMvc.perform(put("/api/classFlight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(classFlightRestService, never()).updateClassFlight(any());
    }

    @Test
    @DisplayName("PUT /api/classFlight/update returns 404 when not found")
    void update_notFound() throws Exception {
        var req = UpdateClassFlightRequestDTO.builder()
                .id(88)
                .seatCapacity(40)
                .price(new BigDecimal("3500000"))
                .build();

        when(classFlightRestService.updateClassFlight(any(UpdateClassFlightRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/classFlight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/classFlight/update returns 500 when service throws")
    void update_exception() throws Exception {
        var req = UpdateClassFlightRequestDTO.builder()
                .id(90)
                .seatCapacity(35)
                .price(new BigDecimal("3300000"))
                .build();

        when(classFlightRestService.updateClassFlight(any(UpdateClassFlightRequestDTO.class)))
                .thenThrow(new RuntimeException("fail"));

        mockMvc.perform(put("/api/classFlight/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("fail")));
    }

    @Test
    @DisplayName("POST /api/classFlight/delete/{id} returns 200 when found")
    void delete_found() throws Exception {
        var resp = sample(5, "FL-D", "business", 10, 10, new BigDecimal("4000000"));
        when(classFlightRestService.deleteClassFlight(5)).thenReturn(resp);

        mockMvc.perform(post("/api/classFlight/delete/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    @DisplayName("POST /api/classFlight/delete/{id} returns 404 when not found")
    void delete_notFound() throws Exception {
        when(classFlightRestService.deleteClassFlight(404)).thenReturn(null);

        mockMvc.perform(post("/api/classFlight/delete/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/classFlight/delete/{id} returns 500 when service throws")
    void delete_exception() throws Exception {
        when(classFlightRestService.deleteClassFlight(6)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/classFlight/delete/{id}", 6))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }
}
