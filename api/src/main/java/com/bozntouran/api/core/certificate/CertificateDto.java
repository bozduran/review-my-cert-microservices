package com.bozntouran.api.core.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CertificateDto {

    private String name;
    private double price;
    private Date updateDate;
    private CertificateField field;
    private String description;
    private String publicId;

}
