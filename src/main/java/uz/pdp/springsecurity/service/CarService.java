package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Car;
import uz.pdp.springsecurity.mapper.CarMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CarDto;
import uz.pdp.springsecurity.repository.CarRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final CarMapper mapper;

    public ApiResponse add(CarDto carDto) {
        repository.save(mapper.toEntity(carDto));
        return new ApiResponse("saved", true);
    }

    public ApiResponse edit(UUID id, CarDto carDto) {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        Car car = optionalCar.get();
        mapper.update(carDto, car);
        repository.save(car);
        return new ApiResponse("successfully edited", true);
    }

    public ApiResponse get(UUID businessId) {
        List<CarDto> carDtoList = mapper.toDto(repository.findAllByBusinessId(businessId));
        return new ApiResponse("all", true, carDtoList);
    }

    public ApiResponse getById(UUID id) {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isEmpty()) {
            return new ApiResponse("not found", false);
        }
        Car car = optionalCar.get();
        return new ApiResponse("found", true, mapper.toDto(car));
    }
}
