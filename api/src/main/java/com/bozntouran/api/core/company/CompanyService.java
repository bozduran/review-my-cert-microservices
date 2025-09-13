package com.bozntouran.api.core.company;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@SecurityRequirement(name = "security_auth")
public interface CompanyService {
    Page<CompanyDto> getCompanies(String name, Integer pageNumber);
    Optional<CompanyDto> getCompanyById(String publicId);

    CompanyDto saveNewCompany(CompanyDto companyDto);


    boolean deleteCompanyById(String id);

    boolean updateCompany(CompanyDto updateCompanyDto);

    Long retrieveInternalIdByPublicId(String publicId);
}
