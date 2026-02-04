package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import ro.insurance.buildinginsurance.application.facade.PolicyFacade;
import ro.insurance.buildinginsurance.application.mapper.PolicyMapper;
import ro.insurance.buildinginsurance.application.service.impl.PolicyService;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;
import ro.insurance.buildinginsurance.util.PolicyNumberGenerator;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private PolicyMapper policyMapper;
    @Mock private PolicyFacade policyFacade;
    @Mock private IPolicyCalculationService policyCalculationService;
    @Mock private PolicyNumberGenerator policyNumberGenerator;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-02-04T10:00:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private PolicyService policyService;

    @Captor private ArgumentCaptor<PolicyCalculationRequestDTO> calcReqCaptor;
    @Captor private ArgumentCaptor<Policy> policyCaptor;
    @Captor private ArgumentCaptor<Specification<Policy>> specCaptor;

    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.parse("2026-02-04T10:00:00Z");
    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 2, 4);

    private static void assertHasError(BusinessException ex, ErrorCode code) {
        assertTrue(ex.getErrors().stream().anyMatch(e -> code.name().equals(e.getCode())));
    }

    // --------------------- CREATE ---------------------

    @Test
    void createDraftPolicy_shouldResolveContext_map_calculate_setFields_save_andReturnResponse() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());

        var ctx = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        Policy entity = aPolicyEntityDraft();
        PolicyResponseDTO response = aPolicyResponse();

        when(policyFacade.resolveCreateContext(request)).thenReturn(ctx);
        when(policyMapper.toEntity(eq(request), eq(client), eq(building), eq(broker), eq(currency))).thenReturn(entity);

        when(policyCalculationService.calculatePremium(any()))
                .thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));

        when(policyNumberGenerator.generatePolicyNumber()).thenReturn("POL-20260202-AAAAAAAAAA");

        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.createDraftPolicy(request);

        assertSame(response, res);

        verify(policyCalculationService).calculatePremium(calcReqCaptor.capture());
        PolicyCalculationRequestDTO calcReq = calcReqCaptor.getValue();

        assertAll(
                () -> assertEquals(request.basePremiumAmount(), calcReq.basePremium()),
                () -> assertEquals(request.buildingId(), calcReq.buildingId()),
                () -> assertEquals(request.currencyId(), calcReq.currencyId()),
                () -> assertEquals(FIXED_TODAY, calcReq.now())
        );

        verify(policyRepository).save(policyCaptor.capture());
        Policy savedArg = policyCaptor.getValue();

        assertAll(
                () -> assertEquals(new BigDecimal("130.00"), savedArg.getFinalPremiumAmount()),
                () -> assertEquals("POL-20260202-AAAAAAAAAA", savedArg.getPolicyNumber())
        );
    }

    @Test
    void createDraftPolicy_whenCalculationThrows_shouldPropagate_andNotSave() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());
        var ctx = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        when(policyFacade.resolveCreateContext(any())).thenReturn(ctx);
        when(policyMapper.toEntity(any(), any(), any(), any(), any())).thenReturn(aPolicyEntityDraft());

        when(policyCalculationService.calculatePremium(any()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> policyService.createDraftPolicy(request));

        verify(policyCalculationService).calculatePremium(any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
        verify(policyNumberGenerator, never()).generatePolicyNumber();
    }

    // --------------------- ACTIVATE ---------------------

    @Test
    void activatePolicy_happyPath_shouldValidate_recalc_setFields_save_andReturnResponse() {
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.getBroker().setCommissionPct(new BigDecimal("10.00"));

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyCalculationService.calculatePremium(any()))
                .thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));

        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.activatePolicy(id);

        assertSame(response, res);

        verify(policyFacade).validateActivateOrThrow(policy);

        verify(policyCalculationService).calculatePremium(calcReqCaptor.capture());
        assertEquals(FIXED_TODAY, calcReqCaptor.getValue().now());

        verify(policyRepository).save(policyCaptor.capture());
        Policy arg = policyCaptor.getValue();

        assertAll(
                () -> assertEquals(PolicyStatus.ACTIVE, arg.getStatus()),
                () -> assertEquals(new BigDecimal("130.00"), arg.getFinalPremiumAmount()),
                () -> assertEquals(new BigDecimal("10.00"), arg.getBrokerCommissionPct()),
                () -> assertEquals(new BigDecimal("13.00"), arg.getBrokerCommissionAmount()),
                () -> assertEquals(FIXED_NOW, arg.getActivatedAt())
        );
    }

    @Test
    void activatePolicy_whenNotFound_shouldThrow_andStop() {
        Long id = 404L;
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyService.activatePolicy(id));
        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verifyNoInteractions(policyFacade, policyCalculationService, policyMapper);
    }

    @Test
    void activatePolicy_whenFacadeValidationThrows_shouldPropagate_andNotRecalcNotSave() {
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        doThrow(new BusinessException(ErrorCode.POLICY_NOT_ACTIVATABLE))
                .when(policyFacade).validateActivateOrThrow(policy);

        assertThrows(BusinessException.class, () -> policyService.activatePolicy(id));

        verifyNoInteractions(policyCalculationService);
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    // --------------------- CANCEL ---------------------

    @Test
    void cancelPolicy_happyPath_shouldValidate_setFields_save_andReturnResponse() {
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setFinalPremiumAmount(new BigDecimal("130.00"));
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));

        PolicyCancelRequestDTO req = new PolicyCancelRequestDTO("Client request");

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.cancelPolicy(id, req);
        assertSame(response, res);

        verify(policyFacade).validateCancelOrThrow(policy);

        verify(policyRepository).save(policyCaptor.capture());
        Policy arg = policyCaptor.getValue();

        assertAll(
                () -> assertEquals(PolicyStatus.CANCELLED, arg.getStatus()),
                () -> assertEquals(FIXED_NOW, arg.getCancelledAt()),
                () -> assertEquals("Client request", arg.getCancellationReason())
        );

        verify(policyMapper).toResponse(any(Policy.class), argThat(cr ->
                cr != null && new BigDecimal("130.00").compareTo(cr.finalPremium()) == 0
        ));
    }

    @Test
    void cancelPolicy_whenOutOfRange_afterEndExclusive_shouldThrow_andNotSave() {
        Clock lateClock = Clock.fixed(Instant.parse("2026-02-04T00:00:01Z"), ZoneOffset.UTC);

        PolicyService svcLate = new PolicyService(
                policyRepository, policyMapper, policyFacade, policyCalculationService, policyNumberGenerator, lateClock
        );

        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 2, 3));

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> svcLate.cancelPolicy(id, new PolicyCancelRequestDTO("x")));

        assertHasError(ex, ErrorCode.POLICY_CANCELLATION_DATE_OUT_OF_RANGE);

        verify(policyFacade).validateCancelOrThrow(policy);
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void cancelPolicy_whenNotFound_shouldThrow_andStop() {
        Long id = 404L;
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.cancelPolicy(id, new PolicyCancelRequestDTO("x")));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verifyNoInteractions(policyFacade, policyMapper);
    }

    // --------------------- GET  ---------------------

    @Test
    void listPolicies_shouldCallRepositoryWithSpecAndPageable_andMapResults() {
        PolicyListFilterDTO filter = new PolicyListFilterDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());

        Policy p1 = aDraftPolicyWithAllMandatoryFields(1L);
        Policy p2 = aDraftPolicyWithAllMandatoryFields(2L);

        Page<Policy> repoPage = new PageImpl<>(List.of(p1, p2), pageable, 2);
        when(policyRepository.findAll(ArgumentMatchers.<Specification<Policy>>any(), eq(pageable))).thenReturn(repoPage);

        PolicyListItemDTO dto1 = new PolicyListItemDTO(1L, "POL-1", PolicyStatus.DRAFT, null, "RON", "c1", "addr1", p1.getStartDate(), p1.getEndDate());
        PolicyListItemDTO dto2 = new PolicyListItemDTO(2L, "POL-2", PolicyStatus.DRAFT, null, "RON", "c2", "addr2", p2.getStartDate(), p2.getEndDate());
        when(policyMapper.toListItem(p1)).thenReturn(dto1);
        when(policyMapper.toListItem(p2)).thenReturn(dto2);

        Page<PolicyListItemDTO> res = policyService.listPolicies(filter, pageable);

        verify(policyRepository).findAll(specCaptor.capture(), eq(pageable));
        assertNotNull(specCaptor.getValue());

        assertEquals(2, res.getContent().size());
        assertSame(dto1, res.getContent().get(0));
        assertSame(dto2, res.getContent().get(1));
    }

    @Test
    void getPolicyDetails_shouldLoadPolicy_andReturnDetailsMappedResponse() {
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);

        PolicyDetailsResponseDTO expected = mock(PolicyDetailsResponseDTO.class);
        when(policyRepository.findDetailsById(id)).thenReturn(Optional.of(policy));
        when(policyMapper.toDetailsResponse(policy)).thenReturn(expected);

        PolicyDetailsResponseDTO response = policyService.getPolicyDetails(id);

        assertSame(expected, response);

        verify(policyRepository).findDetailsById(id);
        verify(policyMapper).toDetailsResponse(policy);
    }

    @Test
    void getPolicyDetails_whenNotFound_shouldThrowPolicyNotFound() {
        Long id = 404L;
        when(policyRepository.findDetailsById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.getPolicyDetails(id));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verify(policyRepository).findDetailsById(id);
        verifyNoInteractions(policyMapper);
    }
}
