package uz.pdp.springsecurity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.pdp.springsecurity.entity.ExchangeProduct;
import uz.pdp.springsecurity.payload.ExchangeProductDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExchangeProductMapper {
    @Mapping(target = "productTypePriceId", ignore = true)
    @Mapping(target = "productExchangeId", source = "product.id")
    ExchangeProductDTO toDto(ExchangeProduct exchangeProduct);

    List<ExchangeProductDTO> toDtoList(List<ExchangeProduct> exchangeProducts);

    @Mapping(target = "productTypePrice", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "product.id", source = "productExchangeId")
    ExchangeProduct toEntity(ExchangeProductDTO exchangeProductDTO);

}
