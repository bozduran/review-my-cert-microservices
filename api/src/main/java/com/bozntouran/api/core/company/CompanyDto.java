package com.bozntouran.api.core.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyDto {

    @NotNull
    private String name;

    @Min(0)
    @Max(3000)
    private int yearOfFoundation;

    @NotNull
    private String publicId;

}
