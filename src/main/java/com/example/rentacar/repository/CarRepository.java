package com.example.rentacar.repository;

import com.example.rentacar.domain.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    @Query("select count (*) from  Car c join c.image img where img.id=:id")
    Integer findCarCountByImagId(@Param("id") String id);

    @EntityGraph(attributePaths = {"image"})
    List<Car> findAll();

    @EntityGraph(attributePaths = {"image"})
    Page<Car> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "image")
    Optional<Car> findCarById(Long aLong);

    @Query("select c from Car c join c.image im where im.id=:id")
    List<Car>findCarsByImageId(@Param("id")String id);

    @EntityGraph(attributePaths = {"id"})
    List<Car> getAllBy();
}
