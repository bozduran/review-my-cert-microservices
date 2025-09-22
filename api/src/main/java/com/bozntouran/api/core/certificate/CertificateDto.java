package com.bozntouran.api.core.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class CertificateDto {

    @NotNull
    @NotBlank
    private String name;
    @Positive
    private double price;

    private Date updateDate;
    private CertificateField field;
    private String description;
    private String publicId;

}
