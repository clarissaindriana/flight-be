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
import apap.ti._5.flight_2306211660_be.restservice.airline.AirlineRestService;

@SpringBootApplication
public class Flight2306211660BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Flight2306211660BeApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDummyAirlines(AirlineRestService airlineRestService) {
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
		};
	}
}
