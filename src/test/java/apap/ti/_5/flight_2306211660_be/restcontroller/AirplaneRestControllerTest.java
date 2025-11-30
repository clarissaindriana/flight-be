package apap.ti._5.flight_2306211660_be.restcontroller;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.flight_2306211660_be.restcontroller.airplane.AirplaneRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.AddAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.UpdateAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airplane.AirplaneResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airplane.AirplaneRestService;

@ExtendWith(MockitoExtension.class)
class AirplaneRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AirplaneRestService airplaneRestService;

    @BeforeEach
    void setup() {
        AirplaneRestController controller = new AirplaneRestController();
        ReflectionTestUtils.setField(controller, "airplaneRestService", airplaneRestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private AirplaneResponseDTO sample(String id, String airlineId, String model, int seats, int year, boolean isDeleted) {
        return AirplaneResponseDTO.builder()
                .id(id)
                .airlineId(airlineId)
                .model(model)
                .seatCapacity(seats)
                .manufactureYear(year)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .isDeleted(isDeleted)
                .build();
    }

    @Test
    @DisplayName("GET /api/airplane/all returns 200 with sorted list without filters")
    void getAllAirplanes_noFilters() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        var a3 = sample("GA-002", "GA", "B737-800", 186, 2020, true);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2, a3));

        mockMvc.perform(get("/api/airplane/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                // Sorted ascending by id: GA-001, GA-002, SQ-001
                .andExpect(jsonPath("$.data[0].id").value("GA-001"))
                .andExpect(jsonPath("$.data[1].id").value("GA-002"))
                .andExpect(jsonPath("$.data[2].id").value("SQ-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with isDeleted filter")
    void getAllAirplanes_filter_isDeleted() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        var a3 = sample("GA-002", "GA", "B737-800", 186, 2020, true);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2, a3));

        mockMvc.perform(get("/api/airplane/all").param("isDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("GA-002"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with search filter")
    void getAllAirplanes_filter_search() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        var a3 = sample("GA-002", "GA", "B737-800", 186, 2020, true);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2, a3));

        mockMvc.perform(get("/api/airplane/all").param("search", "ga-"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("GA-001"))
                .andExpect(jsonPath("$.data[1].id").value("GA-002"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with search filter matching model")
    void getAllAirplanes_filter_search_model() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/airplane/all").param("search", "b73"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("GA-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with search filter matching airlineId")
    void getAllAirplanes_filter_search_airlineId() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/airplane/all").param("search", "sq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("SQ-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with airlineId and model filter")
    void getAllAirplanes_filter_airlineId_model() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        var a3 = sample("GA-002", "GA", "B737-800", 186, 2020, true);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2, a3));

        mockMvc.perform(get("/api/airplane/all").param("airlineId", "SQ").param("model", "A320"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("SQ-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/all with manufactureYear filter")
    void getAllAirplanes_filter_manufactureYear() throws Exception {
        var a1 = sample("GA-001", "GA", "B737", 180, 2018, false);
        var a2 = sample("SQ-001", "SQ", "A320", 150, 2019, false);
        var a3 = sample("GA-002", "GA", "B737-800", 186, 2019, true);
        when(airplaneRestService.getAllAirplanes()).thenReturn(Arrays.asList(a1, a2, a3));

        mockMvc.perform(get("/api/airplane/all").param("manufactureYear", "2019"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("GA-002"))
                .andExpect(jsonPath("$.data[1].id").value("SQ-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/all returns 500 on service error")
    void getAllAirplanes_exception() throws Exception {
        when(airplaneRestService.getAllAirplanes()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/airplane/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("GET /api/airplane/{id} returns 200 when found")
    void getAirplane_found() throws Exception {
        var resp = sample("GA-001", "GA", "B737", 180, 2018, false);
        when(airplaneRestService.getAirplane("GA-001")).thenReturn(resp);

        mockMvc.perform(get("/api/airplane/{id}", "GA-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA-001"));
    }

    @Test
    @DisplayName("GET /api/airplane/{id} returns 404 when not found")
    void getAirplane_notFound() throws Exception {
        when(airplaneRestService.getAirplane("X")).thenReturn(null);

        mockMvc.perform(get("/api/airplane/{id}", "X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/airplane/create returns 201 when valid")
    void createAirplane_valid() throws Exception {
        var request = AddAirplaneRequestDTO.builder()
                .airlineId("GA").model("B737").seatCapacity(180).manufactureYear(2018).build();
        var resp = sample("GA-ABC", "GA", "B737", 180, 2018, false);
        when(airplaneRestService.createAirplane(any(AddAirplaneRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/airplane/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("GA-ABC"));
    }

    @Test
    @DisplayName("POST /api/airplane/create returns 400 when invalid payload")
    void createAirplane_invalid() throws Exception {
        var invalid = AddAirplaneRequestDTO.builder()
                .airlineId("") // NotBlank
                .model("")     // NotBlank
                .seatCapacity(null) // NotNull
                .manufactureYear(null) // NotNull
                .build();

        mockMvc.perform(post("/api/airplane/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airplaneRestService, never()).createAirplane(any());
    }

    @Test
    @DisplayName("POST /api/airplane/create returns 400 for IllegalArgumentException")
    void createAirplane_illegalArgument() throws Exception {
        var request = AddAirplaneRequestDTO.builder()
                .airlineId("GA").model("B737").seatCapacity(180)
                .manufactureYear(LocalDateTime.now().getYear() + 1).build();

        when(airplaneRestService.createAirplane(any(AddAirplaneRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Manufacture year cannot be in the future"));

        mockMvc.perform(post("/api/airplane/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Manufacture year")));
    }

    @Test
    @DisplayName("POST /api/airplane/create returns 500 on other exceptions")
    void createAirplane_exception() throws Exception {
        var request = AddAirplaneRequestDTO.builder()
                .airlineId("GA").model("B737").seatCapacity(180).manufactureYear(2018).build();

        when(airplaneRestService.createAirplane(any(AddAirplaneRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/airplane/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("PUT /api/airplane/update returns 200 when valid")
    void updateAirplane_valid() throws Exception {
        var request = UpdateAirplaneRequestDTO.builder()
                .id("GA-ABC").model("B737-800").seatCapacity(186).manufactureYear(2019).build();
        var resp = sample("GA-ABC", "GA", "B737-800", 186, 2019, false);
        when(airplaneRestService.updateAirplane(any(UpdateAirplaneRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/airplane/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA-ABC"));
    }

    @Test
    @DisplayName("PUT /api/airplane/update returns 400 when invalid payload")
    void updateAirplane_invalid() throws Exception {
        var invalid = UpdateAirplaneRequestDTO.builder()
                .id("") // NotBlank
                .model("") // NotBlank
                .seatCapacity(null) // NotNull
                .manufactureYear(null) // NotNull
                .build();

        mockMvc.perform(put("/api/airplane/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airplaneRestService, never()).updateAirplane(any());
    }

    @Test
    @DisplayName("PUT /api/airplane/update returns 404 when not found")
    void updateAirplane_notFound() throws Exception {
        var request = UpdateAirplaneRequestDTO.builder()
                .id("X").model("B777").seatCapacity(300).manufactureYear(2015).build();
        when(airplaneRestService.updateAirplane(any(UpdateAirplaneRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/airplane/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/airplane/update returns 400 when IllegalStateException")
    void updateAirplane_illegalState() throws Exception {
        var request = UpdateAirplaneRequestDTO.builder()
                .id("GA-DEL").model("B737-800").seatCapacity(186).manufactureYear(2018).build();
        when(airplaneRestService.updateAirplane(any(UpdateAirplaneRequestDTO.class)))
                .thenThrow(new IllegalStateException("Cannot update deleted airplane"));

        mockMvc.perform(put("/api/airplane/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Cannot update")));
    }

    @Test
    @DisplayName("PUT /api/airplane/update returns 500 on other exceptions")
    void updateAirplane_exception() throws Exception {
        var request = UpdateAirplaneRequestDTO.builder()
                .id("GA-ABC").model("B737-800").seatCapacity(186).manufactureYear(2018).build();
        when(airplaneRestService.updateAirplane(any(UpdateAirplaneRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/airplane/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/delete returns 200 when found")
    void deleteAirplane_found() throws Exception {
        var resp = sample("GA-ABC", "GA", "B737", 180, 2018, true);
        when(airplaneRestService.deleteAirplane(eq("GA-ABC"))).thenReturn(resp);

        mockMvc.perform(post("/api/airplane/{id}/delete", "GA-ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA-ABC"));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/delete returns 404 when not found")
    void deleteAirplane_notFound() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("X"))).thenReturn(null);

        mockMvc.perform(post("/api/airplane/{id}/delete", "X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/delete returns 400 when IllegalStateException")
    void deleteAirplane_illegalState() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("GA-ABC")))
                .thenThrow(new IllegalStateException("Pesawat tidak dapat dinonaktifkan"));

        mockMvc.perform(post("/api/airplane/{id}/delete", "GA-ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Pesawat")));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/delete returns 500 on other exceptions")
    void deleteAirplane_exception() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("GA-ABC")))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/airplane/{id}/delete", "GA-ABC"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/activate returns 200 when found")
    void activateAirplane_found() throws Exception {
        var resp = sample("GA-DEL", "GA", "B737", 180, 2018, false);
        when(airplaneRestService.activateAirplane(eq("GA-DEL"))).thenReturn(resp);

        mockMvc.perform(post("/api/airplane/{id}/activate", "GA-DEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA-DEL"));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/activate returns 404 when not found")
    void activateAirplane_notFound() throws Exception {
        when(airplaneRestService.activateAirplane(eq("X"))).thenReturn(null);

        mockMvc.perform(post("/api/airplane/{id}/activate", "X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/activate returns 400 when IllegalStateException")
    void activateAirplane_illegalState() throws Exception {
        when(airplaneRestService.activateAirplane(eq("GA-ABC")))
                .thenThrow(new IllegalStateException("Airplane is already active"));

        mockMvc.perform(post("/api/airplane/{id}/activate", "GA-ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("already active")));
    }

    @Test
    @DisplayName("POST /api/airplane/{id}/activate returns 500 on other exceptions")
    void activateAirplane_exception() throws Exception {
        when(airplaneRestService.activateAirplane(eq("GA-ABC")))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/airplane/{id}/activate", "GA-ABC"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("DELETE /api/airplane/{id} returns 200 when found")
    void deleteAirplaneByDeleteVerb_found() throws Exception {
        var resp = sample("GA-ABC", "GA", "B737", 180, 2018, true);
        when(airplaneRestService.deleteAirplane(eq("GA-ABC"))).thenReturn(resp);

        mockMvc.perform(delete("/api/airplane/{id}", "GA-ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA-ABC"));
    }

    @Test
    @DisplayName("DELETE /api/airplane/{id} returns 404 when not found")
    void deleteAirplaneByDeleteVerb_notFound() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("X"))).thenReturn(null);

        mockMvc.perform(delete("/api/airplane/{id}", "X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/airplane/{id} returns 400 when IllegalStateException")
    void deleteAirplaneByDeleteVerb_illegalState() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("GA-ABC")))
                .thenThrow(new IllegalStateException("Pesawat tidak dapat dinonaktifkan"));

        mockMvc.perform(delete("/api/airplane/{id}", "GA-ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Pesawat")));
    }

    @Test
    @DisplayName("DELETE /api/airplane/{id} returns 500 on other exceptions")
    void deleteAirplaneByDeleteVerb_exception() throws Exception {
        when(airplaneRestService.deleteAirplane(eq("GA-ABC")))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(delete("/api/airplane/{id}", "GA-ABC"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }
}
