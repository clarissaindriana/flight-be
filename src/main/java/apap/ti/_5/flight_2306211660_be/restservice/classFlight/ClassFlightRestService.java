package apap.ti._5.flight_2306211660_be.restservice.classFlight;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;

public interface ClassFlightRestService {

    ClassFlightResponseDTO createClassFlight(AddClassFlightRequestDTO dto);

    List<ClassFlightResponseDTO> getAllClassFlights();

    List<ClassFlightResponseDTO> getClassFlightsByFlight(String flightId);

    ClassFlightResponseDTO getClassFlight(Integer id);

    ClassFlightResponseDTO updateClassFlight(UpdateClassFlightRequestDTO dto);

    ClassFlightResponseDTO deleteClassFlight(Integer id);
}
