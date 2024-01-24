package com.example.rentacar.repository;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.Reservation;
import com.example.rentacar.domain.User;
import com.example.rentacar.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("select r from Reservation r " +
            "join fetch Car c on r.car=c.id where " +
            "c.id=:carId and (r.status not in :status)and :pickUpTime between r.pickUpTime and r.dropOffTime  " +
            "or " +
            "c.id=:carId and (r.status not in :status)and :dropOffTime between r.pickUpTime and r.dropOffTime " +
            "or " +
            "c.id=:carId and (r.status not in :status)and(r.pickUpTime between :pickUpTime and:dropOffTime)")
    List<Reservation> checkCarStatus(@Param("carId") Long carId,
                                     @Param("pickUpTime") LocalDateTime pickUpTime,
                                     @Param("dropOffTime") LocalDateTime dropOffTime,
                                     @Param("status") ReservationStatus[] status);

    //sadece carları getirir ve carların imagelerini de getirir ama image data gelmez
    @EntityGraph(attributePaths = {"car", "car.image"})
    List<Reservation> findAll();

    @EntityGraph(attributePaths = {"car", "car.image"})
    Page<Reservation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"car", "car.image", "user"})
    Optional<Reservation> findById(Long id);

    @EntityGraph(attributePaths = {"car", "car.image", "user"})
    Page<Reservation> findAllByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"car", "car.image", "user"})
    Optional<Reservation> findByIdAndUser(Long id, User user);

    boolean existsByCar(Car car);

    boolean existsByUser(User user);

    @EntityGraph(attributePaths = {"car","user"})
    List<Reservation> findAllBy();
}

