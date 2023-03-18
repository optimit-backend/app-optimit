package uz.pdp.springsecurity.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    @NotNull(message = "required line")
    private String name;
    private UUID businessId;
    private String description;
    private UUID parentCategory;
    private String parentCategoryName;

    public CategoryDto(String name, UUID businessId, String description) {
        this.name = name;
        this.businessId = businessId;
        this.description = description;
    }
}
