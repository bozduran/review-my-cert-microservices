package com.bozntouran.companyservice.controller;

import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.api.core.company.CompanyService;
import com.bozntouran.companyservice.config.SecurityConfig;
import com.bozntouran.companyservice.config.TestJwtConfig;
import com.bozntouran.companyservice.config.TestSecurityConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
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
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(controllers = CompanyController.class,
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
class CompanyControllerTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private CompanyDto sampleCompany;
    private List<CompanyDto> companyDtos;

    @BeforeEach
    void setup() {
        sampleCompany = new CompanyDto("company",1994,"asdasd");
        sampleCompany.setPublicId(UUID.randomUUID().toString());
        sampleCompany.setName("Test Company");
        companyDtos = List.of(
                new CompanyDto("OpenAI", 2015, UUID.randomUUID().toString()),
                new CompanyDto("Google", 1998, UUID.randomUUID().toString()),
                new CompanyDto("Microsoft", 1975, UUID.randomUUID().toString()),
                new CompanyDto("Apple", 1976, UUID.randomUUID().toString())
        );
    }

    @Test
    void getCompanyIdByPublicId() throws Exception {
        Mockito.when(companyService.retrieveInternalIdByPublicId(anyString())).thenReturn(1L);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/internal/company/"+sampleCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.OK.value() , mvcResult.getResponse().getStatus(),
                "Fallse HTTP status");
    }

    @Test
    void getAllCompanies() throws Exception {
        Page<CompanyDto> companyDtoPage = new PageImpl<>(companyDtos, PageRequest.of(0, 10), companyDtos.size());
        Mockito.when(companyService.getCompanies(null,null)).thenReturn(companyDtoPage);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/company")
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus());
        System.out.println(mvcResult.getResponse().getContentAsString());
        Assertions.assertEquals(
                Integer.valueOf(companyDtos.size()),
                JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.content.length()"));


    }

    @Test
    void getCompany() throws Exception {
        Mockito.when(companyService.getCompanyById(sampleCompany.getPublicId()) ).thenReturn(Optional.ofNullable(sampleCompany));

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/company/"+sampleCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        String jsonContent = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus());
        Assertions.assertEquals(sampleCompany.getName(),
                JsonPath.read(jsonContent,"$.name" )
        );
        Assertions.assertEquals(sampleCompany.getPublicId(),
                JsonPath.read(jsonContent,"$.publicId" )
        );

    }

    @Test
    void postCompany() throws Exception {
        Mockito.when(companyService.saveNewCompany(sampleCompany))
                .thenReturn(sampleCompany);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany));

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus() );
    }

    @Test
    void deleteCompany() throws Exception {

        Mockito.when(companyService.deleteCompanyById(sampleCompany.getPublicId()) ).thenReturn(true);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/company/"+sampleCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    void updateCompany() throws Exception {

        Mockito.when(companyService.updateCompany(sampleCompany))
                .thenReturn(true);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany));

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus() );

    }
}