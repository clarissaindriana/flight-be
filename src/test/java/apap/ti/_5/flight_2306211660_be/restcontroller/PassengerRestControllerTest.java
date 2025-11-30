package apap.ti._5.flight_2306211660_be.restcontroller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.restcontroller.passenger.PassengerRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.passenger.PassengerRestService;

@ExtendWith(MockitoExtension.class)
class PassengerRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PassengerRestService passengerRestService;

    @Mock
    private ProfileClient profileClient;

    @Mock
    private PassengerRepository passengerRepository;

    @BeforeEach
    void setup() {
        PassengerRestController controller = new PassengerRestController();
        ReflectionTestUtils.setField(controller, "passengerRestService", passengerRestService);
        ReflectionTestUtils.setField(controller, "profileClient", profileClient);
        ReflectionTestUtils.setField(controller, "passengerRepository", passengerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        // Configure ObjectMapper to support Java Time (LocalDate) in request bodies
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private PassengerResponseDTO sample(UUID id) {
        return PassengerResponseDTO.builder()
                .id(id)
                .fullName("John Doe")
                .birthDate(LocalDate.of(1990,1,1))
                .gender(1)
                .idPassport("P123")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("GET /api/passenger returns 200 with list")
    void getAllPassengers_ok() throws Exception {
        var p1 = sample(UUID.randomUUID());
        var p2 = sample(UUID.randomUUID());
        when(passengerRestService.getAllPassengers()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/passenger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(p1.getId().toString()))
                .andExpect(jsonPath("$.data[1].id").value(p2.getId().toString()));

        verify(passengerRestService).getAllPassengers();
    }

    @Test
    @DisplayName("GET /api/passenger/{id} 200 when found")
    void getPassenger_found() throws Exception {
        UUID id = UUID.randomUUID();
        when(passengerRestService.getPassenger(id)).thenReturn(sample(id));

        mockMvc.perform(get("/api/passenger/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/passenger/{id} 404 when not found")
    void getPassenger_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(passengerRestService.getPassenger(id)).thenReturn(null);

        mockMvc.perform(get("/api/passenger/{id}", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/passenger/create 201 when valid")
    void createPassenger_valid() throws Exception {
        UUID id = UUID.randomUUID();
        var request = AddPassengerRequestDTO.builder()
                .fullName("Jane")
                .birthDate(LocalDate.of(1991,2,2))
                .gender(2)
                .idPassport("P999")
                .build();
        when(passengerRestService.createPassenger(any(AddPassengerRequestDTO.class))).thenReturn(sample(id));

        mockMvc.perform(post("/api/passenger/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("POST /api/passenger/create 400 when invalid")
    void createPassenger_invalid() throws Exception {
        var invalid = AddPassengerRequestDTO.builder()
                .fullName("")
                .birthDate(null)
                .gender(null)
                .idPassport("")
                .build();

        mockMvc.perform(post("/api/passenger/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(passengerRestService, never()).createPassenger(any());
    }

    @Test
    @DisplayName("PUT /api/passenger/update 200 when valid")
    void updatePassenger_valid() throws Exception {
        UUID id = UUID.randomUUID();
        var req = UpdatePassengerRequestDTO.builder()
                .id(id)
                .fullName("New")
                .birthDate(LocalDate.of(1992,3,3))
                .gender(1)
                .idPassport("PX")
                .build();
        when(passengerRestService.updatePassenger(any(UpdatePassengerRequestDTO.class))).thenReturn(sample(id));

        mockMvc.perform(put("/api/passenger/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("PUT /api/passenger/update 400 when invalid payload")
    void updatePassenger_invalid() throws Exception {
        var invalid = UpdatePassengerRequestDTO.builder()
                .id(null)
                .fullName("")
                .birthDate(null)
                .gender(null)
                .idPassport("")
                .build();

        mockMvc.perform(put("/api/passenger/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(passengerRestService, never()).updatePassenger(any());
    }

    @Test
    @DisplayName("PUT /api/passenger/update 404 when not found")
    void updatePassenger_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = UpdatePassengerRequestDTO.builder()
                .id(id)
                .fullName("Nope")
                .birthDate(LocalDate.now())
                .gender(1)
                .idPassport("ZZZ")
                .build();
        when(passengerRestService.updatePassenger(any(UpdatePassengerRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/passenger/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/passenger/delete/{id} 200 when found")
    void deletePassenger_found() throws Exception {
        UUID id = UUID.randomUUID();
        when(passengerRestService.deletePassenger(id)).thenReturn(sample(id));

        mockMvc.perform(post("/api/passenger/delete/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("POST /api/passenger/delete/{id} 404 when not found")
    void deletePassenger_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(passengerRestService.deletePassenger(id)).thenReturn(null);

        mockMvc.perform(post("/api/passenger/delete/{id}", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/users returns 200 with users list")
    void getAllUsers_success() throws Exception {
        var user = new ProfileClient.ProfileUser();
        user.setId("user1");
        user.setName("John");
        user.setEmail("john@example.com");
        var wrapper = new ProfileClient.ProfileUsersWrapper();
        wrapper.setData(List.of(user));
        when(profileClient.getAllUsers()).thenReturn(wrapper);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("user1"));
    }

    @Test
    @DisplayName("GET /api/users returns 500 on exception")
    void getAllUsers_exception() throws Exception {
        when(profileClient.getAllUsers()).thenThrow(new RuntimeException("profile error"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 200 when found")
    void getUserById_found() throws Exception {
        var user = new ProfileClient.ProfileUser();
        user.setId("user1");
        user.setName("John");
        user.setEmail("john@example.com");
        var wrapper = new ProfileClient.ProfileUserWrapper();
        wrapper.setData(user);
        when(profileClient.getUserById("user1")).thenReturn(wrapper);

        mockMvc.perform(get("/api/users/{id}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("user1"));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 404 when not found")
    void getUserById_notFound() throws Exception {
        when(profileClient.getUserById("user1")).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", "user1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/users/{id} returns 200 when updated")
    void updateUserById_success() throws Exception {
        var user = new ProfileClient.ProfileUser();
        user.setId("user1");
        user.setName("John Updated");
        user.setEmail("john@example.com");
        var wrapper = new ProfileClient.ProfileUserWrapper();
        wrapper.setData(user);
        when(profileClient.updateUser(eq("user1"), any())).thenReturn(wrapper);

        mockMvc.perform(put("/api/users/{id}", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("John Updated"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} returns 400 when failed")
    void updateUserById_failed() throws Exception {
        when(profileClient.updateUser(eq("user1"), any())).thenReturn(null);

        mockMvc.perform(put("/api/users/{id}", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/users/customers returns 200 and syncs passengers")
    void getCustomersAndSync_success() throws Exception {
        var user = new ProfileClient.ProfileUser();
        user.setId("user1");
        user.setName("John");
        user.setEmail("john@example.com");
        var wrapper = new ProfileClient.ProfileUsersWrapper();
        wrapper.setData(List.of(user));
        when(profileClient.getCustomers()).thenReturn(wrapper);
        when(passengerRepository.existsByIdPassport("user1")).thenReturn(false);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/api/users/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    @DisplayName("GET /api/users/customers returns 500 on exception")
    void getCustomersAndSync_exception() throws Exception {
        when(profileClient.getCustomers()).thenThrow(new RuntimeException("profile error"));

        mockMvc.perform(get("/api/users/customers"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
