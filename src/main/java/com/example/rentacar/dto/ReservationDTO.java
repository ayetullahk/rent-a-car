package com.example.rentacar.dto;

import com.example.rentacar.domain.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {

    private Long id;

    private CarDTO car;

    private Long userId;

    private LocalDateTime pickUpTime;

    private LocalDateTime dropOffTime;

    private String pickUpLocation;

    private String dropOffLocation;

    private ReservationStatus status;

    private Double totalPrice;
}
