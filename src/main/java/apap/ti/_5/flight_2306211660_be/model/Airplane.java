package apap.ti._5.flight_2306211660_be.model;

import java.time.LocalDateTime;

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
@Table(name = "airplanes")
public class Airplane {
    
    @Id
    private String id;
    
    @Column(name = "airline_id", nullable = false)
    private String airlineId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airline_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Airline airline;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity;
    
    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
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