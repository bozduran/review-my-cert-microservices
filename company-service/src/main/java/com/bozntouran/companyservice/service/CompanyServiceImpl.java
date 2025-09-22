package com.bozntouran.companyservice.service;


import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.api.core.company.CompanyService;
import com.bozntouran.companyservice.mapper.CompanyMapper;
import com.bozntouran.companyservice.persistence.Company;
import com.bozntouran.companyservice.persistence.CompanyRepository;
import com.bozntouran.utils.CustomPageRequests;
import com.bozntouran.utils.exception.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@ComponentScan("com.bozntouran")
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Page<CompanyDto> getCompanies(String name, Integer pageNumber) {

        PageRequest pageRequest = CustomPageRequests.pageRequestBuilder(pageNumber, 0,"name");

        if (name != null){
            return companyRepository.getAllByNameContainingIgnoreCase(name, pageRequest).map(CompanyMapper.MAPPER::fromCompany);
        }

        return companyRepository.findAll(pageRequest).map(CompanyMapper.MAPPER::fromCompany);
    }

    @Override
    public Optional<CompanyDto> getCompanyByPublicId(String publicId) {
        log.info("getCompanyById {}", publicId);
        chekIfUUID(publicId);
        return Optional.of(CompanyMapper.MAPPER.fromCompany(
                companyRepository.findByPublicId(publicId).orElseThrow(()-> new NotFoundException("Company with id " +publicId+ " doesnt exits"))));
    }

    @Override
    public CompanyDto saveCompany(CompanyDto companyDto) {
        Company company = companyRepository.save( CompanyMapper.MAPPER.toCompany(companyDto) );
        return CompanyMapper.MAPPER.fromCompany(company);
    }

    @Override
    public boolean deleteCompanyById(String publicId) {
        chekIfUUID(publicId);
        if (companyRepository.existsByPublicId(publicId)){
            return companyRepository.deleteByPublicId(publicId);
        }
        return false;
    }

    @Override
    public boolean updateCompany(CompanyDto updateCompanyDto) {
        Company updateCompany = CompanyMapper.MAPPER.toCompany(updateCompanyDto);
        companyRepository.findById(updateCompany.getId()).ifPresentOrElse(
                (company)->{
                    if(updateCompany.getDescription()!=null){
                        company.setDescription(updateCompany.getDescription());
                    }
                    if(updateCompany.getName()!=null){
                        company.setName(updateCompany.getName());
                    }
                    if(updateCompany.getYearOfFoundation()>0){
                        company.setYearOfFoundation(updateCompany.getYearOfFoundation());
                    }
                    companyRepository.save(company);
                },
                () -> {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"); }

        );
        return true;
    }

    // returns the internal id from the publicid parameter
    @Override
    public Long retrieveInternalIdByPublicId(String publicId) {

        return companyRepository.findByPublicId(publicId)
                .map(Company::getId)
                .orElse(null);
    }

    private void chekIfUUID(String publicId){
        try {
            UUID.fromString(publicId);
        } catch (Exception e) {
            throw new BadRequestException("Invalid UUID for company");
        }
    }


}
