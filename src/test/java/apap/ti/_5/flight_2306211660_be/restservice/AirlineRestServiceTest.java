package apap.ti._5.flight_2306211660_be.restservice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.Airline;
import apap.ti._5.flight_2306211660_be.repository.AirlineRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.UpdateAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airline.AirlineRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AirlineRestServiceTest {

    @Mock
    private AirlineRepository airlineRepository;

    @InjectMocks
    private AirlineRestServiceImpl airlineRestService;

    private Airline buildAirline(String id, String name, String country) {
        return Airline.builder()
                .id(id)
                .name(name)
                .country(country)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .deletedAt(null)
                .build();
    }

    @Test
    void testCreateAirline_success() {
        // Arrange
        var dto = AddAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("ID")
                .build();

        var saved = buildAirline("GA", "Garuda Indonesia", "ID");

        when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

        // Act
        AirlineResponseDTO result = airlineRestService.createAirline(dto);

        // Assert
        assertNotNull(result);
        assertEquals("GA", result.getId());
        assertEquals("Garuda Indonesia", result.getName());
        assertEquals("ID", result.getCountry());
        verify(airlineRepository).save(any(Airline.class));
    }

    @Test
    void testGetAllAirlines_success() {
        // Arrange
        var a1 = buildAirline("GA", "Garuda Indonesia", "ID");
        var a2 = buildAirline("SQ", "Singapore Airlines", "SG");
        when(airlineRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        // Act
        List<AirlineResponseDTO> result = airlineRestService.getAllAirlines();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("GA", result.get(0).getId());
        assertEquals("SQ", result.get(1).getId());
        verify(airlineRepository).findAll();
    }

    @Test
    void testSearchAirlinesByName_nullOrEmpty_usesFindAll() {
        // Arrange
        var a1 = buildAirline("GA", "Garuda Indonesia", "ID");
        var a2 = buildAirline("SQ", "Singapore Airlines", "SG");
        when(airlineRepository.findAll()).thenReturn(Arrays.asList(a1, a2), Arrays.asList(a1, a2));

        // Act
        var resultNull = airlineRestService.searchAirlinesByName(null);
        var resultEmpty = airlineRestService.searchAirlinesByName("   ");

        // Assert
        assertEquals(2, resultNull.size());
        assertEquals(2, resultEmpty.size());
        verify(airlineRepository, times(2)).findAll();
        verify(airlineRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void testSearchAirlinesByName_withTerm_usesFindByNameContainingIgnoreCase() {
        // Arrange
        var a1 = buildAirline("GA", "Garuda Indonesia", "ID");
        when(airlineRepository.findByNameContainingIgnoreCase("gar")).thenReturn(Collections.singletonList(a1));

        // Act
        var result = airlineRestService.searchAirlinesByName("gar");

        // Assert
        assertEquals(1, result.size());
        assertEquals("GA", result.get(0).getId());
        verify(airlineRepository).findByNameContainingIgnoreCase("gar");
        verify(airlineRepository, never()).findAll();
    }

    @Test
    void testGetAirline_found() {
        // Arrange
        var a1 = buildAirline("GA", "Garuda Indonesia", "ID");
        when(airlineRepository.findById("GA")).thenReturn(Optional.of(a1));

        // Act
        var result = airlineRestService.getAirline("GA");

        // Assert
        assertNotNull(result);
        assertEquals("GA", result.getId());
        verify(airlineRepository).findById("GA");
    }

    @Test
    void testGetAirline_notFound() {
        // Arrange
        when(airlineRepository.findById("XX")).thenReturn(Optional.empty());

        // Act
        var result = airlineRestService.getAirline("XX");

        // Assert
        assertNull(result);
        verify(airlineRepository).findById("XX");
    }

    @Test
    void testUpdateAirline_found() {
        // Arrange
        var existing = buildAirline("GA", "Garuda", "ID");
        var dto = UpdateAirlineRequestDTO.builder()
                .id("GA")
                .name("Garuda Indonesia")
                .country("Indonesia")
                .build();

        var saved = buildAirline("GA", "Garuda Indonesia", "Indonesia");

        when(airlineRepository.findById("GA")).thenReturn(Optional.of(existing));
        when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

        // Act
        var result = airlineRestService.updateAirline(dto);

        // Assert
        assertNotNull(result);
        assertEquals("GA", result.getId());
        assertEquals("Garuda Indonesia", result.getName());
        assertEquals("Indonesia", result.getCountry());
        verify(airlineRepository).findById("GA");
        verify(airlineRepository).save(any(Airline.class));
    }

    @Test
    void testUpdateAirline_notFound() {
        // Arrange
        var dto = UpdateAirlineRequestDTO.builder()
                .id("XX").name("N/A").country("N/A").build();
        when(airlineRepository.findById("XX")).thenReturn(Optional.empty());

        // Act
        var result = airlineRestService.updateAirline(dto);

        // Assert
        assertNull(result);
        verify(airlineRepository).findById("XX");
        verify(airlineRepository, never()).save(any(Airline.class));
    }

    @Test
    void testDeleteAirline_found_softDelete() {
        // Arrange
        var existing = buildAirline("GA", "Garuda", "ID");
        when(airlineRepository.findById("GA")).thenReturn(Optional.of(existing));
        when(airlineRepository.save(any(Airline.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = airlineRestService.deleteAirline("GA");

        // Assert
        assertNotNull(result);
        assertEquals("GA", result.getId());
        assertNotNull(result.getDeletedAt());

        ArgumentCaptor<Airline> captor = ArgumentCaptor.forClass(Airline.class);
        verify(airlineRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDeletedAt(), "deletedAt must be set for soft delete");
    }

    @Test
    void testDeleteAirline_notFound() {
        // Arrange
        when(airlineRepository.findById("XX")).thenReturn(Optional.empty());

        // Act
        var result = airlineRestService.deleteAirline("XX");

        // Assert
        assertNull(result);
        verify(airlineRepository).findById("XX");
        verify(airlineRepository, never()).save(any(Airline.class));
    }

    @Test
    void testGetTotalAirlines() {
        // Arrange
        when(airlineRepository.count()).thenReturn(10L);

        // Act
        long result = airlineRestService.getTotalAirlines();

        // Assert
        assertEquals(10L, result);
        verify(airlineRepository).count();
    }
}
