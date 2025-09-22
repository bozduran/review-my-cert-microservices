package com.bozntouran.companyservice.controller;

import com.bozntouran.api.core.company.CompanyDto;
import com.bozntouran.api.core.company.CompanyService;
import com.bozntouran.companyservice.config.TestSecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private CompanyDto testCompany;
    private List<CompanyDto> companyDtos;

    @BeforeEach
    void setup() {
        testCompany = new CompanyDto("company",1994,"asdasd");
        testCompany.setPublicId(UUID.randomUUID().toString());
        testCompany.setName("Test Company");
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
                MockMvcRequestBuilders.get("/internal/company/"+testCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.OK.value() , mvcResult.getResponse().getStatus(),
                "200 HTTP status expected");
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
        Mockito.when(companyService.getCompanyByPublicId(testCompany.getPublicId()) ).thenReturn(Optional.ofNullable(testCompany));

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/company/"+testCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        String jsonContent = mvcResult.getResponse().getContentAsString();

        Assertions.assertEquals(HttpStatus.OK.value(),mvcResult.getResponse().getStatus());
        Assertions.assertEquals(testCompany.getName(),
                JsonPath.read(jsonContent,"$.name" )
        );
        Assertions.assertEquals(testCompany.getPublicId(),
                JsonPath.read(jsonContent,"$.publicId" )
        );

    }

    @Test
    void postCompany() throws Exception {
        Mockito.when(companyService.saveCompany(testCompany))
                .thenReturn(testCompany);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCompany));

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus() );
    }

    @Test
    void deleteCompany() throws Exception {

        Mockito.when(companyService.deleteCompanyById(testCompany.getPublicId()) ).thenReturn(true);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/company/"+testCompany.getPublicId())
                        .accept(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    void updateCompany() throws Exception {

        Mockito.when(companyService.updateCompany(testCompany))
                .thenReturn(true);

        RequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCompany));

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus() );

    }
}