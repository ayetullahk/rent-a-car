package com.example.rentacar.controller;

import com.example.rentacar.dto.CarDTO;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.service.CarService;
import com.example.rentacar.service.ImageFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/car")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private ImageFileService imageFileService;

    /**
     * Adds a new car with the specified image identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param imageId              The identifier of the image associated with the car.
     * @param carDTO               The request body containing the details of the car. Should be valid (@Valid).
     * @return                     ResponseEntity containing a VRResponse with information about the car addition process.
     *                             The response includes a message and success status.
     *                             The HTTP status in the response is HttpStatus.CREATED.
     * @throws NotFoundException   Thrown if the specified image is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
    @PostMapping("/admin/{imageId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> saveCar(@PathVariable String imageId, @Valid @RequestBody CarDTO carDTO) {
        carService.saveCar(imageId, carDTO);

        VRResponse response = new VRResponse(ResponseMessage.CAR_SAVE_RESPONSE_MESSAGE, true);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    /**
     * Retrieves a list of all cars for visitors (non-authenticated users).
     *
     * @return                  ResponseEntity containing a list of CarDTOs with information about all cars.
     *                          The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if no cars are found.
     */
    @GetMapping("/visitors/all")
    public ResponseEntity<List<CarDTO>> getAllCars() {

        List<CarDTO> allCars = carService.getAllCars();

        return ResponseEntity.ok(allCars);
    }

    /**
     * Retrieves a paginated list of all cars for visitors (non-authenticated users).
     *
     * @param page                The page number to retrieve (0-indexed).
     * @param size                The number of cars per page.
     * @param prop                The property by which to sort the results.
     * @param direction           The sorting direction, either ASC (ascending) or DESC (descending). Default is DESC.
     * @return                    ResponseEntity containing a Page of CarDTOs with information about all cars.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException  Thrown if no cars are found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
    @GetMapping("/visitors/pages")
    public ResponseEntity<Page<CarDTO>> getAllCarsWithPage(@RequestParam("page") int page,
                                                           @RequestParam("size") int size,
                                                           @RequestParam("sort") String prop,
                                                           @RequestParam(value = "direction", required = false,
                                                                   defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));

        Page<CarDTO> pageDTO=carService.findAllWithPage(pageable);

        return ResponseEntity.ok(pageDTO);
    }
    /**
     * Retrieves the details of a specific car for visitors (non-authenticated users) by its identifier.
     *
     * @param id                The identifier of the car to retrieve.
     * @return                  ResponseEntity containing a CarDTO with information about the specified car.
     *                          The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if the specified car is not found.
     */
    @GetMapping("/visitors/{id}")
    public ResponseEntity<CarDTO>getCarById(@PathVariable Long id){
        CarDTO carDTO=carService.findById(id);

        return ResponseEntity.ok(carDTO);
    }

    /**
     * Updates the details of an existing car.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                  The identifier of the car to be updated.
     * @param imageId             The identifier of the image associated with the car.
     * @param carDTO              The request body containing the updated details of the car. Should be valid (@Valid).
     * @return                    ResponseEntity containing a VRResponse with information about the car update process.
     *                            The response includes a message and success status.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if the specified car is not found.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
    @PutMapping("/admin/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> updateCar(@RequestParam("id")Long id,
                                                @RequestParam("imageId")String imageId,
                                                @Valid@RequestBody CarDTO carDTO){
        carService.updateCar(id,imageId,carDTO);

        VRResponse response=new VRResponse(ResponseMessage.CAR_UPDATE_RESPONSE_MESSAGE,true);

        return ResponseEntity.ok(response);
    }
    /**
     * Deletes a car with the specified identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                The identifier of the car to be deleted.
     * @return                  ResponseEntity containing a VRResponse with information about the car deletion process.
     *                          The response includes a message and success status.
     *                          The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if the specified car is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to delete the car.
     */
    @DeleteMapping("/admin/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> deleteCar(@PathVariable Long id){
        carService.removeById(id);

        VRResponse response=new VRResponse(ResponseMessage.CAR_DELETE_RESPONSE_MESSAGE,true);
        return ResponseEntity.ok(response);
    }


}
