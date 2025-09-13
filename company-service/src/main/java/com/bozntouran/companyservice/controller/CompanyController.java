package com.bozntouran.companyservice.controller;



import com.bozntouran.api.core.certificate.CertificateDto;
import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.api.core.company.CompanyService;
import com.bozntouran.utils.exception.BadRequestException;
import com.bozntouran.utils.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController()
@Log4j2
public class CompanyController {
    private static final String COMPANY_URL = "/company";
    private static final String COMPANY_ID_URL = "/company/{publicId}";

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(
            summary = "${api.company.get-companyId-byPublicId.description}",
            description = "${api.company.    get-companyId-byPublicId.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping("/internal" + COMPANY_ID_URL)
    public Long getCompanyIdByPublicId(@PathVariable String publicId){
        log.info("getCompanyIdByPublicId:{}",publicId);
        Long id = companyService.retrieveInternalIdByPublicId(publicId);
        if (id==null){
            throw new ResourceNotFoundException("Company with public id:" + publicId + "coundt be found");
        }
        return id;
    }

    @Operation(
            summary = "${api.company.get-company-byParameters.description}",
            description = "${api.company.get-company-byParameters.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping(COMPANY_URL)
    public ResponseEntity<Page<CompanyDto>> getAllCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber) {

        Page<CompanyDto> companyDtoPage = companyService.getCompanies(name, pageNumber);

        return new ResponseEntity<>(companyDtoPage,HttpStatus.OK);
    }

    @Operation(
            summary = "${api.company.get-company-byId.description}",
            description = "${api.company.get-company-byId.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping(COMPANY_ID_URL)
    public ResponseEntity<CompanyDto> getCompany(@PathVariable String publicId) {

        CompanyDto companyDto = companyService.getCompanyById(publicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Company with publicId " + publicId + " not found"));

        return new ResponseEntity<>(companyDto,HttpStatus.OK);
    }

    @Operation(
            summary = "${api.company.post-company.description}",
            description = "${api.company.post-company.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PostMapping(COMPANY_URL)
    public ResponseEntity<CompanyDto> postCompany(@RequestBody @Valid CompanyDto postCompanyDto){

        CompanyDto companyDto = companyService.saveNewCompany(postCompanyDto);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Location", COMPANY_URL + "/"
                + companyDto.getPublicId());

        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }


    @Operation(
            summary = "${api.company.delete-company.description}",
            description = "${api.company.delete-company.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @DeleteMapping(COMPANY_ID_URL)
    public ResponseEntity<CertificateDto> deleteCompany(@PathVariable String publicId) {
        boolean res = companyService.deleteCompanyById(publicId);
        if(!res){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "${api.company.update-company.description}",
            description = "${api.company.update-company.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PatchMapping(COMPANY_URL)
    public ResponseEntity<CompanyDto> updateCompany(@RequestBody CompanyDto updateCompanyDto){

        if(!companyService.updateCompany(updateCompanyDto)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }





}
