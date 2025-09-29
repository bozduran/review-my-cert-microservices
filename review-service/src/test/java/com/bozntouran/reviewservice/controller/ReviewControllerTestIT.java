package com.bozntouran.reviewservice.controller;

import com.bozntouran.api.core.review.ReviewService;
import com.bozntouran.reviewservice.config.SecurityConfig;
import com.bozntouran.reviewservice.config.SpringSecurityFilterChainForTest;
import com.bozntouran.reviewservice.persistence.Review;
import com.bozntouran.reviewservice.persistence.ReviewRepository;
import com.bozntouran.reviewservice.service.ReviewServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false"
})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({SpringSecurityFilterChainForTest.class})
class ReviewControllerTestIT {

    @Autowired
    ReviewController reviewController;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ReviewService reviewService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockitoBean
    RestTemplate restTemplate;

    MockMvc mockMvc;

    @Container
    @ServiceConnection
    private static PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.9"));

    @Test
    @Order(1)
    void testContainerIsRunning() {
        assertTrue(postgres.isCreated(), "PostgreSQLContainer container has not been created");
        assertTrue(postgres.isRunning(), "PostgreSQLContainer container is not running");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());
    }

    @Test
    void testNoAuth() throws Exception {
/*        var responseEntity = ResponseEntity.of(Optional.of(1L));

        System.out.println(responseEntity.getBody());

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);*/

        Review newReview = Review.builder()
                .certificateId(1L)
                .publicId("16cb2bb8-f06b-4777-9fd4-db463b788550")
                .comment("this is a comment")
                .stars((short) 4)
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isUnauthorized());

    }


    @Test
    void postReviewFallBack() {
    }

    @Test
    void deleteReview() {
    }
}