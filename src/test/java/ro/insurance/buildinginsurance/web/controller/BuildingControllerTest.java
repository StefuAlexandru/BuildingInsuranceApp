package ro.insurance.buildinginsurance.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ro.insurance.buildinginsurance.web.dto.buildingDto.*;
import ro.insurance.buildinginsurance.web.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.application.service.IBuildingService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class BuildingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IBuildingService buildingService;

    @InjectMocks
    private BuildingController buildingController;

    private static final String BASE = "/api/brokers/buildings";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(buildingController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private String createBuildingRequestBody(Long clientId, Long cityId, String street, String number, Integer constructionYear, String buildingType, Integer floors, double surfaceArea, double insuredValue, Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", clientId);
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    private String createBuildingUpdateRequestBody(Long cityId, String street, String number, Integer constructionYear, String buildingType, Integer floors, double surfaceArea, double insuredValue, Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    private String createBuildingPatchBody(Long cityId, String street, String number, Integer constructionYear, String buildingType, Integer floors, Double surfaceArea, Double insuredValue, Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    // ---------------------  CREATE  ---------------------

    @Test
    void createBuilding_shouldReturn201AndResponseBody() throws Exception {
        // Arrange
        BuildingRequestDTO buildingRequestDTO = aBuildingRequest();
        BuildingResponseDTO buildingResponseDTO = aBuildingResponse();

        String body = createBuildingRequestBody(
                buildingRequestDTO.clientId(), buildingRequestDTO.cityId(),
                buildingRequestDTO.street(), buildingRequestDTO.number(),
                buildingRequestDTO.constructionYear(), buildingRequestDTO.buildingType().name(),
                buildingRequestDTO.floors(),
                buildingRequestDTO.surfaceArea().doubleValue(),
                buildingRequestDTO.insuredValue().doubleValue(),
                buildingRequestDTO.floodZone(), buildingRequestDTO.earthquakeZone()
        );

        when(buildingService.registerNewBuilding(any(BuildingRequestDTO.class))).thenReturn(buildingResponseDTO);

        // Act + Assert
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.street").value(buildingResponseDTO.street()),
                        jsonPath("$.number").value(buildingResponseDTO.number()),
                        jsonPath("$.constructionYear").value(buildingResponseDTO.constructionYear()),
                        jsonPath("$.buildingType").value(buildingResponseDTO.buildingType().name()),
                        jsonPath("$.city.id").value(buildingResponseDTO.city().id().intValue()),
                        jsonPath("$.city.name").value(buildingResponseDTO.city().name())
                );

        verify(buildingService).registerNewBuilding(any(BuildingRequestDTO.class));
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void createBuilding_shouldReturn400WhenInvalid() throws Exception {
        String body = createBuildingRequestBody(
                1L, null, "   ", "   ",
                1700, "RESIDENTIAL", 1,
                0.0, 0.0,
                true, true
        );

        // Act + Assert
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(buildingService);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getBuildingById_shouldReturn200AndBody() throws Exception {
        // Arrange
        BuildingResponseDTO buildingResponseDTO = aBuildingResponse();
        when(buildingService.getBuildingById(1L)).thenReturn(buildingResponseDTO);

        // Act + Assert
        mockMvc.perform(get(BASE + "/{buildingId}", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.street").value(buildingResponseDTO.street()),
                        jsonPath("$.buildingType").value(buildingResponseDTO.buildingType().name()),
                        jsonPath("$.city.id").value(buildingResponseDTO.city().id().intValue())
                );

        verify(buildingService).getBuildingById(1L);
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void getBuildingDetails_shouldReturn200AndBody() throws Exception {
        // Arrange
        Long buildingId = 1L;
        BuildingDetailsResponseDTO buildingDetailsResponseDTO = aBuildingDetailsResponse();
        when(buildingService.getBuildingDetailsById(buildingId)).thenReturn(buildingDetailsResponseDTO);

        // Act + Assert
        mockMvc.perform(get(BASE + "/{buildingId}/details", buildingId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingDetailsResponseDTO.id().intValue()),
                        jsonPath("$.street").value(buildingDetailsResponseDTO.street()),
                        jsonPath("$.constructionYear").value(buildingDetailsResponseDTO.constructionYear()),
                        jsonPath("$.buildingType").value(buildingDetailsResponseDTO.buildingType().name()),
                        jsonPath("$.floors").value(buildingDetailsResponseDTO.floors()),
                        jsonPath("$.surfaceArea").value(buildingDetailsResponseDTO.surfaceArea().doubleValue()),
                        jsonPath("$.insuredValue").value(buildingDetailsResponseDTO.insuredValue().doubleValue()),
                        jsonPath("$.floodZone").value(buildingDetailsResponseDTO.floodZone()),
                        jsonPath("$.earthquakeZone").value(buildingDetailsResponseDTO.earthquakeZone()),
                        jsonPath("$.city.id").value(buildingDetailsResponseDTO.city().id().intValue())
                );

        verify(buildingService).getBuildingDetailsById(buildingId);
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void getBuildingsByClientId_shouldReturn200_andDefaultPageable() throws Exception {
        // Arrange
        Long clientId = 1L;
        BuildingResponseDTO buildingResponseDTO = aBuildingResponse();

        Pageable expected = PageRequest.of(0, 5, Sort.by("id"));
        Page<BuildingResponseDTO> page = new PageImpl<>(List.of(buildingResponseDTO), expected, 1);

        when(buildingService.getBuildingsByClientId(eq(clientId), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act + Assert
        mockMvc.perform(get(BASE)
                        .param("clientId", clientId.toString()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.content[0].street").value(buildingResponseDTO.street()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(buildingService).getBuildingsByClientId(eq(clientId), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertAll(
                () -> assertEquals(0, used.getPageNumber()),
                () -> assertEquals(5, used.getPageSize()),
                () -> assertNotNull(used.getSort().getOrderFor("id"))
        );

        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void getBuildingsByClientId_shouldReturn200_whenCustomPageable() throws Exception {
        // Arrange
        Long clientId = 1L;
        BuildingResponseDTO buildingResponseDTO = aBuildingResponse();
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").descending());
        Page<BuildingResponseDTO> page = new PageImpl<>(List.of(buildingResponseDTO), pageable, 6);

        when(buildingService.getBuildingsByClientId(eq(clientId), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(get(BASE)
                        .param("clientId", clientId.toString())
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "id,desc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.totalElements").value(6)
                );

        verify(buildingService).getBuildingsByClientId(eq(clientId), any(Pageable.class));
        verifyNoMoreInteractions(buildingService);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateBuilding_shouldReturn200AndBody() throws Exception {
        // Arrange
        Long buildingId = 10L;
        BuildingUpdateRequestDTO buildingUpdateRequestDTO = aBuildingUpdateRequest();
        BuildingDetailsResponseDTO buildingResponseDTO = aBuildingDetailsResponse();

        String body = createBuildingUpdateRequestBody(
                buildingUpdateRequestDTO.cityId(),
                buildingUpdateRequestDTO.street(), buildingUpdateRequestDTO.number(),
                buildingUpdateRequestDTO.constructionYear(),
                buildingUpdateRequestDTO.buildingType().name(),
                buildingUpdateRequestDTO.floors(),
                buildingUpdateRequestDTO.surfaceArea().doubleValue(),
                buildingUpdateRequestDTO.insuredValue().doubleValue(),
                buildingUpdateRequestDTO.floodZone(), buildingUpdateRequestDTO.earthquakeZone()
        );

        when(buildingService.updateBuilding(eq(buildingId), any(BuildingUpdateRequestDTO.class))).thenReturn(buildingResponseDTO);

        // Act + Assert
        mockMvc.perform(put(BASE + "/{buildingId}", buildingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.client.id").value(buildingResponseDTO.client().id().intValue()),
                        jsonPath("$.city.id").value(buildingResponseDTO.city().id().intValue()),
                        jsonPath("$.city.name").value(buildingResponseDTO.city().name()),
                        jsonPath("$.number").value(buildingResponseDTO.number()),
                        jsonPath("$.constructionYear").value(buildingResponseDTO.constructionYear()),
                        jsonPath("$.floors").value(buildingResponseDTO.floors()),
                        jsonPath("$.insuredValue").value(buildingResponseDTO.insuredValue()),
                        jsonPath("$.surfaceArea").value(buildingResponseDTO.surfaceArea().doubleValue()),
                        jsonPath("$.floodZone").value(buildingResponseDTO.floodZone()),
                        jsonPath("$.earthquakeZone").value(buildingResponseDTO.earthquakeZone()),
                        jsonPath("$.street").value(buildingResponseDTO.street()),
                        jsonPath("$.buildingType").value(buildingResponseDTO.buildingType().name())
                );

        verify(buildingService).updateBuilding(eq(buildingId), any(BuildingUpdateRequestDTO.class));
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void updateBuilding_shouldReturn400WhenInvalid() throws Exception {
        String body = createBuildingUpdateRequestBody(
                1L, "   ", "10A", 2000, "RESIDENTIAL",
                1, 10.0, 100.0, true, true
        );

        // Act + Assert
        mockMvc.perform(put(BASE + "/{buildingId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(buildingService);
    }

    // ---------------------  PATCH  ---------------------

    @Test
    void patchBuilding_shouldReturn200AndBody() throws Exception {
        // Arrange
        Long buildingId = 10L;
        BuildingResponseDTO buildingResponseDTO = aBuildingResponse();

        String body = createBuildingPatchBody(
                null, "Patched Street", null, null, null, null,
                null, null, null, null
        );

        when(buildingService.patchBuilding(eq(buildingId), any(BuildingPatchDTO.class))).thenReturn(buildingResponseDTO);

        // Act + Assert
        mockMvc.perform(patch(BASE + "/{buildingId}", buildingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingResponseDTO.id().intValue()),
                        jsonPath("$.street").value(buildingResponseDTO.street())
                );

        verify(buildingService).patchBuilding(eq(buildingId), any(BuildingPatchDTO.class));
        verifyNoMoreInteractions(buildingService);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteBuilding_shouldReturn204() throws Exception {
        // Arrange
        Long buildingId = 99L;
        doNothing().when(buildingService).deleteBuilding(buildingId);

        // Act + Assert
        mockMvc.perform(delete(BASE + "/{buildingId}", buildingId))
                .andExpect(status().isNoContent());

        verify(buildingService).deleteBuilding(buildingId);
        verifyNoMoreInteractions(buildingService);
    }
}
