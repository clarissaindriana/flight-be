package apap.ti._5.flight_2306211660_be.restservice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.Airplane;
import apap.ti._5.flight_2306211660_be.model.Flight;
import apap.ti._5.flight_2306211660_be.repository.AirplaneRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.AddAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.UpdateAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airplane.AirplaneResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airplane.AirplaneRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AirplaneRestServiceTest {

    @Mock
    private AirplaneRepository airplaneRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private AirplaneRestServiceImpl airplaneRestService;

    private Airplane buildAirplane(String id, String airlineId, String model, int seats, int year, boolean isDeleted) {
        return Airplane.builder()
                .id(id)
                .airlineId(airlineId)
                .model(model)
                .seatCapacity(seats)
                .manufactureYear(year)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .isDeleted(isDeleted)
                .build();
    }

    @Test
    void createAirplane_success() {
        var dto = AddAirplaneRequestDTO.builder()
                .airlineId("GA")
                .model("B737-800")
                .seatCapacity(180)
                .manufactureYear(2018)
                .build();

        when(airplaneRepository.existsById(anyString())).thenReturn(false);
        when(airplaneRepository.save(any(Airplane.class))).thenAnswer(inv -> {
            Airplane a = inv.getArgument(0);
            if (a.getIsDeleted() == null) {
                a.setIsDeleted(false);
            }
            return a;
        });
        when(airplaneRepository.findById(anyString())).thenReturn(Optional.empty()); // keep savedAirplane as-is

        AirplaneResponseDTO res = airplaneRestService.createAirplane(dto);

        assertNotNull(res);
        assertTrue(res.getId().startsWith("GA-"), "Registration must start with airlineId-");
        assertEquals("B737-800", res.getModel());
        assertEquals(180, res.getSeatCapacity());
        assertEquals(Integer.valueOf(2018), res.getManufactureYear());
        verify(airplaneRepository).save(any(Airplane.class));
    }

    @Test
    void createAirplane_futureYear_throws() {
        var dto = AddAirplaneRequestDTO.builder()
                .airlineId("GA")
                .model("B737")
                .seatCapacity(160)
                .manufactureYear(LocalDateTime.now().getYear() + 1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> airplaneRestService.createAirplane(dto));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void getAllAirplanes_success() {
        var a1 = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        var a2 = buildAirplane("SQ-XYZ", "SQ", "A320", 150, 2019, false);
        when(airplaneRepository.findAll()).thenReturn(List.of(a1, a2));

        var list = airplaneRestService.getAllAirplanes();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("GA-ABC", list.get(0).getId());
        verify(airplaneRepository).findAll();
    }

    @Test
    void searchAirplanesByModel_nullOrEmpty_usesFindAll() {
        var a1 = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        var a2 = buildAirplane("SQ-XYZ", "SQ", "A320", 150, 2019, false);
        when(airplaneRepository.findAll()).thenReturn(List.of(a1, a2), List.of(a1, a2));

        var res1 = airplaneRestService.searchAirplanesByModel(null);
        var res2 = airplaneRestService.searchAirplanesByModel("   ");

        assertEquals(2, res1.size());
        assertEquals(2, res2.size());
        verify(airplaneRepository, times(2)).findAll();
        verify(airplaneRepository, never()).findByModelContainingIgnoreCase(anyString());
    }

    @Test
    void searchAirplanesByModel_withTerm_usesQuery() {
        var a1 = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        when(airplaneRepository.findByModelContainingIgnoreCase("b73")).thenReturn(List.of(a1));

        var res = airplaneRestService.searchAirplanesByModel("b73");

        assertEquals(1, res.size());
        assertEquals("GA-ABC", res.get(0).getId());
        verify(airplaneRepository).findByModelContainingIgnoreCase("b73");
        verify(airplaneRepository, never()).findAll();
    }

    @Test
    void getAirplane_found() {
        var a1 = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(a1));

        var res = airplaneRestService.getAirplane("GA-ABC");

        assertNotNull(res);
        assertEquals("GA-ABC", res.getId());
        verify(airplaneRepository).findById("GA-ABC");
    }

    @Test
    void getAirplane_notFound() {
        when(airplaneRepository.findById("X")).thenReturn(Optional.empty());

        var res = airplaneRestService.getAirplane("X");

        assertNull(res);
        verify(airplaneRepository).findById("X");
    }

    @Test
    void updateAirplane_success() {
        var existing = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        var dto = UpdateAirplaneRequestDTO.builder()
                .id("GA-ABC")
                .model("B737-800")
                .seatCapacity(186)
                .manufactureYear(2018)
                .build();

        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(existing));
        when(airplaneRepository.save(any(Airplane.class))).thenAnswer(inv -> {
            Airplane a = inv.getArgument(0);
            if (a.getIsDeleted() == null) {
                a.setIsDeleted(false);
            }
            return a;
        });

        var res = airplaneRestService.updateAirplane(dto);

        assertNotNull(res);
        assertEquals("GA-ABC", res.getId());
        assertEquals("B737-800", res.getModel());
        assertEquals(186, res.getSeatCapacity());
        assertEquals(Integer.valueOf(2018), res.getManufactureYear());
        verify(airplaneRepository).save(any(Airplane.class));
    }

    @Test
    void updateAirplane_notFound_returnsNull() {
        var dto = UpdateAirplaneRequestDTO.builder()
                .id("NA")
                .model("B777")
                .seatCapacity(300)
                .manufactureYear(2015)
                .build();
        when(airplaneRepository.findById("NA")).thenReturn(Optional.empty());

        var res = airplaneRestService.updateAirplane(dto);

        assertNull(res);
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void updateAirplane_deleted_throws() {
        var existing = buildAirplane("GA-DEL", "GA", "B737", 180, 2016, true);
        var dto = UpdateAirplaneRequestDTO.builder()
                .id("GA-DEL")
                .model("B737-800")
                .seatCapacity(186)
                .manufactureYear(2018)
                .build();
        when(airplaneRepository.findById("GA-DEL")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> airplaneRestService.updateAirplane(dto));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void updateAirplane_futureYear_throws() {
        var existing = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        var dto = UpdateAirplaneRequestDTO.builder()
                .id("GA-ABC")
                .model("B737-800")
                .seatCapacity(186)
                .manufactureYear(LocalDateTime.now().getYear() + 5)
                .build();
        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> airplaneRestService.updateAirplane(dto));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void deleteAirplane_notFound_returnsNull() {
        when(airplaneRepository.findById("X")).thenReturn(Optional.empty());

        var res = airplaneRestService.deleteAirplane("X");

        assertNull(res);
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void deleteAirplane_alreadyDeleted_throws() {
        var existing = buildAirplane("GA-DEL", "GA", "B737", 180, 2016, true);
        when(airplaneRepository.findById("GA-DEL")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> airplaneRestService.deleteAirplane("GA-DEL"));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void deleteAirplane_conflictingFlights_throws() {
        var existing = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(existing));
        when(flightRepository.findByAirplaneIdAndIsDeletedAndStatusIn(eq("GA-ABC"), eq(false), anyList()))
                .thenReturn(List.of(Flight.builder().id("GA-ABC-1").airplaneId("GA-ABC").isDeleted(false).status(1).build()));

        assertThrows(IllegalStateException.class, () -> airplaneRestService.deleteAirplane("GA-ABC"));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void deleteAirplane_success_softDelete() {
        var existing = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(existing));
        when(flightRepository.findByAirplaneIdAndIsDeletedAndStatusIn(eq("GA-ABC"), eq(false), anyList()))
                .thenReturn(Collections.emptyList());
        when(airplaneRepository.save(any(Airplane.class))).thenAnswer(inv -> {
            Airplane a = inv.getArgument(0);
            if (a.getIsDeleted() == null) {
                a.setIsDeleted(false);
            }
            return a;
        });

        var res = airplaneRestService.deleteAirplane("GA-ABC");

        assertNotNull(res);
        assertEquals("GA-ABC", res.getId());
        assertTrue(res.isDeleted(), "Airplane should be marked deleted");
        verify(airplaneRepository).save(any(Airplane.class));
    }

    @Test
    void activateAirplane_notFound_returnsNull() {
        when(airplaneRepository.findById("NA")).thenReturn(Optional.empty());

        var res = airplaneRestService.activateAirplane("NA");

        assertNull(res);
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void activateAirplane_alreadyActive_throws() {
        var existing = buildAirplane("GA-ABC", "GA", "B737", 180, 2016, false);
        when(airplaneRepository.findById("GA-ABC")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> airplaneRestService.activateAirplane("GA-ABC"));
        verify(airplaneRepository, never()).save(any());
    }

    @Test
    void activateAirplane_success() {
        var existing = buildAirplane("GA-DEL", "GA", "B737", 180, 2016, true);
        when(airplaneRepository.findById("GA-DEL")).thenReturn(Optional.of(existing));
        when(airplaneRepository.save(any(Airplane.class))).thenAnswer(inv -> {
            Airplane a = inv.getArgument(0);
            if (a.getIsDeleted() == null) {
                a.setIsDeleted(false);
            }
            return a;
        });

        var res = airplaneRestService.activateAirplane("GA-DEL");

        assertNotNull(res);
        assertEquals("GA-DEL", res.getId());
        assertFalse(res.isDeleted(), "Airplane should be active (not deleted)");
        verify(airplaneRepository).save(any(Airplane.class));
    }
}
