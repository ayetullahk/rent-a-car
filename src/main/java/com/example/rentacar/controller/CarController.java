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

    @PostMapping("/admin/{imageId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> saveCar(@PathVariable String imageId, @Valid @RequestBody CarDTO carDTO) {
        carService.saveCar(imageId, carDTO);

        VRResponse response = new VRResponse(ResponseMessage.CAR_SAVE_RESPONSE_MESSAGE, true);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @GetMapping("/visitors/all")
    public ResponseEntity<List<CarDTO>> getAllCars() {

        List<CarDTO> allCars = carService.getAllCars();

        return ResponseEntity.ok(allCars);
    }

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
    @GetMapping("/visitors/{id}")
    public ResponseEntity<CarDTO>getCarById(@PathVariable Long id){
        CarDTO carDTO=carService.findById(id);

        return ResponseEntity.ok(carDTO);
    }
    @PutMapping("/admin/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> updateCar(@RequestParam("id")Long id,
                                                @RequestParam("imageId")String imageId,
                                                @Valid@RequestBody CarDTO carDTO){
        carService.updateCar(id,imageId,carDTO);

        VRResponse response=new VRResponse(ResponseMessage.CAR_UPDATE_RESPONSE_MESSAGE,true);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/admin/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> deleteCar(@PathVariable Long id){
        carService.removeById(id);

        VRResponse response=new VRResponse(ResponseMessage.CAR_DELETE_RESPONSE_MESSAGE,true);
        return ResponseEntity.ok(response);
    }


}
