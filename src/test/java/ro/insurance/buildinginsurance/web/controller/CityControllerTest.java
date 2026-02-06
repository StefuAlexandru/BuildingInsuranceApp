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
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.web.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.application.service.ICityService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class CityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICityService cityService;

    @InjectMocks
    private CityController cityController;

    private static final String BASE = "/api/geography/cities";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cityController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private String createCityRequestBody(String name, Long countyId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("countyId", countyId);
        return objectMapper.writeValueAsString(body);
    }

    // ---------------------  CREATE  ---------------------

    @Test
    void registerCity_shouldReturn201AndResponseBody() throws Exception {
        // Arrange
        CityRequestDTO requestDTO = aCityRequest();
        CityResponseDTO responseDTO = aCityResponse();

        String body = createCityRequestBody(requestDTO.name(), requestDTO.countyId());

        when(cityService.registerCity(any(CityRequestDTO.class))).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(responseDTO.id().intValue()),
                        jsonPath("$.name").value(responseDTO.name()),
                        jsonPath("$.county.id").value(responseDTO.county().id().intValue()),
                        jsonPath("$.county.name").value(responseDTO.county().name()),
                        jsonPath("$.county.countryId").value(responseDTO.county().countryId().intValue()),
                        jsonPath("$.county.countryName").value(responseDTO.county().countryName())
                );


        verify(cityService).registerCity(any(CityRequestDTO.class));
        verifyNoMoreInteractions(cityService);
    }

    @Test
    void registerCity_shouldReturn400WhenInvalid() throws Exception {
        String body = createCityRequestBody("   ", null);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cityService);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getCityById_shouldReturn200AndBody() throws Exception {
        // Arrange
        CityResponseDTO responseDTO = aCityResponse();
        when(cityService.getCityById(1L)).thenReturn(responseDTO);

        // Act + Assert
        mockMvc.perform(get(BASE + "/{cityId}", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(responseDTO.id().intValue()),
                        jsonPath("$.name").value(responseDTO.name()),
                        jsonPath("$.county.id").value(responseDTO.county().id().intValue())
                );

        verify(cityService).getCityById(1L);
        verifyNoMoreInteractions(cityService);
    }

    @Test
    void getCities_whenNoNameParam_shouldPassEmptyString_andReturn200() throws Exception {
        // Arrange
        CityResponseDTO c1 = aCityResponse();
        CityResponseDTO c2 = new CityResponseDTO(2L, "Adjud-2", aCountyDTO());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<CityResponseDTO> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(cityService.getCityByPartialName(eq(""), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get(BASE)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(2),
                        jsonPath("$.content[0].id").value(c1.id().intValue()),
                        jsonPath("$.content[0].name").value(c1.name()),
                        jsonPath("$.content[1].id").value(c2.id().intValue()),
                        jsonPath("$.content[1].name").value(c2.name()),
                        jsonPath("$.totalElements").value(2)
                );

        verify(cityService).getCityByPartialName(eq(""), any(Pageable.class));
        verifyNoMoreInteractions(cityService);
    }

    @Test
    void getCities_whenNameProvided_shouldReturn200() throws Exception {
        // Arrange
        CityResponseDTO cityResponseDTO = aCityResponse();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        Page<CityResponseDTO> page = new PageImpl<>(List.of(cityResponseDTO), pageable, 1);

        when(cityService.getCityByPartialName(eq("adj"), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get(BASE)
                        .param("name", "adj")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content.length()").value(1),
                        jsonPath("$.content[0].id").value(cityResponseDTO.id().intValue()),
                        jsonPath("$.content[0].name").value(cityResponseDTO.name()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(cityService).getCityByPartialName(eq("adj"), any(Pageable.class));
        verifyNoMoreInteractions(cityService);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateCity_shouldReturn200AndBody() throws Exception {
        // Arrange
        CityRequestDTO cityRequestDTO = aCityRequest();
        CityResponseDTO cityResponseDTO = aCityResponse();

        String body = createCityRequestBody("Adjud Updated", cityRequestDTO.countyId());

        when(cityService.updateCity(eq(1L), any(CityRequestDTO.class))).thenReturn(cityResponseDTO);

        // Act + Assert
        mockMvc.perform(put(BASE + "/{cityId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(cityResponseDTO.id().intValue()),
                        jsonPath("$.name").value(cityResponseDTO.name()),
                        jsonPath("$.county.id").value(cityResponseDTO.county().id().intValue())
                );

        verify(cityService).updateCity(eq(1L), any(CityRequestDTO.class));
        verifyNoMoreInteractions(cityService);
    }

    @Test
    void updateCity_shouldReturn400WhenInvalid() throws Exception {
        String body = createCityRequestBody("   ", null);

        mockMvc.perform(put(BASE + "/{cityId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cityService);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteCity_shouldReturn204() throws Exception {
        doNothing().when(cityService).deleteCity(1L);

        mockMvc.perform(delete(BASE + "/{cityId}", 1L))
                .andExpect(status().isNoContent());

        verify(cityService).deleteCity(1L);
        verifyNoMoreInteractions(cityService);
    }


}