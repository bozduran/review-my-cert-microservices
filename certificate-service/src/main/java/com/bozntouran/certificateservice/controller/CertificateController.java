package com.bozntouran.certificateservice.controller;

import com.bozntouran.api.core.certificate.CertificateDto;
import com.bozntouran.api.core.certificate.CertificateField;
import com.bozntouran.api.core.certificate.CertificateService;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.bozntouran.utils.Res4jHelperMethod.someThingBadHappened;

@Log4j2
@RestController
public class CertificateController {

    private static final String CERTIFICATE_URL = "/certificate";
    private static final String CERTIFICATE_ID_URL = "/certificate/{id}";

    private final CertificateService certificateService;

    @Autowired
    public CertificateController(CertificateService certificateService){
        this.certificateService = certificateService;
    }

    @GetMapping("/internal"+CERTIFICATE_ID_URL)
    public Long getCertificateIdByPublicId(@PathVariable String id){
        log.info("getCertificateIdByPublicId with publicID: {}", id);
        return this.certificateService.retrieveInternalIdByPublicId(id);
    }

    @Operation(
            summary = "${api.certificate.get-certificate-byParameters.description}",
            description = "${api.certificate.get-certificate-byParameters.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping(value = CERTIFICATE_URL,produces = "application/json")
    public Page<CertificateDto> getCertificates(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Double startingRangePrice,
            @RequestParam(required = false) Double endRangePrice,
            @RequestParam(required = false) CertificateField certificateField,
            @RequestParam(required = false) Long companyId
    ){

        return this.certificateService.findAll(id,
                name,
                startingRangePrice,
                endRangePrice,
                certificateField,
                pageNumber,
                companyId);
    }


    @Operation(
            summary = "${api.certificate.get-certificate-byId.description}",
            description = "${api.certificate.get-certificate-byId.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping(value = CERTIFICATE_ID_URL,produces = "application/json")
    public CertificateDto getCertificate(@PathVariable Long id){
        return certificateService.getCertificate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

    }

    @Operation(
            summary = "${api.certificate.create-certificate.description}",
            description = "${api.certificate.create-certificate.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @Retry(name = "getRealCompanyID")
    @TimeLimiter(name = "getRealCompanyID")
    @CircuitBreaker(name = "getRealCompanyID",
            fallbackMethod = "postCertificateFallBack")
    @PostMapping( CERTIFICATE_URL )
    public CompletableFuture<ResponseEntity<CertificateDto>> postCertificate(@Validated @RequestBody CertificateDto certificateDto,
                                                                             @RequestParam(value = "delay", required = false,
                                                  defaultValue ="0") int delay,
                                                                             @RequestParam(value = "faultPercent", required = false,
                                                  defaultValue = "0") int faultPercent,
                                                                             Principal principal){

        return CompletableFuture.supplyAsync(() -> {
            // TODO REMOVE ONLY USED FOR DEV Resilience4j also parameter
            if (delay > 0 || faultPercent > 0) {
                someThingBadHappened(delay, faultPercent);
            }
            // when posting a new certificate the publicId is the public id of the COMPANY
            CertificateDto newCertificate = certificateService.saveNewCertificate(certificateDto);
            HttpHeaders httpHeaders = new HttpHeaders();
            // after when we send back the public id is the public id of CERTIFICATE
            httpHeaders.add("Location", CERTIFICATE_URL + "/"
                    + newCertificate.getPublicId());

            return new ResponseEntity<CertificateDto>(httpHeaders, HttpStatus.CREATED);
        });

    }

    public CompletableFuture<ResponseEntity<CertificateDto>> postCertificateFallBack(@Validated @RequestBody CertificateDto certificateDto,
                                                                             @RequestParam(value = "delay", required = false,
                                                                                     defaultValue ="0") int delay,
                                                                             @RequestParam(value = "faultPercent", required = false,
                                                                                     defaultValue = "0") int faultPercent,
                                                                             Principal principal,
                                                                             CallNotPermittedException exception){

        return CompletableFuture.supplyAsync(() -> {
            log.info("postCertificateFallBack {}", exception.getMessage());
            return new ResponseEntity<CertificateDto>(HttpStatus.CREATED);
        });

    }


    @Operation(
            summary = "${api.certificate.delete-certificate.description}",
            description = "${api.certificate.delete-certificate.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @DeleteMapping("/admin" + CERTIFICATE_ID_URL)
    public ResponseEntity deleteCertificate(@PathVariable Long id){

        if(!certificateService.deleteCertificateById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }



    @Operation(
            summary = "${api.certificate.update-certificate.description}",
            description = "${api.certificate.update-certificate.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @PatchMapping(CERTIFICATE_ID_URL)
    public ResponseEntity<CertificateDto> updateCertificate(@RequestBody CertificateDto updateCertificateDto){
        try {
            certificateService.updateCertificate(updateCertificateDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
