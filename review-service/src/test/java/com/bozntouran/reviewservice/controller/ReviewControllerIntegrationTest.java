package com.bozntouran.reviewservice.controller;

import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.api.core.review.ReviewService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.utility.DockerImageName;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.cloud.config.enabled=false"}
)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    ReviewService reviewService;

    String authorizationToken;

    @Autowired
    TestRestTemplate restTemplate ;
    @MockitoBean
    JwtDecoder jwtDecoder;

    @Mock
    private Jwt jwt;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.9"));

    @Test
    @Order(1)
    void testContainerIsRunning() {
        assertTrue(postgres.isCreated(), "PostgreSQLContainer container has not been created");
        assertTrue(postgres.isRunning(), "PostgreSQLContainer container is not running");
    }

    @Test
    void get_auth_toke() throws JSONException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("writer", "secret-writer");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        // Act
        ResponseEntity response = restTemplate.postForEntity("http://localhost:9999/oauth2/token",
                request,
                JsonParser.class);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP Status code should be 200");
        Assertions.assertNotNull(authorizationToken,
                "Response should contain Authorization header with JWT");
        Assertions.assertNotNull(response.getHeaders().
                        getValuesAsList("UserID").get(0),
                "Response should contain UserID in a response header");
    }

    @Test
    void review_can_be_posted() throws JSONException {
        //Arrange
        JSONObject reviewRequestJson = new JSONObject();
        reviewRequestJson.put("stars","5");
        reviewRequestJson.put("publicId", UUID.randomUUID().toString());
        reviewRequestJson.put("certificateId","5");
        reviewRequestJson.put("userId","5");
        reviewRequestJson.put("comment","5");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity<String> request = new HttpEntity<>(reviewRequestJson.toString(), headers);

        // Act
        ResponseEntity<ReviewDto> createdUserDetailsEntity = restTemplate.postForEntity("http://localhost:" + port  +"/review",
                request,
                ReviewDto.class);
        System.out.println(createdUserDetailsEntity);

    }


}
