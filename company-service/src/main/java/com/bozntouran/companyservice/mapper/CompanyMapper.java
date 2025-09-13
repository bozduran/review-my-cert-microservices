package com.bozntouran.companyservice.mapper;



import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.companyservice.persistence.Company;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper MAPPER = Mappers.getMapper(CompanyMapper.class);

    Company toCompany(CompanyDto companyDto);
    CompanyDto fromCompany(Company company);
}
