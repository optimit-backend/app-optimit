package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.entity.CurrentCource;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CurrencyDto;
import uz.pdp.springsecurity.payload.EditCourse;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.CurrencyRepository;
import uz.pdp.springsecurity.repository.CurrentCourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrencyService {
    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrentCourceRepository currentCourceRepository;

    @Autowired
    BusinessRepository businessRepository;

    public ApiResponse add(CurrencyDto currencyDto) {
        Optional<Business> optionalBusiness = businessRepository.findById(currencyDto.getBusinessId());
        if (optionalBusiness.isEmpty()) {
            return new ApiResponse("BUSINESS NOT FOUND", false);
        }
        if (currencyDto.getName().equalsIgnoreCase("SO'M")){
            return new ApiResponse("Can't add 'so'm' currency!");
        }
        CurrentCource currentCource = new CurrentCource();
        Currency currency = new Currency();
        currency.setName(currencyDto.getName());
        currency.setDescription(currencyDto.getDescription());
        if (currencyDto.isActive()){
            List<Currency> currencyList = currencyRepository.findAllByBusinessId(currencyDto.getBusinessId());
            for (Currency currency1 : currencyList) {
                currency1.setActive(false);
                currencyRepository.save(currency1);
            }
        }
        currency.setActive(true);
        currency.setBusiness(optionalBusiness.get());
        currency = currencyRepository.save(currency);

        currentCource.setCurrentCourse(currencyDto.getCurrentCourse());
        currentCource.setCurrency(currency);
        currentCourceRepository.save(currentCource);

        return new ApiResponse("ADDED", true, currency.getId());
    }

    public ApiResponse get(UUID id) {
        if (!currencyRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, currencyRepository.findById(id).get());
    }

    public ApiResponse delete(UUID id) {
        Optional<Currency> optionalCurrency = currencyRepository.findById(id);
        if (optionalCurrency.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }

        Currency currency = optionalCurrency.get();
        if (currency.getName().equalsIgnoreCase("SO'M")) {
            return new ApiResponse("CAN'T DELETE THIS CURRENCY", false);
        } else {
            currencyRepository.deleteById(id);
        }
        return new ApiResponse("DELETED", true);
    }

    public ApiResponse getAllCurrency(UUID businessId) {
        List<Currency> all = currencyRepository.findAllByBusinessId(businessId);
        if (all.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        List<CurrencyDto> dtoList = new ArrayList<>();
        for (Currency currency : all) {
            dtoList.add(generateCurrencyDtoFromCurrency(currency));
        }
        return new ApiResponse("All Currencies", true, dtoList);
    }

    public CurrencyDto generateCurrencyDtoFromCurrency(Currency currency) {
        CurrencyDto dto = new CurrencyDto();
        dto.setId(currency.getId());
        dto.setName(currency.getName());
        dto.setDescription(currency.getDescription());
        dto.setActive(currency.isActive());
        dto.setBusinessId(currency.getBusiness().getId());
        if (!currency.getName().equalsIgnoreCase("SO'M")) {

            dto.setCurrentCourse(currentCourceRepository.getByCurrencyId(currency.getId()).getCurrentCourse());
        }
        return dto;
    }

    public ApiResponse getOneCurrency(UUID id) {
        Optional<Currency> byId = currencyRepository.findById(id);

        if (byId.isPresent()) {
            CurrencyDto dto = generateCurrencyDtoFromCurrency(byId.get());
            return new ApiResponse("One Currency", true, dto);
        }
        return new ApiResponse("NOT FOUND", false);
    }

    public ApiResponse editCurrency(UUID id, CurrencyDto dto) {
        Optional<Currency> byId = currencyRepository.findById(id);
        if (byId.isPresent()) {
            Currency currency = byId.get();
            if (currency.getName().equalsIgnoreCase("SO'M")) {
                return new ApiResponse("Can't edit this currency");
            }
            currency.setName(dto.getName());
            currency.setDescription(dto.getDescription());
            CurrentCource currentCource = currentCourceRepository.getByCurrencyIdAndActive(currency.getId(), true);
            currentCource.setCurrentCourse(dto.getCurrentCourse());
            currentCourceRepository.save(currentCource);
            currency = currencyRepository.save(currency);
            CurrencyDto currencyDto = generateCurrencyDtoFromCurrency(currency);
            return new ApiResponse("Edited", true, currencyDto);
        }
        return new ApiResponse("NOT FOUND", false);
    }

    public Currency generateCurrencyFromCurrencyDto(CurrencyDto dto) {
        Currency currency = new Currency();
        currency.setName(dto.getName());
        currency.setDescription(dto.getDescription());
        return currency;
    }

    public ApiResponse editCourse(UUID id, double course) {
        Optional<Currency> currencyOptional = currencyRepository.findById(id);
        if (currencyOptional.isPresent()) {
            Currency currency = currencyOptional.get();
            if (currency.getName().equalsIgnoreCase("SO'M")) {
                return new ApiResponse("CAN'T EDITED THIS CURRENCY", false);
            }
            CurrentCource currentCource = currentCourceRepository.getByCurrencyId(id);
            currentCource.setCurrentCourse(course);
            currentCourceRepository.save(currentCource);
            return new ApiResponse("Edited", true);
        }
        return new ApiResponse("NOT FOUND", false);
    }

    @Transactional
    public ApiResponse editActiveCourse(EditCourse editCourse) {
        List<Currency> allByActiveTrueAndBusinessId = currencyRepository.findAllByBusinessIdAndActiveTrue(editCourse.getBusinessId());
        for (Currency currency : allByActiveTrueAndBusinessId) {
            currency.setActive(false);
            currencyRepository.save(currency);
        }
        Optional<Currency> optionalCurrency = currencyRepository.findById(editCourse.getId());
        if (optionalCurrency.isPresent()){
            Currency currency = optionalCurrency.get();
            currency.setActive(true);
            currencyRepository.save(currency);
            return new ApiResponse("EDITED",true);
        }else {
            return new ApiResponse("NOT FOUND", false);
        }
    }

    public double getValueByActiveCourse(double value, UUID businessId){
        Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(businessId);
        CurrentCource cource = currentCourceRepository.getByCurrencyIdAndActive(currency.getId(), true);
        if (!currency.getName().equalsIgnoreCase("SO'M")){
            return value / cource.getCurrentCourse();
        }
        return value;
    }
}
