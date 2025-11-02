package apap.ti._5.flight_2306211660_be.restservice.passenger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;

@Service
public class PassengerRestServiceImpl implements PassengerRestService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Override
    public PassengerResponseDTO createPassenger(AddPassengerRequestDTO dto) {
        Passenger passenger = Passenger.builder()
                .id(UUID.randomUUID())
                .fullName(dto.getFullName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .idPassport(dto.getIdPassport())
                .build();

        passenger = passengerRepository.save(passenger);
        return convertToPassengerResponseDTO(passenger);
    }

    @Override
    public List<PassengerResponseDTO> getAllPassengers() {
        List<Passenger> passengers = passengerRepository.findAll();
        return passengers.stream()
                .map(this::convertToPassengerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PassengerResponseDTO getPassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id).orElse(null);
        if (passenger == null) {
            return null;
        }
        return convertToPassengerResponseDTO(passenger);
    }

    @Override
    public PassengerResponseDTO updatePassenger(UpdatePassengerRequestDTO dto) {
        Passenger passenger = passengerRepository.findById(dto.getId()).orElse(null);
        if (passenger == null) {
            return null;
        }

        passenger = passenger.toBuilder()
                .fullName(dto.getFullName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .idPassport(dto.getIdPassport())
                .build();

        passenger = passengerRepository.save(passenger);
        return convertToPassengerResponseDTO(passenger);
    }

    @Override
    public PassengerResponseDTO deletePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id).orElse(null);
        if (passenger == null) {
            return null;
        }

        passengerRepository.delete(passenger);
        return convertToPassengerResponseDTO(passenger);
    }

    private PassengerResponseDTO convertToPassengerResponseDTO(Passenger passenger) {
        return PassengerResponseDTO.builder()
                .id(passenger.getId())
                .fullName(passenger.getFullName())
                .birthDate(passenger.getBirthDate())
                .gender(passenger.getGender())
                .idPassport(passenger.getIdPassport())
                .createdAt(passenger.getCreatedAt())
                .updatedAt(passenger.getUpdatedAt())
                .build();
    }
}
