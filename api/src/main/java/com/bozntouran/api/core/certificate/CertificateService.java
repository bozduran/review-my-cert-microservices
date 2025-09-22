package com.bozntouran.api.core.certificate;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@SecurityRequirement(name = "security_auth")
public interface CertificateService {


    CertificateDto saveCertificate(CertificateDto certificateDto);

    boolean deleteCertificateByPublicId(String publicId);

    boolean updateCertificate(CertificateDto updateCertificate);
    Page<CertificateDto> getCertificates(Long id, String name, Double startingRangePrice, Double endRangePrice, CertificateField certificateField, Integer pageNumber, Long companyId);

    Long retrieveInternalIdByPublicId(String publicId);

    Optional<CertificateDto> retrieveCertificateByPublicId(String publicId);

}
