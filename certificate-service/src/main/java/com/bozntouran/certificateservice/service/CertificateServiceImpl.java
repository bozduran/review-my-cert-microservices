package com.bozntouran.certificateservice.service;


import com.bozntouran.api.core.certificate.CertificateDto;
import com.bozntouran.api.core.certificate.CertificateField;
import com.bozntouran.api.core.certificate.CertificateService;
import com.bozntouran.certificateservice.persistence.Certificate;
import com.bozntouran.certificateservice.persistence.CertificateRepository;
import com.bozntouran.utils.CustomPageRequests;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static com.bozntouran.certificateservice.mapper.CertificateMapper.MAPPER;


@Service
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 9;


    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;
    private final String COMPANY_URL;

    @Autowired
    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  RestTemplate restTemplate){
        this.certificateRepository = certificateRepository;
        this.restTemplate = restTemplate;
        this.COMPANY_URL = "http://company/internal/company/";

    }

    @Override
    public Optional<CertificateDto> getCertificate(long id) {
        return Optional.of(
                MAPPER.fromCertificate(
                        certificateRepository.findById(id).orElseThrow()));
    }

    @Override
    public CertificateDto saveNewCertificate(CertificateDto certificateDto) {
        Long companyId = getCompanyId(certificateDto.getPublicId())
                .orElseThrow(() -> new InternalError("No company found" ));
        certificateDto.setPublicId(UUID.randomUUID().toString());
        Certificate certificate = MAPPER.toCertificate(certificateDto);
        certificate.setCompanyId(companyId);
        log.info("saveNewCer: {} {} {}",companyId,certificate.getCompanyId(), certificate.getId());
        return MAPPER.fromCertificate(certificateRepository.save(certificate));
    }

    private Optional<Long> getCompanyId(String publicId){
        log.info("getCompanyId call to {}",COMPANY_URL+publicId);
        return restTemplate.exchange(COMPANY_URL+publicId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Optional<Long>>() {
                }).getBody();
    }


    @Override
    public boolean deleteCertificateById(Long id) {
        if(certificateRepository.existsById(id) ){
            certificateRepository.deleteById(id);
            return true;
        }

        return false;
    }

    @Override
    public Page<CertificateDto> findAll(Long id,
                                 String name,
                                 Double startingRangePrice,
                                 Double endRangePrice,
                                 CertificateField certificateField,
                                 Integer pageNumber,
                                 Long companyId) {
        PageRequest pageRequest = CustomPageRequests.pageRequestBuilder(pageNumber, 0,"updateDate");
        if(id != null){
            return certificateRepository.findAllById(id, pageRequest).map((MAPPER::fromCertificate));
        }else if (companyId != null){
            return certificateRepository.findAllByCompanyId(companyId, pageRequest).map((MAPPER::fromCertificate));
        } else if (startingRangePrice != null || endRangePrice != null ) {
            double startingRage = 0,endingRage =0;
            if (startingRangePrice != null) {
                startingRage = startingRangePrice;
            }
            if ( endRangePrice != null){
                endingRage = endRangePrice;
            }

            return certificateRepository.findAllByPriceBetween(startingRage, endingRage, pageRequest).map((MAPPER::fromCertificate));
        } else if ( name!=null) {
            return certificateRepository.findByNameContainingIgnoreCase(name, pageRequest).map((MAPPER::fromCertificate));
        }else if (certificateField != null){
            return certificateRepository.findByField(certificateField, pageRequest).map((MAPPER::fromCertificate));
        }

        return certificateRepository.findAll(pageRequest).map((MAPPER::fromCertificate));
    }

    @Override
    public Long retrieveInternalIdByPublicId(String publicId) {
        return this.certificateRepository.findByPublicId(publicId)
                .map(Certificate::getId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Certificate with publicId " + publicId + " not found"
                ));
    }

    @Override
    public boolean updateCertificate(CertificateDto updateCertificateDto) {
        Certificate updateCertificate = MAPPER.toCertificate(updateCertificateDto);
        certificateRepository.findById(updateCertificate.getId()).ifPresentOrElse(
                certificate -> {
                    if (updateCertificate.getCompanyId() != null) {
                        certificate.setCompanyId(updateCertificate.getCompanyId());
                    }
                    if (updateCertificate.getDescription() != null){
                        certificate.setDescription(updateCertificate.getDescription());
                    }
                    if(updateCertificate.getField() !=null){
                        certificate.setField(updateCertificate.getField());
                    }
                    if(updateCertificate.getName()!=null){
                        certificate.setName(updateCertificate.getName());
                    }
                    if(updateCertificate.getPrice()  >=0){
                        certificate.setPrice(updateCertificate.getPrice());
                    }
                    if(updateCertificate.getVersion() >= 0){
                        certificate.setVersion(updateCertificate.getVersion());
                    }
                    certificateRepository.save(certificate);
                },
                () -> {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"); }
        );

        return true;
    }


}
