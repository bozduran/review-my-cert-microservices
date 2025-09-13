package com.bozntouran.api.core.certificate;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@SecurityRequirement(name = "security_auth")
public interface CertificateService {

    Optional<CertificateDto> getCertificate(long id);

    CertificateDto saveNewCertificate(CertificateDto certificateDto);

    boolean deleteCertificateById(Long id);

    boolean updateCertificate(CertificateDto updateCertificate);
    Page<CertificateDto> findAll(Long id, String name, Double startingRangePrice, Double endRangePrice, CertificateField certificateField, Integer pageNumber, Long companyId);

    Long retrieveInternalIdByPublicId(String publicId);

/*
    @GetMapping("/certificate/{id}")
    Optional<CertificateDto> getCertificateById(@PathVariable long id);

    @PostMapping("/admin/certificate")
    CertificateDto saveNewCertificate(@RequestParam CertificateDto certificate);

    @DeleteMapping("/admin/certificate/{id}")
    boolean deleteCertificateById(@PathVariable Long id);

    @PostMapping()
    void updateCertificate(CertificateDto updateCertificate);

    @GetMapping("/certificate")
    Page<CertificateDto> findAll(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false)String name,
            @RequestParam(required = false)Double startingRangePrice,
            @RequestParam(required = false)Double endRangePrice,
            @RequestParam(required = false)CertificateField certificateField,
            @RequestParam(required = false)Long companyId,
            @RequestParam(required = false)Integer pageNumber);*/
}
