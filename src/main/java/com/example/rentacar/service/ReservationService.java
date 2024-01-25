package com.example.rentacar.service;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.Reservation;
import com.example.rentacar.domain.User;
import com.example.rentacar.domain.enums.ReservationStatus;
import com.example.rentacar.dto.ReservationDTO;
import com.example.rentacar.dto.request.ReservationRequest;
import com.example.rentacar.dto.request.ReservationUpdateRequest;
import com.example.rentacar.exception.BadRequestException;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.mapper.ReservationMapper;
import com.example.rentacar.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationMapper reservationMapper;

    /**
     * Creates a new reservation based on the provided reservation request, user, and car.
     *
     * @param reservationRequest The reservation request containing details of the reservation.
     * @param user               The user making the reservation.
     * @param car                The car for which the reservation is being made.
     * @throws BadRequestException Thrown if the reservation time is incorrect or the car is not available.
     */
    public void createReservation(ReservationRequest reservationRequest, User user, Car car) {
        checkReservationTimeIsCorrect(reservationRequest.getPickUpTime(), reservationRequest.getDropOffTime());

        boolean carStatus = checkCarAvailability(car, reservationRequest.getPickUpTime(), reservationRequest.getDropOffTime());

        Reservation reservation = reservationMapper.reservationRequestToReservation(reservationRequest);

        if (carStatus) {
            reservation.setStatus(ReservationStatus.CREATED);
        } else {
            throw new BadRequestException(ErrorMessage.CAR_NOT_AVAILABLE_MESSAGE);
        }
        reservation.setCar(car);
        reservation.setUser(user);
        Double totalPrice = getTotalPrice(car, reservationRequest.getPickUpTime(), reservationRequest.getDropOffTime());
        reservation.setTotalPrice(totalPrice);
        reservationRepository.save(reservation);
    }

    /**
     * Checks if the provided reservation times are correct.
     *
     * @param pickUpTime   The pick-up time for the reservation.
     * @param dropOffTime  The drop-off time for the reservation.
     * @throws BadRequestException Thrown if the reservation times are incorrect.
     */
    public void checkReservationTimeIsCorrect(LocalDateTime pickUpTime, LocalDateTime dropOffTime) {
        LocalDateTime now = LocalDateTime.now();

        if (pickUpTime.isBefore(now)) {
            throw new BadRequestException(ErrorMessage.RESERVATION_TIME_INCORRECT_MESSAGE);
        }
        boolean isEqual = pickUpTime.isEqual(dropOffTime) ? true : false;
        boolean isBefore = pickUpTime.isBefore(dropOffTime) ? true : false;

        if (isEqual || !isBefore) {
            throw new BadRequestException(ErrorMessage.RESERVATION_TIME_INCORRECT_MESSAGE);
        }
    }

    /**
     * Checks if the specified car is available for reservation during the provided time range.
     *
     * @param car         The car to be checked for availability.
     * @param pickUpTime  The pick-up time for the reservation.
     * @param dropOffTime The drop-off time for the reservation.
     * @return true if the car is available, false otherwise.
     */
    public boolean checkCarAvailability(Car car, LocalDateTime pickUpTime, LocalDateTime dropOffTime) {

        List<Reservation> existReservations = getConflictReservations(car, pickUpTime, dropOffTime);
        return existReservations.isEmpty();
    }

    /**
     * Calculates the total price for reserving the specified car during the provided time range.
     *
     * @param car          The car for which the total price is calculated.
     * @param pickUpTime   The pick-up time for the reservation.
     * @param dropOffTime  The drop-off time for the reservation.
     * @return The total price for reserving the car during the specified time range.
     */
    public Double getTotalPrice(Car car, LocalDateTime pickUpTime, LocalDateTime dropOffTime) {
        Long minutes = ChronoUnit.MINUTES.between(pickUpTime, dropOffTime);
        double hours = Math.ceil(minutes / 60.0);
        return car.getPricePerHour() * hours;
    }

    /**
     * Retrieves a list of reservations that conflict with the specified time range for the given car.
     *
     * @param car          The car for which conflicting reservations are checked.
     * @param pickUpTime   The pick-up time for the reservation.
     * @param dropOffTime  The drop-off time for the reservation.
     * @return A list of reservations that conflict with the specified time range.
     * @throws BadRequestException if the pick-up time is after the drop-off time.
     */
    private List<Reservation> getConflictReservations(Car car, LocalDateTime pickUpTime, LocalDateTime dropOffTime) {
        if (pickUpTime.isAfter(dropOffTime)) {
            throw new BadRequestException(ErrorMessage.RESERVATION_TIME_INCORRECT_MESSAGE);
        }
        ReservationStatus[] status = {ReservationStatus.CANCELED, ReservationStatus.DONE};

        List<Reservation> existReservations = reservationRepository.checkCarStatus(car.getId(), pickUpTime, dropOffTime, status);

        return existReservations;
    }

    /**
     * Retrieves a list of all reservations in the system.
     *
     * @return A list of ReservationDTO objects representing all reservations.
     */
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservationMapper.map(reservations);
    }

    /**
     * Retrieves a paginated list of reservations based on the provided pagination parameters.
     *
     * @param pageable The pageable object specifying the page number, size, and sorting criteria.
     * @return A Page of ReservationDTO objects representing the paginated list of reservations.
     */
    public Page<ReservationDTO> getReservationPage(Pageable pageable) {
        Page<Reservation> reservationPage = reservationRepository.findAll(pageable);

        return getReservationDTOPage(reservationPage);
    }

    /**
     * Converts a Page of Reservation entities to a Page of ReservationDTOs using the provided mapper.
     *
     * @param reservationPage The Page of Reservation entities to be converted.
     * @return A Page of ReservationDTO objects representing the converted reservations.
     */
    public Page<ReservationDTO> getReservationDTOPage(Page<Reservation> reservationPage) {
        Page<ReservationDTO> reservationDTOPage = reservationPage.map(new Function<Reservation, ReservationDTO>() {
            @Override
            public ReservationDTO apply(Reservation reservation) {
                return reservationMapper.reservationToReservationDTO(reservation);
            }
        });
        return reservationDTOPage;

    }

    /**
     * Updates a reservation with the provided details.
     *
     * @param reservationId             The ID of the reservation to be updated.
     * @param car                       The car associated with the reservation.
     * @param reservationUpdateRequest  The request containing the updated reservation details.
     * @throws BadRequestException if the reservation status is CANCELED or DONE, or if the new status is CREATED and
     *                             the reservation time conflicts with existing reservations for the car.
     */
    public void updateReservation(Long reservationId, Car car, ReservationUpdateRequest reservationUpdateRequest) {
        Reservation reservation = getById(reservationId);

        if (reservation.getStatus().equals(ReservationStatus.CANCELED) || reservation.getStatus().equals(ReservationStatus.DONE)) {
            throw new BadRequestException(ErrorMessage.RESERVATION_STATUS_CANT_CHANGE_MESSAGE);
        }
        if (reservationUpdateRequest.getStatus() != null &&
                reservationUpdateRequest.getStatus() == ReservationStatus.CREATED) {
            checkReservationTimeIsCorrect(reservationUpdateRequest.getPickUpTime(), reservationUpdateRequest.getDropOffTime());
            List<Reservation> conflictReservations = getConflictReservations(car, reservationUpdateRequest.getPickUpTime(),
                    reservationUpdateRequest.getDropOffTime());
            if (!conflictReservations.isEmpty()) {
                if (!(conflictReservations.size() == 1 && conflictReservations.get(0).getId().equals(reservationId))) {
                    throw new BadRequestException(ErrorMessage.CAR_NOT_AVAILABLE_MESSAGE);
                }
            }
            Double totalPrice = getTotalPrice(car, reservationUpdateRequest.getPickUpTime(), reservationUpdateRequest.getDropOffTime());
            //bakılacak
            reservation.setTotalPrice(totalPrice);
            reservation.setCar(car);
        }
        reservation.setPickUpTime(reservationUpdateRequest.getPickUpTime());
        reservation.setDropOffTime(reservationUpdateRequest.getDropOffTime());
        reservation.setPickUpLocation(reservationUpdateRequest.getPickUpLocation());
        reservation.setDropOffLocation(reservationUpdateRequest.getDropOffLocation());
        reservation.setStatus(reservationUpdateRequest.getStatus());

        reservationRepository.save(reservation);

    }

    /**
     * Retrieves a reservation by its ID.
     *
     * @param id The ID of the reservation to be retrieved.
     * @return The reservation with the specified ID.
     * @throws ResourceNotFoundException if no reservation is found with the given ID.
     */
    public Reservation getById(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));

        return reservation;
    }

    /**
     * Retrieves a reservation DTO by its ID.
     *
     * @param id The ID of the reservation to be retrieved.
     * @return The reservation DTO with the specified ID.
     * @throws ResourceNotFoundException if no reservation is found with the given ID.
     */
    public ReservationDTO getReservationDTO(Long id) {
        Reservation reservation = getById(id);
        return reservationMapper.reservationToReservationDTO(reservation);
    }

    /**
     * Retrieves a page of reservation DTOs for a specific user.
     *
     * @param user     The user for whom reservations are retrieved.
     * @param pageable Information to determine the page to be retrieved.
     * @return A page of reservation DTOs for the specified user.
     */
    public Page<ReservationDTO> findReservationPageByUser(User user, Pageable pageable) {

        Page<Reservation> reservationPage = reservationRepository.findAllByUser(user, pageable);
        return getReservationDTOPage(reservationPage);
    }

    /**
     * Retrieves a reservation DTO by its ID and associated user.
     *
     * @param id   The ID of the reservation to be retrieved.
     * @param user The associated user for whom the reservation is retrieved.
     * @return A reservation DTO for the specified ID and user.
     * @throws ResourceNotFoundException if the reservation is not found.
     */
    public ReservationDTO findByIdAndUser(Long id, User user) {
        Reservation reservation = reservationRepository.findByIdAndUser(id, user).orElseThrow(() -> new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));

        return reservationMapper.reservationToReservationDTO(reservation);
    }

    /**
     * Removes a reservation by its ID.
     *
     * @param id The ID of the reservation to be removed.
     * @throws ResourceNotFoundException if the reservation with the specified ID is not found.
     */
    public void removeById(Long id) {
        boolean exists = reservationRepository.existsById(id);

        if (!exists){
            throw new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,id));
        }
        reservationRepository.deleteById(id);
    }

    /**
     * Checks if there are any reservations associated with the specified car.
     *
     * @param car The car for which reservation existence is checked.
     * @return true if reservations exist for the car, false otherwise.
     */
    public boolean existByCar(Car car) {
        return reservationRepository.existsByCar(car);
    }

    /**
     * Checks if there are any reservations associated with the specified user.
     *
     * @param user The user for which reservation existence is checked.
     * @return true if reservations exist for the user, false otherwise.
     */
    public boolean existByUser(User user) {
        return reservationRepository.existsByUser(user);
    }

    /**
     * Retrieves a list of all reservations.
     *
     * @return List of Reservation entities.
     */
    public List<Reservation> getAll() {
        return reservationRepository.findAllBy();
    }
}
