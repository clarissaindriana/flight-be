package apap.ti._5.flight_2306211660_be;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restservice.airline.AirlineRestService;
import apap.ti._5.flight_2306211660_be.restservice.airport.AirportRestService;
import apap.ti._5.flight_2306211660_be.restservice.passenger.PassengerRestService;

@SpringBootApplication
public class Flight2306211660BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Flight2306211660BeApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDummyAirlinesAndAirportsAndPassengers(AirlineRestService airlineRestService, AirportRestService airportRestService, PassengerRestService passengerRestService) {
		return args -> {
			System.out.println("Generating dummy airlines from IATA dataset...");

			try {
				// Fetch data from the IATA airlines URL
				URL url = new URL("https://lsv.ens-paris-saclay.fr/~sirangel/teaching/dataset/airlines.txt");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					List<String> lines = reader.lines().collect(Collectors.toList());

					int createdCount = 0;
					int failedCount = 0;

					for (String line : lines) {
						// Skip empty lines
						if (line.trim().isEmpty()) continue;

						// Parse the line: "Name;IATA;ICAO;Callsign;Country"
						String[] parts = line.split(";");
						if (parts.length >= 5) {
							String name = parts[0].trim();
							String iataCode = parts[1].trim();
							String country = parts[4].trim();

							// Skip entries with invalid IATA codes (empty or \\N)
							if (iataCode.isEmpty() || iataCode.equals("\\N") || iataCode.length() > 3) {
								continue;
							}

							// Skip entries with empty names or countries
							if (name.isEmpty() || country.isEmpty() || country.equals("\\N")) {
								continue;
							}

							AddAirlineRequestDTO dto = AddAirlineRequestDTO.builder()
									.id(iataCode)
									.name(name)
									.country(country)
									.build();

							try {
								airlineRestService.createAirline(dto);
								createdCount++;
							} catch (Exception e) {
								failedCount++;
								// Continue with next airline instead of stopping
							}
						}
					}

					System.out.println("Dummy airlines generation complete. Created: " + createdCount + ", Failed: " + failedCount);
				}

				connection.disconnect();

			} catch (Exception e) {
				System.err.println("Failed to fetch airlines data: " + e.getMessage());
				System.out.println("Falling back to manual dummy data...");

				// Fallback to manual data if URL fetch fails
				List<String[]> airlinesData = List.of(
					new String[]{"Garuda Indonesia", "GA", "Indonesia"},
					new String[]{"Singapore Airlines", "SQ", "Singapore"},
					new String[]{"Air France", "AF", "France"},
					new String[]{"British Airways", "BA", "United Kingdom"},
					new String[]{"Lufthansa", "LH", "Germany"}
				);

				for (String[] airlineData : airlinesData) {
					AddAirlineRequestDTO dto = AddAirlineRequestDTO.builder()
							.id(airlineData[1])
							.name(airlineData[0])
							.country(airlineData[2])
							.build();

					try {
						airlineRestService.createAirline(dto);
					} catch (Exception ex) {
						System.out.println("Failed to create airline " + airlineData[0] + ": " + ex.getMessage());
					}
				}
				System.out.println("Fallback dummy airlines generation complete.");
			}

			// Fetch airports data
			System.out.println("Fetching airports data from FlightStats API...");

			try {
				URL airportUrl = new URL("https://api.flightstats.com/flex/airports/docs/v1/lts/samples/Airports_response.json");
				HttpURLConnection airportConnection = (HttpURLConnection) airportUrl.openConnection();
				airportConnection.setRequestMethod("GET");

				try (BufferedReader airportReader = new BufferedReader(new InputStreamReader(airportConnection.getInputStream()))) {
					StringBuilder jsonResponse = new StringBuilder();
					String line;
					while ((line = airportReader.readLine()) != null) {
						jsonResponse.append(line);
					}

					com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
					com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonResponse.toString());

					if (rootNode.has("airports")) {
						com.fasterxml.jackson.databind.JsonNode airportsNode = rootNode.get("airports");
						System.out.println("Found " + airportsNode.size() + " airports in the API response.");

						// Process airports data
						for (com.fasterxml.jackson.databind.JsonNode airportNode : airportsNode) {
							try {
								String iataCode = airportNode.get("iata").asText();
								String name = airportNode.get("name").asText();
								String city = airportNode.get("city").asText();
								String country = airportNode.get("countryName").asText();
								Double latitude = null;
								Double longitude = null;
								String timezone = null;

								try {
									latitude = airportNode.get("latitude").asDouble();
								} catch (Exception e) {
									// latitude might be null or invalid
								}

								try {
									longitude = airportNode.get("longitude").asDouble();
								} catch (Exception e) {
									// longitude might be null or invalid
								}

								try {
									timezone = airportNode.get("timezoneRegionName").asText();
								} catch (Exception e) {
									// timezone might be null or invalid
								}

								AddAirportRequestDTO airportDto = AddAirportRequestDTO.builder()
										.iataCode(iataCode)
										.name(name)
										.city(city)
										.country(country)
										.latitude(latitude)
										.longitude(longitude)
										.timezone(timezone)
										.build();

								airportRestService.createAirport(airportDto);
							} catch (Exception e) {
								// Continue with next airport if one fails
							}
						}
					} else {
						System.out.println("No airports data found in the API response.");
					}
				}

				airportConnection.disconnect();
				System.out.println("Airports data fetch complete.");

			} catch (Exception e) {
				System.err.println("Failed to fetch airports data: " + e.getMessage());
			}

			// Passenger data will be synchronized from the profile microservice via /api/users/customers
		};
	}
}
