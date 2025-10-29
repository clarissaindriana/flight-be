package apap.ti._5.flight_2306211660_be.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "seats")
public class Seat {
    
    @Id
    @Column(columnDefinition = "INT")
    private Integer id;
    
    @Column(name = "class_flight_id", nullable = false)
    private Integer classFlightId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_flight_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ClassFlight classFlight;
    
    @Column(name = "passenger_id")
    private UUID passengerId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Passenger passenger;
    
    @Column(name = "seat_code", nullable = false)
    private String seatCode;
    
    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_at_decimal")
    private BigDecimal updatedAtDecimal;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isBooked == null) {
            this.isBooked = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}