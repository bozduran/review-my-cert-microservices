package com.bozntouran.reviewservice.persistence;

import com.bozntouran.reviewservice.config.TestSecurityConfig;
import jakarta.persistence.EntityManager;
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

import javax.swing.text.html.parser.Entity;
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
class ReviewTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void happy_path_for_review_entity(){
        //Arrange
        Review goodReview = Review.builder()
                .stars((short) 5)
                .publicId(UUID.randomUUID().toString())
                .comment("new comment")
                .userId(1L)
                .certificateId(1L)
                .build();
        //Act
        Review persistedEntity = testEntityManager.persistAndFlush(goodReview);
        //Assert
        Assertions.assertEquals(persistedEntity.getCertificateId(),goodReview.getCertificateId());
        Assertions.assertEquals(persistedEntity.getComment(),goodReview.getComment());
        Assertions.assertEquals(persistedEntity.getPublicId(),persistedEntity.getPublicId());
        Assertions.assertNotNull(persistedEntity.getId());
        Assertions.assertNotNull(persistedEntity.getUpdateDate());
        Assertions.assertNotNull(persistedEntity.getVersion());

    }

    @Test
    void not_happy_path_for_publicId(){
        //Arrange
        Review badReview = Review.builder()
                .stars((short) 5)
                .comment("new comment")
                .userId(1L)
                .certificateId(1L)
                .build();

        //Act & assert
        Assertions.assertThrows(
                ConstraintViolationException.class,()->
          testEntityManager.persistAndFlush(badReview)
        );
    }

    @Test
    void not_happy_path_for_userId(){
        //Arrange
        Review badReview = Review.builder()
                .stars((short) 5)
                .publicId(UUID.randomUUID().toString())
                .comment("new comment")
                .certificateId(1L)
                .build();

        //Act & assert
        Assertions.assertThrows(
                ConstraintViolationException.class,()->
                        testEntityManager.persistAndFlush(badReview)
        );
    }

    @Test
    void not_happy_path_for_certificateId(){
        //Arrange
        Review badReview = Review.builder()
                .stars((short) 5)
                .publicId(UUID.randomUUID().toString())
                .comment("new comment")
                .userId(1L)
                .build();

        //Act & assert
        Assertions.assertThrows(
                ConstraintViolationException.class,()->
                        testEntityManager.persistAndFlush(badReview)
        );
    }

    @Test
    void not_happy_path_for_stars_negative(){
        //Arrange
        Review badReview = Review.builder()
                .stars((short) -1)
                .publicId(UUID.randomUUID().toString())
                .comment("new comment")
                .certificateId(1L)
                .userId(1L)
                .build();

        //Act & assert
        Assertions.assertThrows(
                ConstraintViolationException.class,()->
                        testEntityManager.persistAndFlush(badReview)
        );
    }

    @Test
    void not_happy_path_for_stars_out_of_range(){
        //Arrange
        Review badReview = Review.builder()
                .stars((short) 6)
                .publicId(UUID.randomUUID().toString())
                .comment("new comment")
                .certificateId(1L)
                .userId(1L)
                .build();

        //Act & assert
        Assertions.assertThrows(
                ConstraintViolationException.class,()->
                        testEntityManager.persistAndFlush(badReview)
        );
    }
}