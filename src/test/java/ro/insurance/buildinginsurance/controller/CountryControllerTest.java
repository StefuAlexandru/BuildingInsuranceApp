package ro.insurance.buildinginsurance.controller;

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
import ro.insurance.buildinginsurance.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.service.ICountryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICountryService countryService;

    @InjectMocks
    private CountryController countryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(countryController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private String createCountryRequestBody(String name) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        return objectMapper.writeValueAsString(body);
    }

    // ---------------------  CREATE  ---------------------

    @Test
    void registerCountry_shouldReturn201AndResponseBody() throws Exception {
        // Arrange
        CountryRequestDTO countryRequestDTO = new CountryRequestDTO("Romania");
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");

        String body = createCountryRequestBody(countryRequestDTO.name());

        when(countryService.registerCountry(any(CountryRequestDTO.class))).thenReturn(countryResponseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/brokers/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(countryResponseDTO.id().intValue()),
                        jsonPath("$.name").value(countryResponseDTO.name())
                );

        verify(countryService).registerCountry(any(CountryRequestDTO.class));
        verifyNoMoreInteractions(countryService);
    }

    @Test
    void registerCountry_shouldReturn400WhenInvalid() throws Exception {
        String body = createCountryRequestBody("   ");

        mockMvc.perform(post("/api/brokers/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(countryService);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getCountryById_shouldReturn200AndBody() throws Exception {
        // Arrange
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");
        when(countryService.getCountryById(1L)).thenReturn(countryResponseDTO);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/countries/{countryId}", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(countryResponseDTO.id().intValue()),
                        jsonPath("$.name").value(countryResponseDTO.name())
                );

        verify(countryService).getCountryById(1L);
        verifyNoMoreInteractions(countryService);
    }

    @Test
    void getCountries_whenNoNameParam_shouldPassEmptyString_andReturn200() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        CountryResponseDTO countryResponseDTO1 = new CountryResponseDTO(1L, "Romania");
        CountryResponseDTO countryResponseDTO2 = new CountryResponseDTO(2L, "Rwanda");

        Page<CountryResponseDTO> page = new PageImpl<>(List.of(countryResponseDTO1, countryResponseDTO2), pageable, 2);

        when(countryService.getCountryByPartialName(eq(""), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/countries")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(2),
                        jsonPath("$.content[0].id").value(countryResponseDTO1.id().intValue()),
                        jsonPath("$.content[0].name").value(countryResponseDTO1.name()),
                        jsonPath("$.content[1].id").value(countryResponseDTO2.id().intValue()),
                        jsonPath("$.content[1].name").value(countryResponseDTO2.name()),
                        jsonPath("$.totalElements").value(2)
                );

        verify(countryService).getCountryByPartialName(eq(""), any(Pageable.class));
        verifyNoMoreInteractions(countryService);
    }

    @Test
    void getCountries_whenNameProvided_shouldReturn200() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");
        Page<CountryResponseDTO> page = new PageImpl<>(List.of(countryResponseDTO), pageable, 1);

        when(countryService.getCountryByPartialName(eq("rom"), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/countries")
                        .param("name", "rom")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(1),
                        jsonPath("$.content[0].id").value(countryResponseDTO.id().intValue()),
                        jsonPath("$.content[0].name").value(countryResponseDTO.name()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(countryService).getCountryByPartialName(eq("rom"), any(Pageable.class));
        verifyNoMoreInteractions(countryService);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateCountry_shouldReturn200AndBody() throws Exception {
        // Arrange
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");
        String body = createCountryRequestBody("Romania");

        when(countryService.updateCountry(eq(1L), any(CountryRequestDTO.class))).thenReturn(countryResponseDTO);

        // Act + Assert
        mockMvc.perform(put("/api/brokers/countries/{countryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(countryResponseDTO.id().intValue()),
                        jsonPath("$.name").value(countryResponseDTO.name())
                );

        verify(countryService).updateCountry(eq(1L), any(CountryRequestDTO.class));
        verifyNoMoreInteractions(countryService);
    }

    @Test
    void updateCountry_shouldReturn400WhenInvalid() throws Exception {
        String body = createCountryRequestBody("   ");

        mockMvc.perform(put("/api/brokers/countries/{countryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(countryService);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteCountry_shouldReturn204() throws Exception {
        doNothing().when(countryService).deleteCountry(1L);

        mockMvc.perform(delete("/api/brokers/countries/{countryId}", 1L))
                .andExpect(status().isNoContent());

        verify(countryService).deleteCountry(1L);
        verifyNoMoreInteractions(countryService);
    }
}
