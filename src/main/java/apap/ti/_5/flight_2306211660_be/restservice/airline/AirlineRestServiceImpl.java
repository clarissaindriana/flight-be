package apap.ti._5.flight_2306211660_be.restservice.airline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.Airline;
import apap.ti._5.flight_2306211660_be.repository.AirlineRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.UpdateAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;

@Service
public class AirlineRestServiceImpl implements AirlineRestService {

    @Autowired
    private AirlineRepository airlineRepository;

    @Override
    public AirlineResponseDTO createAirline(AddAirlineRequestDTO dto) {
        Airline airline = Airline.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .build();

        return convertToAirlineResponseDTO(airlineRepository.save(airline));
    }

    @Override
    public List<AirlineResponseDTO> getAllAirlines() {
        List<Airline> allAirlines = airlineRepository.findAll();

        return allAirlines.stream()
                .map(airline -> convertToAirlineResponseDTO(airline))
                .collect(Collectors.toList());
    }

    @Override
    public List<AirlineResponseDTO> searchAirlinesByName(String name) {
        List<Airline> airlines;

        // If search term is empty or null, return all airlines
        if (name == null || name.trim().isEmpty()) {
            airlines = airlineRepository.findAll();
        } else {
            // Search by name containing (case-insensitive)
            airlines = airlineRepository.findByNameContainingIgnoreCase(name.trim());
        }

        // Convert to DTOs
        return airlines.stream()
                .map(airline -> convertToAirlineResponseDTO(airline))
                .collect(Collectors.toList());
    }

    @Override
    public AirlineResponseDTO getAirline(String id) {
        Airline airline = airlineRepository.findById(id).orElse(null);
        if (airline == null) {
            return null;
        }
        return convertToAirlineResponseDTO(airline);
    }

    @Override
    public AirlineResponseDTO updateAirline(UpdateAirlineRequestDTO dto) {
        Airline airline = airlineRepository.findById(dto.getId()).orElse(null);

        if (airline == null) return null;

        airline = airline.toBuilder()
                .id(dto.getId())
                .name(dto.getName())
                .country(dto.getCountry())
                .build();

        return convertToAirlineResponseDTO(airlineRepository.save(airline));
    }

    @Override
    public AirlineResponseDTO deleteAirline(String id) {
        Airline airline = airlineRepository.findById(id).orElse(null);
        if (airline == null) {
            return null;
        }

        // Soft delete
        airline.setDeletedAt(LocalDateTime.now());
        return convertToAirlineResponseDTO(airlineRepository.save(airline));
    }

    @Override
    public long getTotalAirlines() {
        // airlineRepository is configured with @Where to exclude soft-deleted entries,
        // so count() returns active airlines only.
        return airlineRepository.count();
    }

    private AirlineResponseDTO convertToAirlineResponseDTO(Airline airline) {
        return AirlineResponseDTO.builder()
                .id(airline.getId())
                .name(airline.getName())
                .country(airline.getCountry())
                .createdAt(airline.getCreatedAt())
                .updatedAt(airline.getUpdatedAt())
                .deletedAt(airline.getDeletedAt())
                .build();
    }
}
