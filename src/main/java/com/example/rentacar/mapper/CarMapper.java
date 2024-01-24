package com.example.rentacar.mapper;

import com.example.rentacar.domain.Car;
import com.example.rentacar.domain.ImageFile;
import com.example.rentacar.dto.CarDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "image",ignore = true)
    Car carDTOToCar(CarDTO carDTO);


    List<CarDTO>map(List<Car> cars);

    @Mapping(source = "image",target = "image",qualifiedByName = "getImageAsString")
    CarDTO carToCarDTO(Car car);

    @Named("getImageAsString")
    public static Set<String> getImageIds(Set<ImageFile> imageFiles){
        Set<String> imgs=new HashSet<>();
        imgs=imageFiles.stream().map(imFile->imFile.getId().toString()).
                collect(Collectors.toSet());
        return imgs;
    }
}
