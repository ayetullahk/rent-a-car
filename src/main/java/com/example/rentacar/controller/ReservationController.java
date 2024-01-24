package com.example.rentacar.controller;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.User;
import com.example.rentacar.dto.ReservationDTO;
import com.example.rentacar.dto.request.ReservationRequest;
import com.example.rentacar.dto.request.ReservationUpdateRequest;
import com.example.rentacar.dto.response.CarAvailabilityResponse;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.service.CarService;
import com.example.rentacar.service.ReservationService;
import com.example.rentacar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CarService carService;

    @Autowired
    UserService userService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<VRResponse> makeReservation(@RequestParam("carId") Long carId,
                                                      @Valid @RequestBody ReservationRequest reservationRequest) {

        Car car = carService.getCarById(carId);
        User user = userService.getCurrentUser();
        reservationService.createReservation(reservationRequest, user, car);

        VRResponse response = new VRResponse(ResponseMessage.RESERVATION_CREATED_RESPONSE_MESSAGE, true);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/aad/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> addReservation(@RequestParam("userId") Long userId,
                                                     @RequestParam("carId") Long carId,
                                                     @Valid @RequestBody ReservationRequest reservationRequest) {
        Car car = carService.getCarById(carId);
        User user = userService.getById(userId);
        reservationService.createReservation(reservationRequest, user, car);

        VRResponse response = new VRResponse(ResponseMessage.RESERVATION_CREATED_RESPONSE_MESSAGE, true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> allReservations = reservationService.getAllReservations();

        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("/admin/all/pages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationDTO>> getAllReservationsWithPage(@RequestParam("page") int page,
                                                                           @RequestParam("size") int size,
                                                                           @RequestParam("sort") String prop,
                                                                           @RequestParam(value = "direction", required = false,
                                                                                   defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
        Page<ReservationDTO> allReservations = reservationService.getReservationPage(pageable);

        return ResponseEntity.ok(allReservations);

    }

    @GetMapping("/auth")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<VRResponse> checkCarIsAvailable(@RequestParam("carId") Long carId,
                                                          @RequestParam("pickUpDateTime")
                                                          @DateTimeFormat(pattern = "MM/dd/yyyy HH:mm:ss") LocalDateTime pickUpDateTime,
                                                          @RequestParam("dropOffDateTime")
                                                          @DateTimeFormat(pattern = "MM/dd/yyyy HH:mm:ss") LocalDateTime dropOffTime) {
        Car car = carService.getCarById(carId);
        boolean isAvailable = reservationService.checkCarAvailability(car, pickUpDateTime, dropOffTime);
        Double totalPrice = reservationService.getTotalPrice(car, pickUpDateTime, dropOffTime);

        VRResponse response = new CarAvailabilityResponse(ResponseMessage.CAR_AVAILABLE_MESSAGE, true, isAvailable, totalPrice);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> updateReservation(@RequestParam("carId") Long carId,
                                                        @RequestParam("reservationId") Long reservationId,
                                                        @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest) {
        Car car = carService.getCarById(carId);
        reservationService.updateReservation(reservationId, car, reservationUpdateRequest);

        VRResponse response = new VRResponse(ResponseMessage.RESERVATION_UPDATED_RESPONSE_MESSAGE, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        ReservationDTO reservationDTO = reservationService.getReservationDTO(id);
        return ResponseEntity.ok(reservationDTO);
    }

    @GetMapping("/admin/auth/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationDTO>> getAllUserReservations(@RequestParam("userId") Long userId,
                                                                       @RequestParam("page") int page,
                                                                       @RequestParam("size") int size,
                                                                       @RequestParam("sort") String prop,
                                                                       @RequestParam(value = "direction", required = false,
                                                                               defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));

        User user = userService.getById(userId);
        Page<ReservationDTO> reservationDTOPage = reservationService.findReservationPageByUser(user, pageable);

        return ResponseEntity.ok(reservationDTOPage);
    }

    @GetMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<ReservationDTO> getUserReservationById(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        ReservationDTO reservationDTO = reservationService.findByIdAndUser(id, user);

        return ResponseEntity.ok(reservationDTO);
    }

    @GetMapping("/auth/all")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<Page<ReservationDTO>> getAllUserReservation(@RequestParam("page") int page,
                                                                      @RequestParam("size") int size,
                                                                      @RequestParam("sort") String prop,
                                                                      @RequestParam(value = "direction", required = false,
                                                                              defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));

        User user = userService.getCurrentUser();

        Page<ReservationDTO> reservationDTOPage = reservationService.findReservationPageByUser(user, pageable);

        return ResponseEntity.ok(reservationDTOPage);
    }
    @DeleteMapping("/admin/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse>deleteReservation(@PathVariable Long id){
        reservationService.removeById(id);

        VRResponse response=new VRResponse(ResponseMessage.RESERVATION_DELETE_RESPONSE_MESSAGE,true);

        return ResponseEntity.ok(response);
    }


}
