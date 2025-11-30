package apap.ti._5.flight_2306211660_be.config.security;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;
import reactor.core.publisher.Mono;

@Component
public class BookingClient {

    private static final Logger logger = LoggerFactory.getLogger(BookingClient.class);

    // Service URLs from configuration
    private final String flightServiceUrl;
    private final String insuranceServiceUrl;
    private final String tourpackageServiceUrl;
    private final String accommodationServiceUrl;
    private final String vehiclerentalServiceUrl;

    public BookingClient(
            @Value("${service.callback.base-url.flight:http://2306211660-be.hafizmuh.site}") String flightServiceUrl,
            @Value("${service.callback.base-url.insurance:http://2306240061-be.hafizmuh.site}") String insuranceServiceUrl,
            @Value("${service.callback.base-url.tourpackage:http://2306219575-be.hafizmuh.site}") String tourpackageServiceUrl,
            @Value("${service.callback.base-url.accommodation:http://2306212083-be.hafizmuh.site}") String accommodationServiceUrl,
            @Value("${service.callback.base-url.vehiclerental:http://2306203236-be.hafizmuh.site}") String vehiclerentalServiceUrl) {
        this.flightServiceUrl = flightServiceUrl;
        this.insuranceServiceUrl = insuranceServiceUrl;
        this.tourpackageServiceUrl = tourpackageServiceUrl;
        this.accommodationServiceUrl = accommodationServiceUrl;
        this.vehiclerentalServiceUrl = vehiclerentalServiceUrl;
    }

    /**
     * Confirm payment and update service resource status.
     * Routes to the appropriate service based on serviceName.
     * 
     * @param serviceName The name of the service (flight, insurance, tourpackage, accommodation, vehiclerental)
     * @param dto Contains serviceReferenceId (resource ID) and customerId
     */
    public void confirmPayment(String serviceName, ConfirmPaymentRequestDTO dto) {
        if (serviceName == null || serviceName.isBlank() || dto == null || dto.getServiceReferenceId() == null) {
            logger.warn("Invalid confirm payment request: serviceName or serviceReferenceId is null");
            return;
        }

        String serviceNameLower = serviceName.toLowerCase();
        String serviceUrl = getServiceUrl(serviceNameLower);
        String endpoint = getServiceEndpoint(serviceNameLower);

        if (serviceUrl == null || serviceUrl.isBlank()) {
            logger.warn("No service URL configured for service: {}", serviceName);
            return;
        }

        try {
            logger.info("=== Starting Payment Confirmation ===");
            logger.info("Service: {}", serviceNameLower);
            logger.info("Service URL: {}", serviceUrl);
            logger.info("Endpoint: {}", endpoint);
            logger.info("Full URL: {}{}", serviceUrl, endpoint);
            logger.info("Service Reference ID: {}", dto.getServiceReferenceId());
            logger.info("Customer ID: {}", dto.getCustomerId());

            WebClient wc = WebClient.builder()
                    .baseUrl(serviceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            logger.info("Sending POST request to update booking status...");

            wc.post()
                    .uri(endpoint)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            logger.error("=== Payment Confirmation HTTP Error ===");
                            logger.error("Service: {}", serviceNameLower);
                            logger.error("Status Code: {}", clientResponse.statusCode());
                            logger.error("Response Body: {}", body);
                            return Mono.error(new RuntimeException(serviceNameLower + " service HTTP " + clientResponse.statusCode() + ": " + body));
                        })
                    )
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(5))
                    .doOnSuccess(response -> logger.info("=== Payment Confirmation Success ===\nService: {}\nBooking status updated successfully", serviceNameLower))
                    .doOnError(error -> {
                        logger.error("=== Payment Confirmation Error/Timeout ===");
                        logger.error("Service: {}", serviceNameLower);
                        logger.error("Error Type: {}", error.getClass().getSimpleName());
                        logger.error("Error Message: {}", error.getMessage());
                    })
                    .block();

            logger.info("Payment confirmation completed for: {}", serviceNameLower);
        } catch (Exception ex) {
            logger.error("=== Payment Confirmation Exception ===");
            logger.error("Service: {}", serviceNameLower);
            logger.error("Exception Type: {}", ex.getClass().getSimpleName());
            logger.error("Exception Message: {}", ex.getMessage());
            logger.error("Full Stack:", ex);
        }
    }

    /**
     * Get the service URL based on service name
     */
    private String getServiceUrl(String serviceNameLower) {
        return switch (serviceNameLower) {
            case "flight" -> flightServiceUrl;
            case "insurance" -> insuranceServiceUrl;
            case "tourpackage" -> tourpackageServiceUrl;
            case "accommodation" -> accommodationServiceUrl;
            case "vehiclerental" -> vehiclerentalServiceUrl;
            default -> null;
        };
    }

    /**
     * Get the endpoint path based on service name.
     * Format: /api/booking/payment/confirm (universal for all services)
     * Special case: insurance service uses "policy" endpoint
     */
    private String getServiceEndpoint(String serviceNameLower) {
        if ("insurance".equals(serviceNameLower)) {
            return "/api/policy/payment/confirm";
        }
        return "/api/booking/payment/confirm";
    }

    // Generic wrapper class for service responses
    public static class ServiceResponseWrapper {
        private Integer status;
        private String message;
        private Object data;

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}

