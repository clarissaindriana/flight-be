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
     * @param serviceName The name of the service (flight, insurance, tourpackage, accomodation, vehiclerental)
     * @param dto Contains serviceReferenceId (resource ID) and customerId
     * @return Updated resource response or null on failure
     */
    public ServiceResponseWrapper confirmPayment(String serviceName, ConfirmPaymentRequestDTO dto) {
        if (serviceName == null || serviceName.isBlank() || dto == null || dto.getServiceReferenceId() == null) {
            logger.warn("Invalid confirm payment request: serviceName or serviceReferenceId is null");
            return null;
        }

        String serviceNameLower = serviceName.toLowerCase();
        String serviceUrl = getServiceUrl(serviceNameLower);
        String endpoint = getServiceEndpoint(serviceNameLower);

        if (serviceUrl == null || serviceUrl.isBlank()) {
            logger.warn("No service URL configured for service: {}", serviceName);
            return null;
        }

        try {
            logger.info("Confirming payment for {} service: {} (customer: {})", 
                    serviceNameLower, dto.getServiceReferenceId(), dto.getCustomerId());

            WebClient wc = WebClient.builder()
                    .baseUrl(serviceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Mono<ServiceResponseWrapper> mono = wc.post()
                    .uri(endpoint)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            logger.error("{} service confirm payment HTTP error: status={}, body={}", 
                                    serviceNameLower, clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException(serviceNameLower + " service HTTP " + clientResponse.statusCode() + ": " + body));
                        })
                    )
                    .bodyToMono(ServiceResponseWrapper.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(ex -> {
                        logger.error("{} service confirm payment error/timeout: {}", serviceNameLower, ex.getMessage());
                        return Mono.empty();
                    });

            ServiceResponseWrapper wrapper = mono.block();
            if (wrapper == null) {
                logger.warn("{} service returned null response for confirm payment", serviceNameLower);
                return null;
            }

            logger.info("Payment confirmed successfully for {} service: {}", serviceNameLower, dto.getServiceReferenceId());
            return wrapper;
        } catch (Exception ex) {
            logger.error("Failed to confirm payment with {} service: {} - {}", 
                    serviceNameLower, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            return null;
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
     * Format: /api/{serviceName}/payment/confirm
     * Special case: insurance service uses "policy" endpoint
     */
    private String getServiceEndpoint(String serviceNameLower) {
        if ("insurance".equals(serviceNameLower)) {
            return "/api/policy/payment/confirm";
        }
        return "/api/" + serviceNameLower + "/payment/confirm";
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

