package com.bozntouran.reviewservice.persistence;

import com.bozntouran.api.core.review.ReviewService;
import com.bozntouran.reviewservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

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