package com.bozntouran.certificateservice.mapper;


import com.bozntouran.api.core.certificate.CertificateDto;
import com.bozntouran.certificateservice.persistence.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    CertificateMapper MAPPER = Mappers.getMapper(CertificateMapper.class);



    Certificate toCertificate(CertificateDto certificateDto);

    @Mapping(source = "price" , target = "price", numberFormat = "$#.00")
    CertificateDto fromCertificate(Certificate certificate);
}
