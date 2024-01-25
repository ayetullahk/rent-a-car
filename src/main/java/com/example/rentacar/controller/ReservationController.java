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

    /**
     * Method that creates a reservation for the specified car.
     * The user must have the permission to reserve the specified car.
     *
     * @param carId              - Identifier of the car for which the reservation is to be made.
     * @param reservationRequest - Request body containing the details of the reservation.
     * @return ResponseEntity object containing a response indicating the creation of the reservation.
     * The response includes a VRResponse object with the success status and message.
     * @throws NotFoundException     - If the specified car is not found.
     * @throws UnauthorizedException - If the user does not have the permission to make a reservation.
     */
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

    /**
     * This method should be called by users with ADMIN role.
     * Processes the reservation request of a user and returns the result in a ResponseEntity.
     *
     * @param userId             The identifier of the user making the reservation.
     * @param carId              The identifier of the car for which the reservation is to be made.
     * @param reservationRequest The request body containing the reservation details. Should be valid (@Valid).
     * @return ResponseEntity containing information about the reservation process.
     * Contains HttpStatus.CREATED and true for success status.
     * @throws NotFoundException Thrown if the specified user or car is not found.
     */
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

    /**
     * Retrieves all reservations from the system.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return ResponseEntity containing a list of ReservationDTOs if successful.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws Exception Thrown if an unexpected error occurs during the retrieval process.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> allReservations = reservationService.getAllReservations();

        return ResponseEntity.ok(allReservations);
    }

    /**
     * Retrieves a paginated list of all reservations from the system.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param page      The page number to retrieve (0-indexed).
     * @param size      The number of reservations per page.
     * @param prop      The property by which to sort the results.
     * @param direction The sorting direction, either ASC (ascending) or DESC (descending). Default is DESC.
     * @return ResponseEntity containing a Page of ReservationDTOs if successful.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws Exception Thrown if an unexpected error occurs during the retrieval process.
     */
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

    /**
     * Checks the availability of a car for reservation within the specified time range.
     * This endpoint is accessible to users with either ADMIN or CUSTOMER roles.
     *
     * @param carId          The identifier of the car to check for availability.
     * @param pickUpDateTime The date and time when the car will be picked up.
     * @param dropOffTime    The date and time when the car will be dropped off.
     * @return ResponseEntity containing a VRResponse with information about car availability.
     * The response includes a message, success status, availability status, and total price if available.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified car is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid or the specified time range is not valid.
     */
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

    /**
     * Updates an existing reservation for a car.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param carId                    The identifier of the car associated with the reservation.
     * @param reservationId            The identifier of the reservation to be updated.
     * @param reservationUpdateRequest The request body containing the updates for the reservation. Should be valid (@Valid).
     * @return ResponseEntity containing a VRResponse with information about the update process.
     * The response includes a message and success status.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified car or reservation is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid or the update request is not valid.
     * @throws UnauthorizedException Thrown if the user is not authorized to update the reservation.
     */
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

    /**
     * Retrieves the details of a reservation by its identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id The identifier of the reservation to retrieve.
     * @return ResponseEntity containing a ReservationDTO with information about the reservation.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified reservation is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to access the reservation details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        ReservationDTO reservationDTO = reservationService.getReservationDTO(id);
        return ResponseEntity.ok(reservationDTO);
    }

    /**
     * Retrieves a paginated list of reservations for a specific user.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param userId    The identifier of the user for whom reservations are to be retrieved.
     * @param page      The page number to retrieve (0-indexed).
     * @param size      The number of reservations per page.
     * @param prop      The property by which to sort the results.
     * @param direction The sorting direction, either ASC (ascending) or DESC (descending). Default is DESC.
     * @return ResponseEntity containing a Page of ReservationDTOs for the specified user.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified user is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     * @throws UnauthorizedException Thrown if the user is not authorized to access reservations for the specified user.
     */
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

    /**
     * Retrieves the details of a reservation for the authenticated user by its identifier.
     * This endpoint is accessible to users with either ADMIN or CUSTOMER roles.
     *
     * @param id The identifier of the reservation to retrieve.
     * @return ResponseEntity containing a ReservationDTO with information about the reservation.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified reservation is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to access the reservation details.
     */
    @GetMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<ReservationDTO> getUserReservationById(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        ReservationDTO reservationDTO = reservationService.findByIdAndUser(id, user);

        return ResponseEntity.ok(reservationDTO);
    }

    /**
     * Retrieves a paginated list of reservations for the authenticated user.
     * This endpoint is accessible to users with either ADMIN or CUSTOMER roles.
     *
     * @param page      The page number to retrieve (0-indexed).
     * @param size      The number of reservations per page.
     * @param prop      The property by which to sort the results.
     * @param direction The sorting direction, either ASC (ascending) or DESC (descending). Default is DESC.
     * @return ResponseEntity containing a Page of ReservationDTOs for the authenticated user.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified user is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     * @throws UnauthorizedException Thrown if the user is not authorized to access their reservations.
     */
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

    /**
     * Deletes a reservation with the specified identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id The identifier of the reservation to be deleted.
     * @return ResponseEntity containing a VRResponse with information about the deletion process.
     * The response includes a message and success status.
     * The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException     Thrown if the specified reservation is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to delete the reservation.
     */
    @DeleteMapping("/admin/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> deleteReservation(@PathVariable Long id) {
        reservationService.removeById(id);

        VRResponse response = new VRResponse(ResponseMessage.RESERVATION_DELETE_RESPONSE_MESSAGE, true);

        return ResponseEntity.ok(response);
    }


}
