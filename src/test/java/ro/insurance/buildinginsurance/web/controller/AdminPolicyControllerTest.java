package ro.insurance.buildinginsurance.web.controller.admin;

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
import ro.insurance.buildinginsurance.application.service.IPolicyService;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.controller.admin.AdminPolicyController;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyDetailsResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyListFilterDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyListItemDTO;
import ro.insurance.buildinginsurance.web.error.CustomExceptionHandler;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminPolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPolicyService policyService;

    @InjectMocks
    private AdminPolicyController controller;

    private static final String BASE = "/api/admin/policies";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // --------------------- LIST ---------------------

    @Test
    void listPolicies_shouldReturn200_andCallServiceWithCorrectFilterAndPageable() throws Exception {

        PolicyListItemDTO dto = mock(PolicyListItemDTO.class);
        Page<PolicyListItemDTO> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(policyService.listPoliciesForAdmin(any(), any()))
                .thenReturn(page);

        ArgumentCaptor<PolicyListFilterDTO> filterCaptor =
                ArgumentCaptor.forClass(PolicyListFilterDTO.class);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get(BASE)
                        .param("clientId", "10")
                        .param("brokerId", "5")
                        .param("status", "ACTIVE")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .param("page", "1")
                        .param("pageSize", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(policyService)
                .listPoliciesForAdmin(filterCaptor.capture(), pageableCaptor.capture());

        PolicyListFilterDTO usedFilter = filterCaptor.getValue();
        Pageable usedPageable = pageableCaptor.getValue();

        assertAll(
                () -> assertEquals(10L, usedFilter.clientId()),
                () -> assertEquals(5L, usedFilter.brokerId()),
                () -> assertEquals(PolicyStatus.ACTIVE, usedFilter.status()),
                () -> assertEquals(LocalDate.of(2026,1,1), usedFilter.startDate()),
                () -> assertEquals(LocalDate.of(2026,12,31), usedFilter.endDate())
        );

        assertAll(
                () -> assertEquals(0, usedPageable.getPageNumber()),
                () -> assertEquals(20, usedPageable.getPageSize())
        );

        verifyNoMoreInteractions(policyService);
    }

    // --------------------- DETAILS ---------------------

    @Test
    void getPolicyDetails_shouldReturn200_andCallService() throws Exception {
        Long policyId = 99L;
        PolicyDetailsResponseDTO dto = mock(PolicyDetailsResponseDTO.class);

        when(policyService.getPolicyDetailsForAdmin(policyId))
                .thenReturn(dto);

        mockMvc.perform(get(BASE + "/{id}", policyId))
                .andExpect(status().isOk());

        verify(policyService).getPolicyDetailsForAdmin(policyId);
        verifyNoMoreInteractions(policyService);
    }
}
