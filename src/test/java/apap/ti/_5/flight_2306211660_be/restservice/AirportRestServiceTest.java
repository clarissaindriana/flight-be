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
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.Airport;
import apap.ti._5.flight_2306211660_be.repository.AirportRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.UpdateAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airport.AirportResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.airport.AirportRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AirportRestServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportRestServiceImpl airportRestService;

    private Airport buildAirport(String code, String name, String city, String country,
                                 Double lat, Double lng, String tz,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Airport.builder()
                .iataCode(code)
                .name(name)
                .city(city)
                .country(country)
                .latitude(lat)
                .longitude(lng)
                .timezone(tz)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    @Test
    void createAirport_success() {
        var dto = AddAirportRequestDTO.builder()
                .iataCode("CGK")
                .name("Soekarno Hatta International Airport")
                .city("Jakarta")
                .country("Indonesia")
                .latitude(-6.125556)
                .longitude(106.655833)
                .timezone("Asia/Jakarta")
                .build();

        LocalDateTime now = LocalDateTime.now().minusDays(1);
        var saved = buildAirport("CGK", "Soekarno Hatta International Airport", "Jakarta", "Indonesia",
                -6.125556, 106.655833, "Asia/Jakarta",
                now, now);

        when(airportRepository.save(any(Airport.class))).thenReturn(saved);

        AirportResponseDTO res = airportRestService.createAirport(dto);

        assertNotNull(res);
        assertEquals("CGK", res.getIataCode());
        assertEquals("Soekarno Hatta International Airport", res.getName());
        assertEquals("Jakarta", res.getCity());
        assertEquals("Indonesia", res.getCountry());
        assertEquals(-6.125556, res.getLatitude());
        assertEquals(106.655833, res.getLongitude());
        assertEquals("Asia/Jakarta", res.getTimezone());
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void getAllAirports_success() {
        var a1 = buildAirport("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        var a2 = buildAirport("SIN", "Changi", "Singapore", "Singapore", 1.36, 103.99, "Asia/Singapore",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(airportRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<AirportResponseDTO> list = airportRestService.getAllAirports();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("CGK", list.get(0).getIataCode());
        assertEquals("SIN", list.get(1).getIataCode());
        verify(airportRepository).findAll();
    }

    @Test
    void searchAirportsByName_nullOrEmpty_usesFindAll() {
        var a1 = buildAirport("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        var a2 = buildAirport("SIN", "Changi", "Singapore", "Singapore", 1.36, 103.99, "Asia/Singapore",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(airportRepository.findAll()).thenReturn(Arrays.asList(a1, a2), Arrays.asList(a1, a2));

        var resNull = airportRestService.searchAirportsByName(null);
        var resEmpty = airportRestService.searchAirportsByName("   ");

        assertEquals(2, resNull.size());
        assertEquals(2, resEmpty.size());
        verify(airportRepository, times(2)).findAll();
        verify(airportRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchAirportsByName_withTerm_usesQuery() {
        var a1 = buildAirport("CGK", "Soekarno Hatta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(airportRepository.findByNameContainingIgnoreCase("soekarno"))
                .thenReturn(Collections.singletonList(a1));

        var result = airportRestService.searchAirportsByName("soekarno");

        assertEquals(1, result.size());
        assertEquals("CGK", result.get(0).getIataCode());
        verify(airportRepository).findByNameContainingIgnoreCase("soekarno");
        verify(airportRepository, never()).findAll();
    }

    @Test
    void getAirport_found() {
        var a1 = buildAirport("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        when(airportRepository.findById("CGK")).thenReturn(Optional.of(a1));

        var res = airportRestService.getAirport("CGK");

        assertNotNull(res);
        assertEquals("CGK", res.getIataCode());
        verify(airportRepository).findById("CGK");
    }

    @Test
    void getAirport_notFound() {
        when(airportRepository.findById("XXX")).thenReturn(Optional.empty());

        var res = airportRestService.getAirport("XXX");

        assertNull(res);
        verify(airportRepository).findById("XXX");
    }

    @Test
    void updateAirport_found() {
        var existing = buildAirport("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        var dto = UpdateAirportRequestDTO.builder()
                .iataCode("CGK")
                .name("Soekarno Hatta International Airport")
                .city("Tangerang")
                .country("Indonesia")
                .latitude(-6.125556)
                .longitude(106.655833)
                .timezone("Asia/Jakarta")
                .build();

        when(airportRepository.findById("CGK")).thenReturn(Optional.of(existing));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = airportRestService.updateAirport(dto);

        assertNotNull(res);
        assertEquals("CGK", res.getIataCode());
        assertEquals("Soekarno Hatta International Airport", res.getName());
        assertEquals("Tangerang", res.getCity());
        assertEquals(-6.125556, res.getLatitude());
        assertEquals(106.655833, res.getLongitude());
        assertEquals("Asia/Jakarta", res.getTimezone());
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void updateAirport_notFound_returnsNull() {
        var dto = UpdateAirportRequestDTO.builder()
                .iataCode("XXX").name("N/A").city("N/A").country("N/A").build();

        when(airportRepository.findById("XXX")).thenReturn(Optional.empty());

        var res = airportRestService.updateAirport(dto);

        assertNull(res);
        verify(airportRepository, never()).save(any());
    }

    @Test
    void deleteAirport_found_setsUpdatedAt() {
        var existing = buildAirport("CGK", "Soetta", "Jakarta", "Indonesia", -6.12, 106.65, "Asia/Jakarta",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(airportRepository.findById("CGK")).thenReturn(Optional.of(existing));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = airportRestService.deleteAirport("CGK");

        assertNotNull(res);
        assertEquals("CGK", res.getIataCode());

        ArgumentCaptor<Airport> captor = ArgumentCaptor.forClass(Airport.class);
        verify(airportRepository).save(captor.capture());
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt must be set on delete");
    }

    @Test
    void deleteAirport_notFound_returnsNull() {
        when(airportRepository.findById("XXX")).thenReturn(Optional.empty());

        var res = airportRestService.deleteAirport("XXX");

        assertNull(res);
        verify(airportRepository, never()).save(any());
    }
}
