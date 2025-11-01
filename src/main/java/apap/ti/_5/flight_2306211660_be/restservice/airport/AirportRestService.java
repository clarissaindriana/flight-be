package apap.ti._5.flight_2306211660_be.restservice.airport;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.UpdateAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airport.AirportResponseDTO;

public interface AirportRestService {

    AirportResponseDTO createAirport(AddAirportRequestDTO dto);

    List<AirportResponseDTO> getAllAirports();

    List<AirportResponseDTO> searchAirportsByName(String name);

    AirportResponseDTO getAirport(String iataCode);

    AirportResponseDTO updateAirport(UpdateAirportRequestDTO dto);

    AirportResponseDTO deleteAirport(String iataCode);
}
