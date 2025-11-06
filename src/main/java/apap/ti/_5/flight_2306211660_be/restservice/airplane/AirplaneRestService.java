package apap.ti._5.flight_2306211660_be.restservice.airplane;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.airplane.AddAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.UpdateAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airplane.AirplaneResponseDTO;

public interface AirplaneRestService {
    AirplaneResponseDTO createAirplane(AddAirplaneRequestDTO dto);
    List<AirplaneResponseDTO> getAllAirplanes();
    List<AirplaneResponseDTO> searchAirplanesByModel(String model);
    AirplaneResponseDTO getAirplane(String id);
    AirplaneResponseDTO updateAirplane(UpdateAirplaneRequestDTO dto);
    AirplaneResponseDTO deleteAirplane(String id);
    AirplaneResponseDTO activateAirplane(String id);
}
