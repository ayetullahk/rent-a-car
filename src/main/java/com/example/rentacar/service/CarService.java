package com.example.rentacar.service;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.ImageFile;
import com.example.rentacar.dto.CarDTO;
import com.example.rentacar.exception.BadRequestException;
import com.example.rentacar.exception.ConflictException;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.mapper.CarMapper;
import com.example.rentacar.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ImageFileService imageFileService;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    ReservationService reservationService;

    /**
     * Saves a new car with the provided information and associates it with the specified image.
     *
     * @param imageId        The identifier of the image to be associated with the new car.
     * @param carDTO         The data transfer object containing information for creating a new car.
     * @throws ConflictException Thrown if the specified image is already associated with another car.
     */
    public void saveCar(String imageId, CarDTO carDTO) {

        ImageFile imageFile = imageFileService.findImageById(imageId);

        Integer usedCarCount = carRepository.findCarCountByImagId(imageFile.getId());

        if (usedCarCount > 0) {
            throw new ConflictException(ErrorMessage.IMAGE_USED_MESSAGE);
        }
        Car car = carMapper.carDTOToCar(carDTO);

        Set<ImageFile> imFiles = new HashSet<>();
        imFiles.add(imageFile);

        car.setImage(imFiles);

        carRepository.save(car);

    }

    /**
     * Retrieves a list of all cars.
     *
     * @return List of CarDTOs containing information about all cars.
     */
    public List<CarDTO> getAllCars() {

        List<Car> carList = carRepository.findAll();
        return carMapper.map(carList);

    }

    /**
     * Retrieves a paginated list of cars.
     *
     * @param pageable      The pageable object specifying the page, size, and sorting criteria.
     * @return              Page of CarDTOs containing information about the cars on the specified page.
     */
    public Page<CarDTO> findAllWithPage(Pageable pageable) {

        Page<Car> carPage = carRepository.findAll(pageable);
        Page<CarDTO> carPageDTO = carPage.map(new Function<Car, CarDTO>() {
            @Override
            public CarDTO apply(Car car) {
                return carMapper.carToCarDTO(car);
            }
        });
        return carPageDTO;

    }

    /**
     * Retrieves information about a car by its identifier.
     *
     * @param id   The identifier of the car to be retrieved.
     * @return     CarDTO containing information about the specified car.
     * @throws NotFoundException Thrown if the specified car is not found.
     */
    public CarDTO findById(Long id) {

        Car car = getCar(id);

        return carMapper.carToCarDTO(car);
    }

    /**
     * Retrieves a car by its identifier.
     *
     * @param id   The identifier of the car to be retrieved.
     * @return     Car entity representing the specified car.
     * @throws ResourceNotFoundException Thrown if the specified car is not found.
     */
    public Car getCar(Long id) {
        Car car = carRepository.findCarById(id).orElseThrow(() -> new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return car;
    }

    /**
     * Updates the information of a car with the provided data and associates it with a new image.
     *
     * @param id         The identifier of the car to be updated.
     * @param imageId    The identifier of the new image to be associated with the car.
     * @param carDTO     The data transfer object containing updated information for the car.
     * @throws BadRequestException   Thrown if the specified car is marked as built-in, and updates are not permitted.
     * @throws ConflictException     Thrown if the new image is already associated with another car.
     * @throws ResourceNotFoundException Thrown if the specified car or image is not found.
     */
    public void updateCar(Long id, String imageId, CarDTO carDTO) {
        Car car = getCar(id);

        if (car.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        ImageFile imageFile = imageFileService.findImageById(imageId);

        List<Car> carList = carRepository.findCarsByImageId(imageFile.getId());
        for (Car c : carList) {
            if (car.getId().longValue() != c.getId().longValue()) {
                throw new ConflictException(ErrorMessage.IMAGE_USED_MESSAGE);
            }
        }
        car.setAge(carDTO.getAge());
        car.setAirConditioning(carDTO.getAirConditioning());
        car.setBuiltin(carDTO.getBuiltin());
        car.setDoors(carDTO.getDoors());
        car.setFuelType(carDTO.getFuelType());
        car.setLuggage(carDTO.getLuggage());
        car.setModel(carDTO.getModel());
        car.setPricePerHour(carDTO.getPricePerHour());
        car.setSeats(carDTO.getSeats());
        car.setTransmission(carDTO.getTransmission());

        car.getImage().add(imageFile);

        carRepository.save(car);

    }

    /**
     * Deletes a car by its identifier.
     *
     * @param id   The identifier of the car to be deleted.
     * @throws BadRequestException           Thrown if the specified car is marked as built-in, and deletion is not permitted.
     * @throws BadRequestException           Thrown if the specified car is currently reserved, and deletion is not permitted.
     * @throws ResourceNotFoundException     Thrown if the specified car is not found.
     */
    public void removeById(Long id) {
        Car car = getCar(id);

        if (car.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean exist=reservationService.existByCar(car);

        if (exist){
            throw new BadRequestException(ErrorMessage.CAR_USED_BY_RESERVATION_MESSAGE);
        }

        carRepository.delete(car);
    }

    /**
     * Retrieves a car by its identifier.
     *
     * @param id   The identifier of the car to be retrieved.
     * @return     Car entity representing the specified car.
     * @throws ResourceNotFoundException Thrown if the specified car is not found.
     */
    public Car getCarById(Long id){
        Car car=carRepository.findById(id).orElseThrow(()->new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,id)));
        return car;
    }

    /**
     * Retrieves a list of all cars.
     *
     * @return List of Car entities representing all cars.
     */
    public List<Car> getAllCar() {
        return carRepository.getAllBy();
    }
}

