package apap.ti._5.flight_2306211660_be.restservice.airport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.Airport;
import apap.ti._5.flight_2306211660_be.repository.AirportRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.UpdateAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airport.AirportResponseDTO;

@Service
public class AirportRestServiceImpl implements AirportRestService {

    @Autowired
    private AirportRepository airportRepository;

    @Override
    public AirportResponseDTO createAirport(AddAirportRequestDTO dto) {
        Airport airport = Airport.builder()
                .iataCode(dto.getIataCode())
                .name(dto.getName())
                .city(dto.getCity())
                .country(dto.getCountry())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .timezone(dto.getTimezone())
                .build();

        return convertToAirportResponseDTO(airportRepository.save(airport));
    }

    @Override
    public List<AirportResponseDTO> getAllAirports() {
        List<Airport> allAirports = airportRepository.findAll();

        return allAirports.stream()
                .map(airport -> convertToAirportResponseDTO(airport))
                .collect(Collectors.toList());
    }

    @Override
    public List<AirportResponseDTO> searchAirportsByName(String name) {
        List<Airport> airports;

        // If search term is empty or null, return all airports
        if (name == null || name.trim().isEmpty()) {
            airports = airportRepository.findAll();
        } else {
            // Search by name containing (case-insensitive)
            airports = airportRepository.findByNameContainingIgnoreCase(name.trim());
        }

        // Convert to DTOs
        return airports.stream()
                .map(airport -> convertToAirportResponseDTO(airport))
                .collect(Collectors.toList());
    }

    @Override
    public AirportResponseDTO getAirport(String iataCode) {
        Airport airport = airportRepository.findById(iataCode).orElse(null);
        if (airport == null) {
            return null;
        }
        return convertToAirportResponseDTO(airport);
    }

    @Override
    public AirportResponseDTO updateAirport(UpdateAirportRequestDTO dto) {
        Airport airport = airportRepository.findById(dto.getIataCode()).orElse(null);

        if (airport == null) return null;

        airport = airport.toBuilder()
                .iataCode(dto.getIataCode())
                .name(dto.getName())
                .city(dto.getCity())
                .country(dto.getCountry())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .timezone(dto.getTimezone())
                .build();

        return convertToAirportResponseDTO(airportRepository.save(airport));
    }

    @Override
    public AirportResponseDTO deleteAirport(String iataCode) {
        Airport airport = airportRepository.findById(iataCode).orElse(null);
        if (airport == null) {
            return null;
        }

        // Soft delete
        airport.setUpdatedAt(LocalDateTime.now());
        return convertToAirportResponseDTO(airportRepository.save(airport));
    }

    private AirportResponseDTO convertToAirportResponseDTO(Airport airport) {
        return AirportResponseDTO.builder()
                .iataCode(airport.getIataCode())
                .name(airport.getName())
                .city(airport.getCity())
                .country(airport.getCountry())
                .latitude(airport.getLatitude())
                .longitude(airport.getLongitude())
                .timezone(airport.getTimezone())
                .createdAt(airport.getCreatedAt())
                .updatedAt(airport.getUpdatedAt())
                .build();
    }
}
