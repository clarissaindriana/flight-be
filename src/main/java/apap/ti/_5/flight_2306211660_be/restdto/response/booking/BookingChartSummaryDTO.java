package apap.ti._5.flight_2306211660_be.restdto.response.booking;

import java.math.BigDecimal;

public class BookingChartSummaryDTO {
    private Long totalBookings;
    private BigDecimal totalRevenue;
    private String topPerformer;

    public BookingChartSummaryDTO() {}

    public BookingChartSummaryDTO(Long totalBookings, BigDecimal totalRevenue, String topPerformer) {
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.topPerformer = topPerformer;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getTopPerformer() {
        return topPerformer;
    }

    public void setTopPerformer(String topPerformer) {
        this.topPerformer = topPerformer;
    }
}
