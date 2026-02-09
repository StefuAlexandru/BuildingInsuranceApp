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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ro.insurance.buildinginsurance.application.service.IBuildingService;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;
import ro.insurance.buildinginsurance.web.error.CustomExceptionHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class BuildingControllerTest {

    private MockMvc mockMvc;

    @Mock private IBuildingService buildingService;
    @InjectMocks private BuildingController buildingController;

    private static final String BASE = "/api/brokers/buildings";
    private static final String USER = "broker@email.com";
    private static final String PASS = "pass";

    private static RequestPostProcessor principalEmail() {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(USER, PASS));
            return request;
        };
    }

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(buildingController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    // --------------------- CREATE ---------------------

    @Test
    void createBuilding_shouldReturn201AndResponseBody() throws Exception {
        BuildingRequestDTO req = aBuildingRequest();
        BuildingResponseDTO res = aBuildingResponse();

        String body = createBuildingRequestBody(
                req.clientId(), req.cityId(),
                req.street(), req.number(),
                req.constructionYear(), req.buildingType().name(),
                req.floors(),
                req.surfaceArea().doubleValue(),
                req.insuredValue().doubleValue(),
                req.floodZone(), req.earthquakeZone()
        );

        when(buildingService.registerNewBuilding(any(BuildingRequestDTO.class), eq(USER))).thenReturn(res);

        mockMvc.perform(post(BASE)
                        .with(principalEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(res.id().intValue()),
                        jsonPath("$.street").value(res.street()),
                        jsonPath("$.number").value(res.number()),
                        jsonPath("$.constructionYear").value(res.constructionYear()),
                        jsonPath("$.buildingType").value(res.buildingType().name()),
                        jsonPath("$.city.id").value(res.city().id().intValue()),
                        jsonPath("$.city.name").value(res.city().name())
                );

        verify(buildingService).registerNewBuilding(any(BuildingRequestDTO.class), eq(USER));
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

        mockMvc.perform(post(BASE)
                        .with(principalEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(buildingService);
    }

    // --------------------- GET ---------------------

    @Test
    void getBuildingById_shouldReturn200AndBody() throws Exception {
        BuildingResponseDTO res = aBuildingResponse();
        when(buildingService.getBuildingById(1L, USER)).thenReturn(res);

        mockMvc.perform(get(BASE + "/{buildingId}", 1L)
                        .with(principalEmail()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(res.id().intValue()),
                        jsonPath("$.street").value(res.street()),
                        jsonPath("$.buildingType").value(res.buildingType().name()),
                        jsonPath("$.city.id").value(res.city().id().intValue())
                );

        verify(buildingService).getBuildingById(1L, USER);
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void getBuildingDetails_shouldReturn200AndBody() throws Exception {
        long buildingId = 1L;
        BuildingDetailsResponseDTO res = aBuildingDetailsResponse();

        when(buildingService.getBuildingDetailsById(buildingId, USER)).thenReturn(res);

        mockMvc.perform(get(BASE + "/{buildingId}/details", buildingId)
                        .with(principalEmail()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(res.id().intValue()),
                        jsonPath("$.street").value(res.street()),
                        jsonPath("$.constructionYear").value(res.constructionYear()),
                        jsonPath("$.buildingType").value(res.buildingType().name()),
                        jsonPath("$.floors").value(res.floors()),
                        jsonPath("$.surfaceArea").value(res.surfaceArea().doubleValue()),
                        jsonPath("$.insuredValue").value(res.insuredValue().doubleValue()),
                        jsonPath("$.floodZone").value(res.floodZone()),
                        jsonPath("$.earthquakeZone").value(res.earthquakeZone()),
                        jsonPath("$.city.id").value(res.city().id().intValue())
                );

        verify(buildingService).getBuildingDetailsById(buildingId, USER);
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void getBuildingsByClientId_shouldReturn200_andDefaultPageable() throws Exception {
        long clientId = 1L;
        BuildingResponseDTO dto = aBuildingResponse();

        Pageable expected = PageRequest.of(0, 5, Sort.by("id"));
        Page<BuildingResponseDTO> page = new PageImpl<>(List.of(dto), expected, 1);

        when(buildingService.getBuildingsByClientId(eq(clientId), any(Pageable.class), eq(USER))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get(BASE)
                        .with(principalEmail())
                        .param("clientId", String.valueOf(clientId)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(dto.id().intValue()),
                        jsonPath("$.content[0].street").value(dto.street()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(buildingService).getBuildingsByClientId(eq(clientId), pageableCaptor.capture(), eq(USER));

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
        long clientId = 1L;
        BuildingResponseDTO dto = aBuildingResponse();

        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").descending());
        Page<BuildingResponseDTO> page = new PageImpl<>(List.of(dto), pageable, 6);

        when(buildingService.getBuildingsByClientId(eq(clientId), any(Pageable.class), eq(USER))).thenReturn(page);

        mockMvc.perform(get(BASE)
                        .with(principalEmail())
                        .param("clientId", String.valueOf(clientId))
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "id,desc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(dto.id().intValue()),
                        jsonPath("$.totalElements").value(6)
                );

        verify(buildingService).getBuildingsByClientId(eq(clientId), any(Pageable.class), eq(USER));
        verifyNoMoreInteractions(buildingService);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateBuilding_shouldReturn200AndBody() throws Exception {
        long buildingId = 10L;
        BuildingUpdateRequestDTO req = aBuildingUpdateRequest();
        BuildingDetailsResponseDTO res = aBuildingDetailsResponse();

        String body = createBuildingUpdateRequestBody(
                req.cityId(),
                req.street(), req.number(),
                req.constructionYear(),
                req.buildingType().name(),
                req.floors(),
                req.surfaceArea().doubleValue(),
                req.insuredValue().doubleValue(),
                req.floodZone(), req.earthquakeZone()
        );

        when(buildingService.updateBuilding(eq(buildingId), any(BuildingUpdateRequestDTO.class), eq(USER))).thenReturn(res);

        mockMvc.perform(put(BASE + "/{buildingId}", buildingId)
                        .with(principalEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(res.id().intValue()),
                        jsonPath("$.client.id").value(res.client().id().intValue()),
                        jsonPath("$.city.id").value(res.city().id().intValue()),
                        jsonPath("$.city.name").value(res.city().name()),
                        jsonPath("$.number").value(res.number()),
                        jsonPath("$.constructionYear").value(res.constructionYear()),
                        jsonPath("$.floors").value(res.floors()),
                        jsonPath("$.insuredValue").value(res.insuredValue().doubleValue()),
                        jsonPath("$.surfaceArea").value(res.surfaceArea().doubleValue()),
                        jsonPath("$.floodZone").value(res.floodZone()),
                        jsonPath("$.earthquakeZone").value(res.earthquakeZone()),
                        jsonPath("$.street").value(res.street()),
                        jsonPath("$.buildingType").value(res.buildingType().name())
                );

        verify(buildingService).updateBuilding(eq(buildingId), any(BuildingUpdateRequestDTO.class), eq(USER));
        verifyNoMoreInteractions(buildingService);
    }

    @Test
    void updateBuilding_shouldReturn400WhenInvalid() throws Exception {
        String body = createBuildingUpdateRequestBody(
                1L, "   ", "10A", 2000, "RESIDENTIAL",
                1, 10.0, 100.0, true, true
        );

        mockMvc.perform(put(BASE + "/{buildingId}", 1L)
                        .with(principalEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(buildingService);
    }

    // --------------------- PATCH ---------------------

    @Test
    void patchBuilding_shouldReturn200AndBody() throws Exception {
        long buildingId = 10L;
        BuildingResponseDTO res = aBuildingResponse();

        String body = createBuildingPatchBody(
                null, "Patched Street", null, null, null, null,
                null, null, null, null
        );

        when(buildingService.patchBuilding(eq(buildingId), any(BuildingPatchDTO.class), eq(USER))).thenReturn(res);

        mockMvc.perform(patch(BASE + "/{buildingId}", buildingId)
                        .with(principalEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(res.id().intValue()),
                        jsonPath("$.street").value(res.street())
                );

        verify(buildingService).patchBuilding(eq(buildingId), any(BuildingPatchDTO.class), eq(USER));
        verifyNoMoreInteractions(buildingService);
    }

    // --------------------- DELETE ---------------------

    @Test
    void deleteBuilding_shouldReturn204() throws Exception {
        long buildingId = 99L;
        doNothing().when(buildingService).deleteBuilding(buildingId, USER);

        mockMvc.perform(delete(BASE + "/{buildingId}", buildingId)
                        .with(principalEmail()))
                .andExpect(status().isNoContent());

        verify(buildingService).deleteBuilding(buildingId, USER);
        verifyNoMoreInteractions(buildingService);
    }
}
