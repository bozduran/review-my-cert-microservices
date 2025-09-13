package com.bozntouran.api.core.company;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyDto {

    private String name;
    private int yearOfFoundation;
    private String publicId;

}
