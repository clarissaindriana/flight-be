package apap.ti._5.flight_2306211660_be.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
@Table(name = "passengers")
public class Passenger {
    
    @Id
    private UUID id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    
    @Column(nullable = false)
    private Integer gender;
    
    @Column(name = "id_passport", nullable = false)
    private String idPassport;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "passenger", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Seat> seatsList;
    
    @OneToMany(mappedBy = "passenger", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookingPassenger> bookingPassengers;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
