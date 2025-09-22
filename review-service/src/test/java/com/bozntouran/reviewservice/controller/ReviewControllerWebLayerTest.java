package com.bozntouran.reviewservice.controller;

import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.api.core.review.ReviewService;
import com.bozntouran.reviewservice.config.TestSecurityConfig;
import com.bozntouran.reviewservice.persistence.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(controllers = ReviewController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        })
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerWebLayerTest {
    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    Review review;
    ReviewDto reviewDto;

    List<ReviewDto> reviewDtoList;

    @BeforeEach
    void setUp(){
        review = review.builder()
                .certificateId(1L)
                .comment("comment ")
                .publicId(UUID.randomUUID().toString())
                .updateDate(LocalDateTime.now())
                .stars((short) 5)
                .build();

        reviewDto = ReviewDto.builder()
                .comment("asdasd")
                .publicId(UUID.randomUUID().toString())
                .stars((short) 5)
                .updateDate(LocalDateTime.now())
                .build();

        reviewDtoList = List.of(
                ReviewDto.builder()
                        .comment("Excellent service and friendly staff!")
                        .publicId(UUID.randomUUID().toString())
                        .stars((short) 5)
                        .updateDate(LocalDateTime.now())
                        .build(),

                ReviewDto.builder()
                        .comment("Good experience overall, but could be better")
                        .publicId(UUID.randomUUID().toString())
                        .stars((short) 4)
                        .updateDate(LocalDateTime.now().minusDays(1))
                        .build(),

                ReviewDto.builder()
                        .comment("Average quality, nothing special")
                        .publicId(UUID.randomUUID().toString())
                        .stars((short) 3)
                        .updateDate(LocalDateTime.now().minusDays(2))
                        .build(),

                ReviewDto.builder()
                        .comment("Needs improvement in many areas")
                        .publicId(UUID.randomUUID().toString())
                        .stars((short) 2)
                        .updateDate(LocalDateTime.now().minusDays(3))
                        .build(),

                ReviewDto.builder()
                        .comment("Terrible experience, would not recommend")
                        .publicId(UUID.randomUUID().toString())
                        .stars((short) 1)
                        .updateDate(LocalDateTime.now().minusDays(4))
                        .build()
        );
    }

    @Test
    void getReviewByPublicId() throws Exception {

        //Arrange
        Mockito.when(reviewService.retrieveReviewByPublicId(anyString())).thenReturn(Optional.of(reviewDto));
        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/review/"+reviewDto.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);
        //Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        //Assert
        Assertions.assertEquals(HttpStatus.OK.value() , mvcResult.getResponse().getStatus(),
                "200 HTTP status expected");

    }

    @Test
    void getAllReviews() throws Exception {
        //Arrange
        Page<ReviewDto> certificateDtoPage = new PageImpl<>(reviewDtoList, PageRequest.of(0, 10), reviewDtoList.size());

        Mockito.when(reviewService.retrieveAllReviews(null,null,null,null)).thenReturn(certificateDtoPage);
        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/review")
                        .accept(MediaType.APPLICATION_JSON);
        //Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String returnJson = mvcResult.getResponse().getContentAsString();

        //Assert
        Mockito.verify(reviewService).retrieveAllReviews(null, null, null, null);
        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus()
                ,"200 HTTP status expected");

        Assertions.assertEquals(
                Integer.valueOf(reviewDtoList.size()),
                JsonPath.read(returnJson,"$.content.length()")
                ,"Expected 5");

        for (int i = 0; i < reviewDtoList.size(); i++) {
            Assertions.assertEquals(
                    reviewDtoList.get(i).getPublicId(),
                    JsonPath.read(returnJson, "$.content[" + i + "].publicId"),
                    "Public ID mismatch at index " + i
            );
        }
    }

    @Test
    void postReview() throws Exception {
        //Arrange
        Mockito.when(reviewService.save(reviewDto)).thenReturn(Optional.ofNullable(reviewDto));
        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/review")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto));

        //Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andReturn();
        String returnJson = mvcResult.getResponse().getContentAsString();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult)).andReturn();

        //Assert
        Mockito.verify(reviewService).save(reviewDto);

        Assertions.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus() );
        String location = mvcResult.getResponse().getHeader("Location");
        Assertions.assertNotNull(location);
    }

    @Test
    void deleteReview() throws Exception {
        //Arrange
        Mockito.when(reviewService.remove(anyString())).thenReturn(true);
        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/review/"+reviewDto.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto));
        //Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //Assert
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus());
    }
}