package apap.ti._5.flight_2306211660_be.restservice.airline;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.UpdateAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;

public interface AirlineRestService {
    AirlineResponseDTO createAirline(AddAirlineRequestDTO dto);
    List<AirlineResponseDTO> getAllAirlines();
    List<AirlineResponseDTO> searchAirlinesByName(String name);
    AirlineResponseDTO getAirline(String id);
    AirlineResponseDTO updateAirline(UpdateAirlineRequestDTO dto);
    AirlineResponseDTO deleteAirline(String id);
}
