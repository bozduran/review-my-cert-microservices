package com.bozntouran.reviewservice.controller;

import com.bozntouran.reviewservice.config.SpringSecurityFilterChainForTest;
import com.bozntouran.reviewservice.persistence.Review;
import com.bozntouran.reviewservice.persistence.ReviewRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.Instant;
import java.util.List;


import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ImportAutoConfiguration(exclude = { SpringSecurityFilterChainForTest.class})
class ReviewControllerTestIT {



    @Autowired
    ReviewController reviewController;

    @Autowired
    ReviewRepository reviewRepository;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.9"))
            .withDatabaseName("myappdb")
            .withUsername("myappadmin")
            .withPassword("myappadmin");

    @Container
    private static WireMockContainer wireMockContainer =
            new WireMockContainer(            DockerImageName.parse("wiremock/wiremock:2.35.0") )
                    .withExposedPorts(8080);
            //.withMapping("certificate",  "mappings/review-get-certificate-id.json");

    @BeforeAll
    void setup() {
        String baseUrl = "http://" + wireMockContainer.getHost() +
                ":" + wireMockContainer.getMappedPort(8080);
        System.out.println("baseUrl: " + baseUrl);
        wireMockContainer.withMappingFromJSON("{\n" +
                "  \"request\": {\n" +
                "    \"method\": \"GET\",\n" +
                "    \"url\": \"/internal/certificate/16cb2bb8-f06b-4777-9fd4-db463b788550\"\n" +
                "  },\n" +
                "  \"response\": {\n" +
                "    \"status\": 200,\n" +
                "    \"headers\": {\n" +
                "      \"Cache-Control\": \"no-cache, no-store, max-age=0, must-revalidate\",\n" +
                "      \"Connection\": \"keep-alive\",\n" +
                "      \"Content-Type\": \"application/json\",\n" +
                "      \"Expires\": \"0\",\n" +
                "      \"Keep-Alive\": \"timeout=60\",\n" +
                "      \"Pragma\": \"no-cache\",\n" +
                "      \"Transfer-Encoding\": \"chunked\",\n" +
                "      \"X-Content-Type-Options\": \"nosniff\",\n" +
                "      \"X-Frame-Options\": \"DENY\",\n" +
                "      \"X-XSS-Protection\": \"0\"\n" +
                "    },\n" +
                "    \"jsonBody\": 1123213\n" +
                "  }\n" +
                "}\n");
        wireMockContainer.start();
    }

    @AfterAll
    void tearDown() {
        wireMockContainer.stop();
    }
    @Test
    @Order(1)
    void testContainerIsRunning() {
        assertTrue(postgres.isCreated(), "PostgreSQLContainer container has not been created");
        assertTrue(postgres.isRunning(), "PostgreSQLContainer container is not running");
        assertTrue(wireMockContainer.isCreated(), "WireMockContainer container has not been created");
        assertTrue(wireMockContainer.isRunning(), "WireMockContainer container is not running");
    }

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessorUser =
            jwt().jwt(jwt -> {
                jwt.claims(claims -> {
                            claims.put("scope", "product:read product:write");
                            claims.put("roles", List.of("ROLE_USER"));
                        })
                        .subject("test-user")
                        .notBefore(Instant.now().minusSeconds(5L));
            }).authorities(new SimpleGrantedAuthority("ROLE_USER"));

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessorAdmin =
            jwt().jwt(jwt -> {
                jwt.claims(claims -> {
                            claims.put("scope", "product:read product:write");
                            claims.put("roles", List.of("ROLE_ADMIN"));
                        })
                        .subject("test-user")
                        .notBefore(Instant.now().minusSeconds(5L));
            }).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getReviewById() throws Exception {
        Review review = reviewRepository.findAll().get(0);

        mockMvc.perform(get("/review/" + review.getPublicId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.publicId",is(review.getPublicId()) ) )
        ;

    }

    @Test
    void getAllReviews() throws Exception {
        List<Review> reviews = reviewRepository.findAll();

        var result = mockMvc.perform(get("/review" )
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content.size()",is(reviews.size()) ) )
        ;

        for( int i =0;i<reviews.size();i++){
            result.andExpect(jsonPath("$.content[" + i + "].publicId",is(reviews.get(i).getPublicId()) ));
        }
    }

    @Test
    @Rollback
    @Transactional
    void postReview() throws Exception {
        Review newReview = Review.builder()
                .certificateId(1L)
                .publicId("16cb2bb8-f06b-4777-9fd4-db463b788550")
                .comment("this is a comment")
                .stars((short) 4)
                .build();


        MvcResult mvcResult = mockMvc.perform(post("/review")
                        .with(jwtRequestPostProcessorUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());

        // because we use async if the jwt isnt with the right scope and authority
        // we will get error async not started till it dont get to the controller
        // but if the scope and auth is right we ne a async type so the post will go through
    }

    @Test
    @Rollback
    @Transactional
    void postReview_Admin_is_forbidden() throws Exception {
        Review newReview = Review.builder()
                .certificateId(1L)
                .publicId("16cb2bb8-f06b-4777-9fd4-db463b788550")
                .comment("this is a comment")
                .stars((short) 4)
                .build();


        mockMvc.perform(post("/review")
                        .with(jwtRequestPostProcessorAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(status().isForbidden());


        // because we use async if the jwt isnt with the right scope and authority
        // we will get error async not started till it dont get to the controller
        // but if the scope and auth is right we ne a async type so the post will go through
    }


    @Test
    void testNoAuth() throws Exception {
        Review newReview = Review.builder()
                .certificateId(1L)
                .publicId("16cb2bb8-f06b-4777-9fd4-db463b788550")
                .comment("this is a comment")
                .stars((short) 4)
                .build();

        mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());


    }


    @Test
    void postReviewFallBack() {
    }

    @Test
    void deleteReview() {
    }
}