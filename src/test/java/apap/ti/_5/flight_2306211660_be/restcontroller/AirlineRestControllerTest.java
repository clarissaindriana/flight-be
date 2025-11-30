package apap.ti._5.flight_2306211660_be.restcontroller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

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

import apap.ti._5.flight_2306211660_be.restcontroller.airline.AirlineRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.UpdateAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airline.AirlineRestService;

@ExtendWith(MockitoExtension.class)
class AirlineRestControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AirlineRestService airlineRestService;

    @BeforeEach
    void setup() {
        AirlineRestController controller = new AirlineRestController();
        ReflectionTestUtils.setField(controller, "airlineRestService", airlineRestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private AirlineResponseDTO sample(String id, String name, String country) {
        return AirlineResponseDTO.builder()
                .id(id)
                .name(name)
                .country(country)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .deletedAt(null)
                .build();
    }

    @Test
    @DisplayName("GET /api/airline/all without search returns 200 with list")
    void getAllAirlines_noSearch() throws Exception {
        var a1 = sample("GA", "Garuda Indonesia", "Indonesia");
        var a2 = sample("SQ", "Singapore Airlines", "Singapore");

        when(airlineRestService.getAllAirlines()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/airline/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("GA"))
                .andExpect(jsonPath("$.data[1].id").value("SQ"));

        verify(airlineRestService).getAllAirlines();
        verify(airlineRestService, never()).searchAirlinesByName(any());
    }

    @Test
    @DisplayName("GET /api/airline/all with search returns 200 with filtered list")
    void getAllAirlines_withSearch() throws Exception {
        var a1 = sample("GA", "Garuda Indonesia", "Indonesia");
        when(airlineRestService.searchAirlinesByName("gar")).thenReturn(Collections.singletonList(a1));

        mockMvc.perform(get("/api/airline/all").param("search", "gar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("GA"));

        verify(airlineRestService).searchAirlinesByName("gar");
        verify(airlineRestService, never()).getAllAirlines();
    }

    @Test
    @DisplayName("GET /api/airline/{id} returns 200 when found")
    void getAirline_found() throws Exception {
        var a1 = sample("GA", "Garuda Indonesia", "Indonesia");
        when(airlineRestService.getAirline("GA")).thenReturn(a1);

        mockMvc.perform(get("/api/airline/{id}", "GA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA"))
                .andExpect(jsonPath("$.message").exists());

        verify(airlineRestService).getAirline("GA");
    }

    @Test
    @DisplayName("GET /api/airline/{id} returns 404 when not found")
    void getAirline_notFound() throws Exception {
        when(airlineRestService.getAirline("XX")).thenReturn(null);

        mockMvc.perform(get("/api/airline/{id}", "XX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(airlineRestService).getAirline("XX");
    }

    @Test
    @DisplayName("POST /api/airline/create returns 201 when valid")
    void createAirline_valid() throws Exception {
        var request = AddAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("Indonesia")
                .build();
        var response = sample("GA", "Garuda Indonesia", "Indonesia");

        when(airlineRestService.createAirline(any(AddAirlineRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/airline/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("GA"));

        verify(airlineRestService).createAirline(any(AddAirlineRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/airline/create returns 400 when invalid payload")
    void createAirline_invalid() throws Exception {
        var invalid = AddAirlineRequestDTO.builder()
                .id("")
                .name("")
                .country("")
                .build();

        mockMvc.perform(post("/api/airline/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airlineRestService, never()).createAirline(any());
    }

    @Test
    @DisplayName("POST /api/airline/create returns 500 when service returns null")
    void createAirline_internalError() throws Exception {
        var request = AddAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("Indonesia")
                .build();

        when(airlineRestService.createAirline(any(AddAirlineRequestDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/airline/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("PUT /api/airline/update returns 200 when valid")
    void updateAirline_valid() throws Exception {
        var request = UpdateAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("Indonesia")
                .build();

        var response = sample("GA", "Garuda Indonesia", "Indonesia");
        when(airlineRestService.updateAirline(any(UpdateAirlineRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/airline/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA"));
    }

    @Test
    @DisplayName("PUT /api/airline/update returns 400 when invalid payload")
    void updateAirline_invalid() throws Exception {
        var invalid = UpdateAirlineRequestDTO.builder()
                .id("")
                .name("")
                .country("")
                .build();

        mockMvc.perform(put("/api/airline/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airlineRestService, never()).updateAirline(any());
    }

    @Test
    @DisplayName("PUT /api/airline/update returns 404 when not found")
    void updateAirline_notFound() throws Exception {
        var request = UpdateAirlineRequestDTO.builder()
                .id("XX")
                .name("Unknown")
                .country("N/A")
                .build();

        when(airlineRestService.updateAirline(any(UpdateAirlineRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/airline/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/airline/update returns 500 when exception thrown")
    void updateAirline_exception() throws Exception {
        var request = UpdateAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("Indonesia")
                .build();

        when(airlineRestService.updateAirline(any(UpdateAirlineRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/airline/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("DELETE /api/airline/delete/{id} returns 200 when found")
    void deleteAirline_found() throws Exception {
        var response = sample("GA", "Garuda", "Indonesia");
        when(airlineRestService.deleteAirline(eq("GA"))).thenReturn(response);

        mockMvc.perform(delete("/api/airline/delete/{id}", "GA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("GA"));
    }

    @Test
    @DisplayName("DELETE /api/airline/delete/{id} returns 404 when not found")
    void deleteAirline_notFound() throws Exception {
        when(airlineRestService.deleteAirline(eq("XX"))).thenReturn(null);

        mockMvc.perform(delete("/api/airline/delete/{id}", "XX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/airline/delete/{id} returns 500 when exception thrown")
    void deleteAirline_exception() throws Exception {
        when(airlineRestService.deleteAirline(eq("GA"))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(delete("/api/airline/delete/{id}", "GA"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("fail")));
    }

    @Test
    @DisplayName("GET /api/airline/total returns 200 with total count")
    void getTotalAirlines_success() throws Exception {
        when(airlineRestService.getTotalAirlines()).thenReturn(5L);

        mockMvc.perform(get("/api/airline/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(5))
                .andExpect(jsonPath("$.message").value("Total airlines retrieved"));
    }

    @Test
    @DisplayName("GET /api/airline/total returns 500 when exception thrown")
    void getTotalAirlines_exception() throws Exception {
        when(airlineRestService.getTotalAirlines()).thenThrow(new RuntimeException("db error"));

        mockMvc.perform(get("/api/airline/total"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error: db error"));
    }
}
