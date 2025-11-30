package apap.ti._5.flight_2306211660_be.restcontroller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import apap.ti._5.flight_2306211660_be.model.Booking;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.restcontroller.booking.BookingRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.booking.BookingRestService;
import static org.mockito.Mockito.lenient;

/**
 * Standalone MockMvc tests to push coverage for
 * [BookingRestController](flight-2306211660-be/src/main/java/apap/ti/_5/flight_2306211660_be/restcontroller/booking/BookingRestController.java:1)
 */
@ExtendWith(MockitoExtension.class)
class BookingRestControllerTest {

    @Mock
    private BookingRestService bookingRestService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingRestController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        controller = new BookingRestController();
        ReflectionTestUtils.setField(controller, "bookingRestService", bookingRestService);
        ReflectionTestUtils.setField(controller, "bookingRepository", bookingRepository);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpMessageConverter<?> jsonConverter = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(jsonConverter)
                .addFilter((request, response, chain) -> {
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password",
                            java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
                    chain.doFilter(request, response);
                })
                .build();

        // Mock security context with SUPERADMIN role
        var auth = new UsernamePasswordAuthenticationToken("admin", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String toJson(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    private BookingResponseDTO bookingDTO(String id) {
        return BookingResponseDTO.builder()
                .id(id)
                .flightId("FL-1")
                .classFlightId(10)
                .classType("economy")
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .status(1)
                .totalPrice(new BigDecimal("1000000"))
                .isDeleted(false)
                .build();
    }

    // GET /api/booking (list)

    @Test
    @DisplayName("GET /api/booking without params -> 200, calls getAllBookings(includeDeleted=null)")
    void getAll_noParams() throws Exception {
        when(bookingRestService.getAllBookings(null, null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(bookingRestService).getAllBookings(null, null, null, null);
    }

    @Test
    @DisplayName("GET /api/booking with flightId only -> 200, calls getBookingsByFlight(flightId, null)")
    void getAll_withFlightId() throws Exception {
        when(bookingRestService.getBookingsByFlight("FL-1", null, null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/booking").param("flightId", "FL-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(bookingRestService).getBookingsByFlight("FL-1", null, null, null, null);
    }

    @Test
    @DisplayName("GET /api/booking with flightId and includeDeleted=true -> 200")
    void getAll_withFlightIdIncludeDeleted() throws Exception {
        when(bookingRestService.getBookingsByFlight("FL-1", true, null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/booking")
                        .param("flightId", "FL-1")
                        .param("includeDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(bookingRestService).getBookingsByFlight("FL-1", true, null, null, null);
    }

    @Test
    @DisplayName("GET /api/booking filters by customer email when role is CUSTOMER")
    void getAll_customerOwnershipFilter() throws Exception {
        var booking1 = BookingResponseDTO.builder()
                .id("B-1")
                .flightId("FL-1")
                .classFlightId(10)
                .classType("economy")
                .contactEmail("customer@x.com")
                .contactPhone("08123")
                .passengerCount(1)
                .status(1)
                .totalPrice(new BigDecimal("1000000"))
                .isDeleted(false)
                .build();
        var booking2 = BookingResponseDTO.builder()
                .id("B-2")
                .flightId("FL-1")
                .classFlightId(10)
                .classType("economy")
                .contactEmail("other@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .status(1)
                .totalPrice(new BigDecimal("1000000"))
                .isDeleted(false)
                .build();
        when(bookingRestService.getAllBookings(null, null, null, null)).thenReturn(List.of(booking1, booking2));

        // Set CUSTOMER role for this test
        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn("customer@x.com");

            mockMvc.perform(get("/api/booking"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value("B-1"));
        }
    }

    // GET /api/booking/{id}

    @Test
    @DisplayName("GET /api/booking/{id} found -> 200")
    void get_one_found() throws Exception {
        when(bookingRestService.getBooking("B-1")).thenReturn(bookingDTO("B-1"));

        mockMvc.perform(get("/api/booking/B-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("B-1"));
    }

    @Test
    @DisplayName("GET /api/booking/{id} not found -> 404")
    void get_one_notFound() throws Exception {
        when(bookingRestService.getBooking("NF")).thenReturn(null);

        mockMvc.perform(get("/api/booking/NF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/booking/{id} customer forbidden -> 403")
    void get_one_customerForbidden() throws Exception {
        var booking = BookingResponseDTO.builder()
                .id("B-1")
                .contactEmail("other@x.com")
                .build();
        when(bookingRestService.getBooking("B-1")).thenReturn(booking);

        // Set CUSTOMER role
        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn("customer@x.com");

            mockMvc.perform(get("/api/booking/B-1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }

    // POST /api/booking/create

    @Test
    @DisplayName("POST /api/booking/create valid -> 201")
    void create_valid() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        when(bookingRestService.createBooking(any(AddBookingRequestDTO.class))).thenReturn(bookingDTO("NEW-1"));

        mockMvc.perform(post("/api/booking/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("NEW-1"));
    }

    @Test
    @DisplayName("POST /api/booking/create invalid binding -> 400")
    void create_invalidBinding() throws Exception {
        // Intentionally invalid: missing requireds (expect @Valid to catch)
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId(null)
                .classFlightId(null)
                .contactEmail(null)
                .contactPhone(null)
                .passengerCount(null)
                .passengers(null)
                .build();

        mockMvc.perform(post("/api/booking/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/booking/create IllegalArgumentException -> 400")
    void create_illegalArgument() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        org.mockito.Mockito.lenient().when(bookingRestService.createBooking(any(AddBookingRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(post("/api/booking/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/booking/create Exception -> 500")
    void create_exception() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        org.mockito.Mockito.lenient().when(bookingRestService.createBooking(any(AddBookingRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/booking/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // PUT /api/booking/update

    @Test
    @DisplayName("PUT /api/booking/update valid -> 200")
    void update_valid() throws Exception {
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("B-1")
                .contactEmail("new@y.com")
                .contactPhone("08123")
                .build();

        when(bookingRestService.updateBooking(any(UpdateBookingRequestDTO.class))).thenReturn(bookingDTO("B-1"));

        mockMvc.perform(put("/api/booking/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("B-1"));
    }

    @Test
    @DisplayName("PUT /api/booking/update invalid binding -> 400")
    void update_invalidBinding() throws Exception {
        // Missing id and contact fields
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("")
                .contactEmail("")
                .contactPhone("")
                .build();

        mockMvc.perform(put("/api/booking/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/booking/update not found -> 404")
    void update_notFound() throws Exception {
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("NF")
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .build();

        org.mockito.Mockito.lenient().when(bookingRestService.updateBooking(any(UpdateBookingRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/booking/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/booking/update IllegalArgument/IllegalState -> 400")
    void update_illegalArgOrState() throws Exception {
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("ERR")
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .build();

        org.mockito.Mockito.lenient().when(bookingRestService.updateBooking(any(UpdateBookingRequestDTO.class)))
                .thenThrow(new IllegalStateException("nope"));

        mockMvc.perform(put("/api/booking/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/booking/update Exception -> 500")
    void update_exception() throws Exception {
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("ERR2")
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .build();

        org.mockito.Mockito.lenient().when(bookingRestService.updateBooking(any(UpdateBookingRequestDTO.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(put("/api/booking/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }


    // GET /api/booking/statistics



    // POST /api/booking/delete/{id}

    @Test
    @DisplayName("POST /api/booking/delete/{id} valid -> 200")
    void delete_ok() throws Exception {
        when(bookingRestService.deleteBooking("DEL-1")).thenReturn(bookingDTO("DEL-1"));

        mockMvc.perform(post("/api/booking/delete/DEL-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("DEL-1"));
    }

    @Test
    @DisplayName("POST /api/booking/delete/{id} not found -> 404")
    void delete_notFound() throws Exception {
        org.mockito.Mockito.lenient().when(bookingRestService.deleteBooking("NF")).thenReturn(null);

        mockMvc.perform(post("/api/booking/delete/NF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/booking/delete/{id} IllegalState -> 400")
    void delete_illegalState() throws Exception {
        when(bookingRestService.deleteBooking("ERR")).thenThrow(new IllegalStateException("nope"));

        mockMvc.perform(post("/api/booking/delete/ERR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/booking/delete/{id} Exception -> 500")
    void delete_exception() throws Exception {
        when(bookingRestService.deleteBooking("ERR2")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/booking/delete/ERR2"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("POST /api/booking/delete/{id} customer forbidden -> 403")
    void delete_customerForbidden() throws Exception {
        var existing = BookingResponseDTO.builder()
                .id("B-1")
                .contactEmail("other@x.com")
                .build();
        when(bookingRestService.getBooking("B-1")).thenReturn(existing);

        // Set CUSTOMER role
        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn("customer@x.com");

            mockMvc.perform(post("/api/booking/delete/B-1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }

    // GET /api/booking/today

    @Test
    @DisplayName("GET /api/booking/today returns 200 with count")
    void getTodayBookings_ok() throws Exception {
        when(bookingRestService.getTodayBookings()).thenReturn(5L);

        mockMvc.perform(get("/api/booking/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("GET /api/booking/today returns 500 on exception")
    void getTodayBookings_exception() throws Exception {
        when(bookingRestService.getTodayBookings()).thenThrow(new RuntimeException("db error"));

        mockMvc.perform(get("/api/booking/today"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // GET /api/booking/chart

    @Test
    @DisplayName("GET /api/booking/chart returns 200 with chart data")
    void getBookingChart_ok() throws Exception {
        var chartData = new apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResultDTO();
        chartData.setChart(List.of());
        var summary = new apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartSummaryDTO();
        summary.setTotalBookings(10L);
        summary.setTotalRevenue(BigDecimal.valueOf(1000.0));
        summary.setTopPerformer("Airline1");
        chartData.setSummary(summary);

        lenient().when(bookingRestService.getBookingChartData(1, 2024)).thenReturn(chartData);

        mockMvc.perform(get("/api/booking/chart")
                .param("month", "1")
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("GET /api/booking/chart returns 200 with empty chart")
    void getBookingChart_empty() throws Exception {
        var chartData = new apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResultDTO();
        chartData.setChart(Collections.emptyList());

        lenient().when(bookingRestService.getBookingChartData(1, 2024)).thenReturn(chartData);

        mockMvc.perform(get("/api/booking/chart")
                .param("month", "1")
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("GET /api/booking/chart returns 400 on invalid request")
    void getBookingChart_badRequest() throws Exception {
        when(bookingRestService.getBookingChartData(13, 2024)).thenThrow(new IllegalArgumentException("Invalid month"));

        mockMvc.perform(get("/api/booking/chart")
                .param("month", "13")
                .param("year", "2024"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // POST /api/booking/payment/confirm

    @Test
    @DisplayName("POST /api/booking/payment/confirm returns 200 on success")
    void confirmPayment_ok() throws Exception {
        var req = new ConfirmPaymentRequestDTO();
        req.setBillId(java.util.UUID.randomUUID());
        req.setCustomerId("cust1");

        when(bookingRestService.confirmPayment(any(ConfirmPaymentRequestDTO.class))).thenReturn(bookingDTO("B-1"));

        mockMvc.perform(post("/api/booking/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking payment confirmed"));
    }

    @Test
    @DisplayName("POST /api/booking/payment/confirm throws IllegalArgumentException")
    void confirmPayment_illegalArgument() throws Exception {
        var req = new ConfirmPaymentRequestDTO();
        req.setBillId(java.util.UUID.randomUUID());
        req.setCustomerId("cust1");

        when(bookingRestService.confirmPayment(any(ConfirmPaymentRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Booking not found"));

        mockMvc.perform(post("/api/booking/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/booking/payment/confirm throws SecurityException")
    void confirmPayment_securityException() throws Exception {
        var req = new ConfirmPaymentRequestDTO();
        req.setBillId(java.util.UUID.randomUUID());
        req.setCustomerId("cust1");

        when(bookingRestService.confirmPayment(any(ConfirmPaymentRequestDTO.class)))
                .thenThrow(new SecurityException("Access denied"));

        mockMvc.perform(post("/api/booking/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/booking/payment/confirm throws IllegalStateException")
    void confirmPayment_illegalState() throws Exception {
        var req = new ConfirmPaymentRequestDTO();
        req.setBillId(java.util.UUID.randomUUID());
        req.setCustomerId("cust1");

        when(bookingRestService.confirmPayment(any(ConfirmPaymentRequestDTO.class)))
                .thenThrow(new IllegalStateException("Invalid booking state"));

        mockMvc.perform(post("/api/booking/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/booking/payment/confirm throws generic Exception")
    void confirmPayment_exception() throws Exception {
        var req = new ConfirmPaymentRequestDTO();
        req.setBillId(java.util.UUID.randomUUID());
        req.setCustomerId("cust1");

        when(bookingRestService.confirmPayment(any(ConfirmPaymentRequestDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/booking/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    // Additional edge case tests for complete coverage

    @Test
    @DisplayName("POST /api/booking/create customer forces email to JWT")
    void create_customerForcesEmail() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("other@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        lenient().when(bookingRestService.createBooking(any(AddBookingRequestDTO.class))).thenReturn(bookingDTO("NEW-1"));

        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn("correct@x.com");

            mockMvc.perform(post("/api/booking/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(req)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("PUT /api/booking/update customer ensures ownership")
    void update_customerEnsuresOwnership() throws Exception {
        UpdateBookingRequestDTO req = UpdateBookingRequestDTO.builder()
                .id("B-1")
                .contactEmail("other@y.com")
                .contactPhone("08123")
                .build();

        var existing = BookingResponseDTO.builder()
                .id("B-1")
                .contactEmail("other@x.com")
                .build();
        lenient().when(bookingRestService.getBooking("B-1")).thenReturn(existing);

        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn("customer@x.com");

            mockMvc.perform(put("/api/booking/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }


    @Test
    @DisplayName("POST /api/booking/create auto bill creation works")
    void create_billAutoCreation() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        lenient().when(bookingRestService.createBooking(any(AddBookingRequestDTO.class))).thenReturn(bookingDTO("NEW-1"));

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/booking/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201));
        }
    }

    @Test
    @DisplayName("POST /api/booking/create with null customerEmail")
    void create_nullCustomerEmail() throws Exception {
        AddBookingRequestDTO req = AddBookingRequestDTO.builder()
                .flightId("FL-1")
                .classFlightId(10)
                .contactEmail("x@y.com")
                .contactPhone("08123")
                .passengerCount(1)
                .passengers(List.of(
                        apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO.builder()
                                .fullName("A")
                                .birthDate(LocalDate.now().minusYears(20))
                                .gender(1)
                                .idPassport("P1")
                                .build()
                ))
                .build();

        lenient().when(bookingRestService.createBooking(any(AddBookingRequestDTO.class))).thenReturn(bookingDTO("NEW-1"));

        var auth = new UsernamePasswordAuthenticationToken("customer", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try (MockedStatic<apap.ti._5.flight_2306211660_be.config.security.CurrentUser> mocked = mockStatic(apap.ti._5.flight_2306211660_be.config.security.CurrentUser.class)) {
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(apap.ti._5.flight_2306211660_be.config.security.CurrentUser::getEmail).thenReturn(null);

            mockMvc.perform(post("/api/booking/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(req)))
                    .andExpect(status().isCreated());
        }
    }
}
