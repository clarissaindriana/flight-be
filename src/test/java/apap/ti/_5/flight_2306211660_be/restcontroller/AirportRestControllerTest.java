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

import apap.ti._5.flight_2306211660_be.restcontroller.airport.AirportRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.UpdateAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airport.AirportResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airport.AirportRestService;

@ExtendWith(MockitoExtension.class)
class AirportRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AirportRestService airportRestService;

    @BeforeEach
    void setup() {
        AirportRestController controller = new AirportRestController();
        ReflectionTestUtils.setField(controller, "airportRestService", airportRestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private AirportResponseDTO sample(
            String code,
            String name,
            String city,
            String country,
            Double lat,
            Double lng,
            String tz
    ) {
        return AirportResponseDTO.builder()
                .iataCode(code)
                .name(name)
                .city(city)
                .country(country)
                .latitude(lat)
                .longitude(lng)
                .timezone(tz)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("GET /api/airport/all without search returns 200 with list")
    void getAllAirports_noSearch() throws Exception {
        var a1 = sample("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta");
        var a2 = sample("SIN", "Changi", "Singapore", "Singapore", 1.36, 103.99, "Asia/Singapore");
        when(airportRestService.getAllAirports()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/airport/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].iataCode").value("CGK"))
                .andExpect(jsonPath("$.data[1].iataCode").value("SIN"));

        verify(airportRestService).getAllAirports();
        verify(airportRestService, never()).searchAirportsByName(any());
    }

    @Test
    @DisplayName("GET /api/airport/all with search returns 200 with filtered list")
    void getAllAirports_withSearch() throws Exception {
        var a1 = sample("CGK", "Soekarno Hatta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta");
        when(airportRestService.searchAirportsByName("soekarno")).thenReturn(Collections.singletonList(a1));

        mockMvc.perform(get("/api/airport/all").param("search", "soekarno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].iataCode").value("CGK"));

        verify(airportRestService).searchAirportsByName("soekarno");
        verify(airportRestService, never()).getAllAirports();
    }

    @Test
    @DisplayName("GET /api/airport/{code} returns 200 when found")
    void getAirport_found() throws Exception {
        var a1 = sample("CGK", "Soekarno Hatta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta");
        when(airportRestService.getAirport("CGK")).thenReturn(a1);

        mockMvc.perform(get("/api/airport/{iataCode}", "CGK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.iataCode").value("CGK"));
    }

    @Test
    @DisplayName("GET /api/airport/{code} returns 404 when not found")
    void getAirport_notFound() throws Exception {
        when(airportRestService.getAirport("XXX")).thenReturn(null);

        mockMvc.perform(get("/api/airport/{iataCode}", "XXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/airport/create returns 201 when valid")
    void createAirport_valid() throws Exception {
        var request = AddAirportRequestDTO.builder()
                .iataCode("CGK").name("Soekarno Hatta International Airport")
                .city("Jakarta").country("Indonesia")
                .latitude(-6.125556).longitude(106.655833).timezone("Asia/Jakarta")
                .build();
        var resp = sample("CGK", "Soekarno Hatta International Airport", "Jakarta", "Indonesia", -6.125556, 106.655833, "Asia/Jakarta");
        when(airportRestService.createAirport(any(AddAirportRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/airport/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.iataCode").value("CGK"));
    }

    @Test
    @DisplayName("POST /api/airport/create returns 400 when invalid payload")
    void createAirport_invalid() throws Exception {
        var invalid = AddAirportRequestDTO.builder()
                .iataCode("") // NotBlank
                .name("") // NotBlank
                .city("") // NotBlank
                .country("") // NotBlank
                .build();

        mockMvc.perform(post("/api/airport/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airportRestService, never()).createAirport(any());
    }

    @Test
    @DisplayName("POST /api/airport/create returns 500 when service returns null")
    void createAirport_internalError() throws Exception {
        var request = AddAirportRequestDTO.builder()
                .iataCode("CGK").name("Soekarno Hatta International Airport")
                .city("Jakarta").country("Indonesia")
                .latitude(-6.125556).longitude(106.655833).timezone("Asia/Jakarta")
                .build();

        when(airportRestService.createAirport(any(AddAirportRequestDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/airport/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("PUT /api/airport/update returns 200 when valid")
    void updateAirport_valid() throws Exception {
        var request = UpdateAirportRequestDTO.builder()
                .iataCode("CGK")
                .name("Soekarno Hatta International Airport")
                .city("Tangerang")
                .country("Indonesia")
                .latitude(-6.125556)
                .longitude(106.655833)
                .timezone("Asia/Jakarta")
                .build();

        var resp = sample("CGK", "Soekarno Hatta International Airport", "Tangerang", "Indonesia", -6.125556, 106.655833, "Asia/Jakarta");
        when(airportRestService.updateAirport(any(UpdateAirportRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/airport/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.iataCode").value("CGK"));
    }

    @Test
    @DisplayName("PUT /api/airport/update returns 400 when invalid payload")
    void updateAirport_invalid() throws Exception {
        var invalid = UpdateAirportRequestDTO.builder()
                .iataCode("") // NotBlank
                .name("") // NotBlank
                .city("") // NotBlank
                .country("") // NotBlank
                .build();

        mockMvc.perform(put("/api/airport/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(airportRestService, never()).updateAirport(any());
    }

    @Test
    @DisplayName("PUT /api/airport/update returns 404 when not found")
    void updateAirport_notFound() throws Exception {
        var request = UpdateAirportRequestDTO.builder()
                .iataCode("XXX").name("Unknown").city("N/A").country("N/A").build();

        when(airportRestService.updateAirport(any(UpdateAirportRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/airport/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/airport/update returns 500 when exception thrown")
    void updateAirport_exception() throws Exception {
        var request = UpdateAirportRequestDTO.builder()
                .iataCode("CGK").name("Soekarno Hatta International Airport")
                .city("Tangerang").country("Indonesia")
                .latitude(-6.125556).longitude(106.655833).timezone("Asia/Jakarta")
                .build();

        when(airportRestService.updateAirport(any(UpdateAirportRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/airport/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("boom")));
    }

    @Test
    @DisplayName("DELETE /api/airport/delete/{code} returns 200 when found")
    void deleteAirport_found() throws Exception {
        var resp = sample("CGK", "Soekarno Hatta", "Tangerang", "Indonesia", -6.125556, 106.655833, "Asia/Jakarta");
        when(airportRestService.deleteAirport(eq("CGK"))).thenReturn(resp);

        mockMvc.perform(delete("/api/airport/delete/{iataCode}", "CGK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.iataCode").value("CGK"));
    }

    @Test
    @DisplayName("DELETE /api/airport/delete/{code} returns 404 when not found")
    void deleteAirport_notFound() throws Exception {
        when(airportRestService.deleteAirport(eq("XXX"))).thenReturn(null);

        mockMvc.perform(delete("/api/airport/delete/{iataCode}", "XXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/airport/delete/{code} returns 500 when exception thrown")
    void deleteAirport_exception() throws Exception {
        when(airportRestService.deleteAirport(eq("CGK"))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(delete("/api/airport/delete/{iataCode}", "CGK"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("fail")));
    }
}
