package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.Business;
import uz.pdp.springsecurity.entity.Currency;
import uz.pdp.springsecurity.mapper.CurrencyMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.CurrencyDto;
import uz.pdp.springsecurity.repository.BusinessRepository;
import uz.pdp.springsecurity.repository.CurrencyRepository;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    private final BusinessRepository businessRepository;
    private final CurrencyMapper currencyMapper;

    public ApiResponse get(UUID businessId) {
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(businessId);
        if (optionalCurrency.isPresent()){
            return new ApiResponse("SUCCESS", true, currencyMapper.toDto(optionalCurrency.get()));
        }
        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        if (optionalBusiness.isEmpty())
            return new ApiResponse("BUSINESS NOT FOUND", false);
        create(optionalBusiness.get());
        return get(businessId);
    }

    public ApiResponse edit(UUID businessId, CurrencyDto currencyDto) {
        Optional<Currency> optionalCurrency = currencyRepository.findByBusinessId(businessId);
        if (optionalCurrency.isPresent()){
            Currency currency = optionalCurrency.get();
            currency.setCourse(currencyDto.getCourse());
            currencyRepository.save(currency);
            return new ApiResponse("SUCCESS", true);
        }
        Optional<Business> optionalBusiness = businessRepository.findById(businessId);
        if (optionalBusiness.isEmpty())
            return new ApiResponse("BUSINESS NOT FOUND", false);
        create(optionalBusiness.get());
        return edit(businessId, currencyDto);
    }

    private void create(Business business) {
        Optional<Currency> optionalCurrency = currencyRepository.findFirstByCourseIsNotNullOrderByUpdateAtDesc();
        if (optionalCurrency.isPresent()){
            currencyRepository.save(new Currency(
                    business,
                    optionalCurrency.get().getCourse()
            ));
        }else {
            currencyRepository.save(new Currency(
                    business,
                    11400
            ));
        }
    }

    /*public ApiResponse add(CurrencyDto currencyDto) {
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
    }*/

    /*public ApiResponse get(UUID id) {
        if (!currencyRepository.existsById(id)) return new ApiResponse("NOT FOUND", false);
        return new ApiResponse("FOUND", true, currencyRepository.findById(id).get());
    }*/

    /*public ApiResponse delete(UUID id) {
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
    }*/

    /*public ApiResponse getAllCurrency(UUID businessId) {
        List<Currency> all = currencyRepository.findAllByBusinessId(businessId);
        if (all.isEmpty()) {
            return new ApiResponse("NOT FOUND", false);
        }
        List<CurrencyDto> dtoList = new ArrayList<>();
        for (Currency currency : all) {
            dtoList.add(generateCurrencyDtoFromCurrency(currency));
        }
        return new ApiResponse("All Currencies", true, dtoList);
    }*/

    /*public ApiResponse getOneCurrency(UUID id) {
        Optional<Currency> byId = currencyRepository.findById(id);

        if (byId.isPresent()) {
            CurrencyDto dto = generateCurrencyDtoFromCurrency(byId.get());
            return new ApiResponse("One Currency", true, dto);
        }
        return new ApiResponse("NOT FOUND", false);
    }*/

    /*public ApiResponse editCurrency(UUID id, CurrencyDto dto) {
        Optional<Currency> byId = currencyRepository.findById(id);
        if (byId.isPresent()) {
            Currency currency = byId.get();
            if (currency.getName().equalsIgnoreCase("SO'M")) {
                return new ApiResponse("Can't paySalary this currency");
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
    }*/

    /*public Currency generateCurrencyFromCurrencyDto(CurrencyDto dto) {
        Currency currency = new Currency();
        currency.setName(dto.getName());
        currency.setDescription(dto.getDescription());
        return currency;
    }*/

    /*public ApiResponse editCourse(UUID id, double course) {
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
    }*/

    /*@Transactional
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
    }*/

    /*public double getValueByActiveCourse(double value, UUID businessId){
        Currency currency = currencyRepository.findByBusinessIdAndActiveTrue(businessId);
        CurrentCource cource = currentCourceRepository.getByCurrencyIdAndActive(currency.getId(), true);
        if (!currency.getName().equalsIgnoreCase("SO'M")){
            return value / cource.getCurrentCourse();
        }
        return value;
    }*/
}
