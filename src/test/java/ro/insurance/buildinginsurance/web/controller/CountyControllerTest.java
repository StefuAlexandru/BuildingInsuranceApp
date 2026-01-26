package ro.insurance.buildinginsurance.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.web.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.application.service.ICountyService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class CountyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICountyService countyService;

    @InjectMocks
    private CountyController countyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(countyController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private String createCountyRequestBody(String name, Long countryId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("countryId", countryId);
        return objectMapper.writeValueAsString(body);
    }

    // ---------------------  CREATE  ---------------------

    @Test
    void registerCounty_shouldReturn201AndResponseBody() throws Exception {
        // Arrange
        CountyRequestDTO requestDTO = aCountyRequest();
        CountyResponseDTO responseDTO = aCountyResponse();

        String body = createCountyRequestBody(requestDTO.name(), requestDTO.countryId());

        when(countyService.registerCounty(any(CountyRequestDTO.class))).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/geography/counties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(responseDTO.id().intValue()),
                        jsonPath("$.name").value(responseDTO.name()),
                        jsonPath("$.country.id").value(responseDTO.country().id().intValue()),
                        jsonPath("$.country.name").value(responseDTO.country().name())
                );

        verify(countyService).registerCounty(any(CountyRequestDTO.class));
        verifyNoMoreInteractions(countyService);
    }

    @Test
    void registerCounty_shouldReturn400WhenInvalid() throws Exception {
        String body = createCountyRequestBody("   ", null);

        mockMvc.perform(post("/api/geography/counties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(countyService);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getCountyById_shouldReturn200AndBody() throws Exception {
        // Arrange
        CountyResponseDTO responseDTO = aCountyResponse();
        when(countyService.getCountyById(1L)).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(get("/api/geography/counties/{countyId}", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(responseDTO.id().intValue()),
                        jsonPath("$.name").value(responseDTO.name()),
                        jsonPath("$.country.id").value(responseDTO.country().id().intValue()),
                        jsonPath("$.country.name").value(responseDTO.country().name())
                );

        verify(countyService).getCountyById(1L);
        verifyNoMoreInteractions(countyService);
    }

    @Test
    void getCounties_whenNoNameParam_shouldPassEmptyString_andReturn200() throws Exception {
        // Arrange
        CountyResponseDTO c1 = aCountyResponse();
        CountyResponseDTO c2 = aCountyResponseWithIdAndName(2L, "Bihor");

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<CountyResponseDTO> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(countyService.getCountyByPartialName(eq(""), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/geography/counties")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(2),
                        jsonPath("$.content[0].id").value(c1.id().intValue()),
                        jsonPath("$.content[0].name").value(c1.name()),
                        jsonPath("$.content[0].country.id").value(c1.country().id().intValue()),
                        jsonPath("$.content[1].id").value(c2.id().intValue()),
                        jsonPath("$.content[1].name").value(c2.name()),
                        jsonPath("$.totalElements").value(2)
                );

        verify(countyService).getCountyByPartialName(eq(""), any(Pageable.class));
        verifyNoMoreInteractions(countyService);
    }

    @Test
    void getCounties_whenNameProvided_shouldReturn200() throws Exception {
        // Arrange
        CountyResponseDTO c1 = aCountyResponse();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<CountyResponseDTO> page = new PageImpl<>(List.of(c1), pageable, 1);

        when(countyService.getCountyByPartialName(eq("vra"), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/geography/counties")
                        .param("name", "vra")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(1),
                        jsonPath("$.content[0].id").value(c1.id().intValue()),
                        jsonPath("$.content[0].name").value(c1.name()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(countyService).getCountyByPartialName(eq("vra"), any(Pageable.class));
        verifyNoMoreInteractions(countyService);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateCounty_shouldReturn200AndBody() throws Exception {
        // Arrange
        CountyRequestDTO requestDTO = aCountyRequest();
        CountyResponseDTO responseDTO = aCountyResponse();

        String body = createCountyRequestBody("Bihor", requestDTO.countryId());

        when(countyService.updateCounty(eq(1L), any(CountyRequestDTO.class))).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(put("/api/geography/counties/{countyId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(responseDTO.id().intValue()),
                        jsonPath("$.name").value(responseDTO.name()),
                        jsonPath("$.country.id").value(responseDTO.country().id().intValue()),
                        jsonPath("$.country.name").value(responseDTO.country().name())
                );

        verify(countyService).updateCounty(eq(1L), any(CountyRequestDTO.class));
        verifyNoMoreInteractions(countyService);
    }

    @Test
    void updateCounty_shouldReturn400WhenInvalid() throws Exception {
        String body = createCountyRequestBody("   ", null);

        mockMvc.perform(put("/api/geography/counties/{countyId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(countyService);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteCounty_shouldReturn204() throws Exception {
        doNothing().when(countyService).deleteCounty(1L);

        mockMvc.perform(delete("/api/geography/counties/{countyId}", 1L))
                .andExpect(status().isNoContent());

        verify(countyService).deleteCounty(1L);
        verifyNoMoreInteractions(countyService);
    }
}
