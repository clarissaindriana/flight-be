package apap.ti._5.flight_2306211660_be.restdto.response.booking;

import java.util.List;

public class BookingChartResultDTO {
    private List<BookingChartResponseDTO> chart;
    private BookingChartSummaryDTO summary;

    public BookingChartResultDTO() {}

    public BookingChartResultDTO(List<BookingChartResponseDTO> chart, BookingChartSummaryDTO summary) {
        this.chart = chart;
        this.summary = summary;
    }

    public List<BookingChartResponseDTO> getChart() {
        return chart;
    }

    public void setChart(List<BookingChartResponseDTO> chart) {
        this.chart = chart;
    }

    public BookingChartSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(BookingChartSummaryDTO summary) {
        this.summary = summary;
    }
}
