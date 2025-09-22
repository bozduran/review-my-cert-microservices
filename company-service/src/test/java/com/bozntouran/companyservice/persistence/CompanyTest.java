package com.bozntouran.companyservice.persistence;

import com.bozntouran.companyservice.config.TestSecurityConfig;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

/**
 * needed for working without problem
 */
@DataJpaTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        }
)
@Import(TestSecurityConfig.class)
class CompanyTest {

    /**
     * Used for a specific filter
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    TestEntityManager testEntityManager;


    @Test
    void happy_path_for_entity(){
        Company company = Company.builder()
                .name("some name")
                .yearOfFoundation(2)
                .publicId(UUID.randomUUID().toString())
                .build();

        Company storedCompany = testEntityManager.persistAndFlush(company);

        Assertions.assertTrue(storedCompany.getId() > 0);
        Assertions.assertEquals(  0,storedCompany.getVersion());

    }

    @Test
    void no_happy_path_for_name(){
        Company company = Company.builder()
                .yearOfFoundation(2)
                .publicId(UUID.randomUUID().toString())
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(company);
        }, "Was expecting a ConstraintViolationException to be thrown.");
    }

    @Test
    void no_happy_path_for_name_blank(){
        Company company = Company.builder()
                .name("              ")
                .yearOfFoundation(2)
                .publicId(UUID.randomUUID().toString())
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(company);
        }, "Was expecting a ConstraintViolationException to be thrown.");

    }

    @Test
    void no_happy_path_for_yearOfFoundation_blank(){
        Company company = Company.builder()
                .name("some name")
                .yearOfFoundation(-2)
                .publicId(UUID.randomUUID().toString())
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(company);
        }, "Was expecting a ConstraintViolationException to be thrown.");
    }

    @Test
    void no_happy_path_for_yearOfFoundation_blank_bigger_than_3000(){
        Company company = Company.builder()
                .name("some name")
                .yearOfFoundation(3001)
                .publicId(UUID.randomUUID().toString())
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(company);
        }, "Was expecting a ConstraintViolationException to be thrown.");
    }

    @Test
    void no_happy_path_for_publicId_(){
        Company company = Company.builder()
                .name("some name")
                .yearOfFoundation(300)
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, ()->{
            testEntityManager.persistAndFlush(company);
        }, "Was expecting a ConstraintViolationException to be thrown.");
    }
}