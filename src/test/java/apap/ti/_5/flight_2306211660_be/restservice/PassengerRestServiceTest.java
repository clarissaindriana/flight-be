package apap.ti._5.flight_2306211660_be.restservice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.passenger.PassengerRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PassengerRestServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerRestServiceImpl passengerRestService;

    private Passenger buildPassenger(UUID id,
                                     String fullName,
                                     LocalDate birthDate,
                                     Integer gender,
                                     String idPassport,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        return Passenger.builder()
                .id(id)
                .fullName(fullName)
                .birthDate(birthDate)
                .gender(gender)
                .idPassport(idPassport)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    @Test
    void createPassenger_success() {
        var req = AddPassengerRequestDTO.builder()
                .fullName("John Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(1)
                .idPassport("P1234567")
                .build();

        UUID genId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now().minusDays(1);

        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> {
            Passenger p = inv.getArgument(0);
            return p.toBuilder()
                    .id(genId)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        });

        PassengerResponseDTO res = passengerRestService.createPassenger(req);

        assertNotNull(res);
        assertEquals(genId, res.getId());
        assertEquals("John Doe", res.getFullName());
        assertEquals(LocalDate.of(1990, 1, 1), res.getBirthDate());
        assertEquals(1, res.getGender());
        assertEquals("P1234567", res.getIdPassport());

        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengerRepository).save(captor.capture());
        assertEquals("John Doe", captor.getValue().getFullName());
    }

    @Test
    void getAllPassengers_success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        var p1 = buildPassenger(id1, "Alice", LocalDate.of(1995, 5, 10), 2, "A123", LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4));
        var p2 = buildPassenger(id2, "Bob", LocalDate.of(1988, 7, 20), 1, "B456", LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(passengerRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<PassengerResponseDTO> list = passengerRestService.getAllPassengers();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(id1, list.get(0).getId());
        assertEquals(id2, list.get(1).getId());
        verify(passengerRepository).findAll();
    }

    @Test
    void getPassenger_found() {
        UUID id = UUID.randomUUID();
        var p = buildPassenger(id, "Charlie", LocalDate.of(2000, 2, 2), 1, "C789", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        when(passengerRepository.findById(id)).thenReturn(Optional.of(p));

        PassengerResponseDTO res = passengerRestService.getPassenger(id);

        assertNotNull(res);
        assertEquals(id, res.getId());
        assertEquals("Charlie", res.getFullName());
        verify(passengerRepository).findById(id);
    }

    @Test
    void getPassenger_notFound_returnsNull() {
        UUID id = UUID.randomUUID();
        when(passengerRepository.findById(id)).thenReturn(Optional.empty());

        PassengerResponseDTO res = passengerRestService.getPassenger(id);

        assertNull(res);
        verify(passengerRepository).findById(id);
    }

    @Test
    void updatePassenger_found_success() {
        UUID id = UUID.randomUUID();
        var existing = buildPassenger(
                id, "Old Name", LocalDate.of(1991, 1, 1), 1, "OLDP",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5)
        );

        var dto = UpdatePassengerRequestDTO.builder()
                .id(id)
                .fullName("New Name")
                .birthDate(LocalDate.of(1992, 2, 2))
                .gender(2)
                .idPassport("NEWP")
                .build();

        when(passengerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        PassengerResponseDTO res = passengerRestService.updatePassenger(dto);

        assertNotNull(res);
        assertEquals(id, res.getId());
        assertEquals("New Name", res.getFullName());
        assertEquals(LocalDate.of(1992, 2, 2), res.getBirthDate());
        assertEquals(2, res.getGender());
        assertEquals("NEWP", res.getIdPassport());

        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengerRepository).save(captor.capture());
        assertEquals("New Name", captor.getValue().getFullName());
    }

    @Test
    void updatePassenger_notFound_returnsNull() {
        UUID id = UUID.randomUUID();
        var dto = UpdatePassengerRequestDTO.builder()
                .id(id)
                .fullName("X")
                .birthDate(LocalDate.now())
                .gender(1)
                .idPassport("PX")
                .build();

        when(passengerRepository.findById(id)).thenReturn(Optional.empty());

        PassengerResponseDTO res = passengerRestService.updatePassenger(dto);

        assertNull(res);
        verify(passengerRepository, never()).save(any());
    }

    @Test
    void deletePassenger_found_success() {
        UUID id = UUID.randomUUID();
        var p = buildPassenger(id, "Del Name", LocalDate.of(1999, 9, 9), 1, "DEL", LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));

        when(passengerRepository.findById(id)).thenReturn(Optional.of(p));
        doNothing().when(passengerRepository).delete(p);

        PassengerResponseDTO res = passengerRestService.deletePassenger(id);

        assertNotNull(res);
        assertEquals(id, res.getId());
        verify(passengerRepository).delete(p);
    }

    @Test
    void deletePassenger_notFound_returnsNull() {
        UUID id = UUID.randomUUID();
        when(passengerRepository.findById(id)).thenReturn(Optional.empty());

        PassengerResponseDTO res = passengerRestService.deletePassenger(id);

        assertNull(res);
        verify(passengerRepository, never()).delete(any());
    }
}
