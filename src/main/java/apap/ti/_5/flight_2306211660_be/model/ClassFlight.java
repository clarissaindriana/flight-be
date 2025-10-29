package apap.ti._5.flight_2306211660_be.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "class_flights")
public class ClassFlight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "flight_id", nullable = false)
    private String flightId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Flight flight;
    
    @Column(name = "class_type", nullable = false)
    private String classType;
    
    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity;
    
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @OneToMany(mappedBy = "classFlight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Seat> classSeats;
    
    @OneToMany(mappedBy = "classFlight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Booking> bookings;
}