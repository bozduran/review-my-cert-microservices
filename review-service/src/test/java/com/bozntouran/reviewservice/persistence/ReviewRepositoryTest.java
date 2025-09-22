package com.bozntouran.reviewservice.persistence;

import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.api.core.review.ReviewService;
import com.bozntouran.reviewservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {"spring.cloud.config.enabled=false"}
)
@Testcontainers
@Import(TestSecurityConfig.class)
class ReviewRepositoryTest {

    @Autowired
    ReviewService reviewService;

    @Autowired
    RestTemplate restTemplate;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.9"));

    @Test
    void testContainerIsRunning() {
        assertTrue(postgres.isCreated(), "PostgreSQLContainer container has not been created");
        assertTrue(postgres.isRunning(), "PostgreSQLContainer container is not running");
    }


    @Test
    void getByPublicIdIs() {
    }

    @Test
    void getAllByCertificateId() {
    }

    @Test
    void getAllByUserId() {
    }

    @Test
    void deleteByPublicId() {
    }
}