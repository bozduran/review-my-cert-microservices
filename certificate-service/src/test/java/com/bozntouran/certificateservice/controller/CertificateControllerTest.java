package com.bozntouran.certificateservice.controller;

import com.bozntouran.api.core.certificate.CertificateDto;
import com.bozntouran.api.core.certificate.CertificateField;
import com.bozntouran.api.core.certificate.CertificateService;
import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.certificateservice.config.TestSecurityConfig;
import com.bozntouran.certificateservice.mapper.CertificateMapper;
import com.bozntouran.certificateservice.persistence.Certificate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.core.util.Json;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(controllers = CertificateController.class,
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
class CertificateControllerTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    CertificateService certificateService;

    Certificate testCertificate;
    List<CertificateDto> certificateList;
    @BeforeEach
    void setUp() {
        testCertificate = Certificate.builder()
                .companyId(1L)
                .name("certificate_1")
                .description("certificate_1 decsription")
                .field(CertificateField.IT)
                .price(1)
                .publicId(UUID.randomUUID().toString())
                .build();

        certificateList = List.of(
                CertificateDto.builder()
                        .name("certificate_2")
                        .field(CertificateField.CYBERSECURITY)
                        .price(2)
                        .publicId(UUID.randomUUID().toString())
                        .build(),
                CertificateDto.builder()
                        .name("certificate_3")
                        .field(CertificateField.DEVELOPMENT)
                        .price(3)
                        .publicId(UUID.randomUUID().toString())
                        .build(),
                CertificateDto.builder()
                        .name("certificate_4")
                        .field(CertificateField.IT)
                        .price(4)
                        .publicId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @Test
    void getCertificateIdByPublicId() throws Exception {
        Mockito.when(certificateService.retrieveInternalIdByPublicId(anyString()))
                .thenReturn(1L);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/internal/certificate/"+testCertificate.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.OK.value() , mvcResult.getResponse().getStatus(),
                "200 HTTP status expected");

    }

    @Test
    void getCertificates() throws Exception {

        Page<CertificateDto> certificateDtoPage = new PageImpl<>(certificateList, PageRequest.of(0, 10), certificateList.size());
        Mockito.when(certificateService.getCertificates(null,null,null,
        null,null,null,null
        )).thenReturn(certificateDtoPage);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/certificate")
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String returnJson = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus()
        ,"200 HTTP status expected");
        System.out.println(mvcResult.getResponse().getContentAsString());
        Assertions.assertEquals(
                Integer.valueOf(certificateList.size()),
                JsonPath.read(returnJson,"$.content.length()")
        ,"Expected 3");

        Assertions.assertEquals(certificateList.get(0).getName(),
                JsonPath.read(returnJson, "$.content[0].name")
                );


        Assertions.assertEquals(certificateList.get(1).getName(),
                JsonPath.read(returnJson, "$.content[1].name")
        );        Assertions.assertEquals(certificateList.get(2).getName(),
                JsonPath.read(returnJson, "$.content[2].name")
        );


    }

    @Test
    void getCertificate() throws Exception {
        Mockito.when(certificateService.retrieveCertificateByPublicId(anyString()))
                .thenReturn(Optional.of(certificateList.get(0)));

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/certificate/"+certificateList.get(0).getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String returnJson = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus()
                ,"200 HTTP status expected");

        Assertions.assertEquals(certificateList.get(0).getName(),
                JsonPath.read(returnJson, "$.name"));

        Assertions.assertEquals(certificateList.get(0).getPrice(),
                JsonPath.read(returnJson, "$.price"));
        Assertions.assertEquals(certificateList.get(0).getField().toString(),
                JsonPath.read(returnJson, "$.field"));

    }

    @Test
    void postCertificate() throws Exception {
        CertificateDto certificateDto = CertificateMapper.MAPPER.fromCertificate(testCertificate);
        Mockito.when(certificateService.saveCertificate( certificateDto))
                .thenReturn( certificateDto );

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/certificate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certificateDto));

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult)).andReturn();

        String returnJson = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus() );

    }

    @Test
    void postCertificate_Not_Valid_Dto() throws Exception {
        testCertificate.setName(null);
        CertificateDto certificateDto = CertificateMapper.MAPPER.fromCertificate(testCertificate);
        Mockito.when(certificateService.saveCertificate( certificateDto))
                .thenReturn( certificateDto );

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/certificate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certificateDto));

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andReturn();

        String returnJson = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus() );

    }

    @Test
    void deleteCertificate() throws Exception {
        Mockito.when(certificateService.deleteCertificateByPublicId(anyString())).thenReturn(true);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/certificate/"+testCertificate.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    void updateCertificate() throws Exception {

        Mockito.when(certificateService.updateCertificate(CertificateMapper.MAPPER.fromCertificate(testCertificate)))
                .thenReturn(true);
        CertificateDto certificateDto = CertificateMapper.MAPPER.fromCertificate(testCertificate);
        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/certificate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                certificateDto));

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus() );

    }
}