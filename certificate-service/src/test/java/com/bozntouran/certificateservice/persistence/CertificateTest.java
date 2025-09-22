package com.bozntouran.certificateservice.persistence;

import com.bozntouran.api.core.certificate.CertificateField;
import com.bozntouran.certificateservice.config.TestSecurityConfig;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class CertificateTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    TestEntityManager testEntityManager;

    Certificate certificate;

    @BeforeEach
    void setUp(){
        this.certificate = Certificate.builder()
                .name("some name")
                .price(2)
                .field(CertificateField.PRODUCTION)
                .companyId(1L)
                .publicId(UUID.randomUUID().toString())
                .build();
    }

    @Test
    void happy_path_for_entity(){

        Certificate storedCertificate = testEntityManager.persistAndFlush(certificate);

        Assertions.assertEquals(certificate.getCompanyId() , storedCertificate.getCompanyId());
        Assertions.assertEquals(certificate.getField() , storedCertificate.getField());
        Assertions.assertEquals(certificate.getPrice() , storedCertificate.getPrice());

    }

    @Test
    void not_happy_path_for_public_id(){

        this.certificate.setPublicId(null);

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(certificate);
        }, "Was expecting a ConstraintViolationException to be thrown.");

    }

    @Test
    void not_happy_path_for_name(){

        this.certificate.setName(null);

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(certificate);
        }, "Was expecting a ConstraintViolationException to be thrown.");

    }

    @Test
    void not_happy_path_for_negative_price(){

        this.certificate.setPrice(-1);

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(certificate);
        }, "Was expecting a ConstraintViolationException to be thrown.");

    }

}