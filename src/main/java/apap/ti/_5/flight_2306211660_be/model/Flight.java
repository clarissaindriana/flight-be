package apap.ti._5.flight_2306211660_be.model;

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
@Table(name = "flights")
public class Flight {
    
    @Id
    private String id;
    
    @Column(name = "airline_id", nullable = false)
    private String airlineId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airline_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Airline airline;
    
    @Column(name = "airplane_id", nullable = false)
    private String airplaneId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airplane_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Airplane airplane;
    
    @Column(name = "origin_airport_code", nullable = false)
    private String originAirportCode;
    
    @Column(name = "destination_airport_code", nullable = false)
    private String destinationAirportCode;
    
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;
    
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;
    
    @Column(nullable = false)
    private String terminal;
    
    @Column(nullable = false)
    private String gate;
    
    @Column(name = "baggage_allowance", nullable = false)
    private Integer baggageAllowance;
    
    @Column(columnDefinition = "TEXT")
    private String facilities;
    
    @Column(nullable = false)
    private Integer status; // 1=Scheduled, 2=In Flight, 3=Finished, 4=Delayed, 5=Cancelled
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // FALSE = aktif (default), TRUE = cancelled/nonaktif
    
    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassFlight> classes;
    
    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Booking> bookings;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        // Set default status to Scheduled (1) upon creation
        if (this.status == null) {
            this.status = 1; 
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}