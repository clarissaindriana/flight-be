package apap.ti._5.flight_2306211660_be.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "bookings")
public class Booking {
    
    @Id
    private String id;
    
    @Column(name = "flight_id", nullable = false)
    private String flightId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Flight flight;
    
    @Column(name = "class_flight_id", nullable = false)
    private Integer classFlightId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_flight_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ClassFlight classFlight;
    
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;
    
    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;
    
    @Column(nullable = false)
    private Integer status;
    
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    // Removed direct relationship to Passenger - using BookingPassenger junction table instead
    
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookingPassenger> bookingPassengers;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
