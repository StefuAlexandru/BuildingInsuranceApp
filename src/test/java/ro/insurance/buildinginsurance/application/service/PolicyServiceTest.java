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
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BrokerRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;
import ro.insurance.buildinginsurance.util.PolicyNumberGenerator;
import ro.insurance.buildinginsurance.web.dto.policy_calculation_dto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policy_calculation_dto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.*;
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
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private BrokerRepository brokerRepository;
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

    // --------------------- CREATE ---------------------

    @Test
    void createDraftPolicy_shouldResolveContext_map_calculate_setFields_save_andReturnResponse() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Long brokerId = 1L;

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(brokerId);
        Currency currency = aCurrencyWithId(request.currencyId());

        var ctx = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        Policy entity = aPolicyEntityDraft();
        PolicyResponseDTO response = aPolicyResponse();

        when(policyFacade.resolveCreateContext(request,broker)).thenReturn(ctx);
        when(policyMapper.toEntity(request, client, building, broker, currency)).thenReturn(entity);
        when(brokerRepository.findByEmailIgnoreCase(broker.getEmail())).thenReturn(Optional.of(broker));
        when(policyCalculationService.calculatePremium(any())).thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));
        when(policyNumberGenerator.generatePolicyNumber()).thenReturn("POL-20260202-AAAAAAAAAA");
        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.createDraftPolicy(request,broker.getEmail());

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
        Long brokerId = 1L;

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(brokerId);
        Currency currency = aCurrencyWithId(request.currencyId());
        var ctx = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        when(brokerRepository.findByEmailIgnoreCase(broker.getEmail())).thenReturn(Optional.of(broker));
        when(policyFacade.resolveCreateContext(any(),any())).thenReturn(ctx);
        when(policyMapper.toEntity(any(), any(), any(), any(), any())).thenReturn(aPolicyEntityDraft());

        when(policyCalculationService.calculatePremium(any())).thenThrow(new RuntimeException("boom"));

        String brokerEmail = broker.getEmail();
        assertThrows(RuntimeException.class, () -> policyService.createDraftPolicy(request,brokerEmail));

        verify(policyCalculationService).calculatePremium(any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
        verify(policyNumberGenerator, never()).generatePolicyNumber();
    }

    // --------------------- ACTIVATE ---------------------

    @Test
    void activatePolicy_happyPath_shouldValidate_recalc_setFields_save_andReturnResponse() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);
        policy.getBroker().setCommissionPct(new BigDecimal("10.00"));

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));
        when(policyCalculationService.calculatePremium(any()))
                .thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));

        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.activatePolicy(policyId,brokerEmail);

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

    // --------------------- ACTIVATE ---------------------

    @Test
    void activatePolicy_whenNotFound_shouldThrow_andStop() {
        Long policyId = 404L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.activatePolicy(policyId, brokerEmail));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verifyNoInteractions(policyFacade, policyCalculationService, policyMapper);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void activatePolicy_whenFacadeValidationThrows_shouldPropagate_andNotRecalcNotSave() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);
        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));

        doThrow(new BusinessException(ErrorCode.POLICY_NOT_ACTIVATABLE))
                .when(policyFacade).validateActivateOrThrow(policy);

        assertThrows(BusinessException.class, () -> policyService.activatePolicy(policyId, brokerEmail));

        verifyNoInteractions(policyCalculationService);
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    // (optional, but recommended)
    @Test
    void activatePolicy_whenBrokerNotFound_shouldThrow_andStop() {
        Long policyId = 10L;
        String brokerEmail = "Missing Broker";

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.activatePolicy(policyId, brokerEmail));

        assertHasError(ex, ErrorCode.BROKER_NOT_FOUND);

        verifyNoInteractions(policyRepository, policyFacade, policyCalculationService, policyMapper);
    }


    // --------------------- CANCEL ---------------------

    @Test
    void cancelPolicy_happyPath_shouldValidate_setFields_save_andReturnResponse() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setFinalPremiumAmount(new BigDecimal("130.00"));
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));

        PolicyCancelRequestDTO request = new PolicyCancelRequestDTO("Client request");

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.cancelPolicy(policyId, request, brokerEmail);
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
                policyRepository, brokerRepository, policyMapper,
                policyFacade, policyCalculationService, policyNumberGenerator, lateClock
        );

        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 2, 3));

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));

        PolicyCancelRequestDTO request = new PolicyCancelRequestDTO("x");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> svcLate.cancelPolicy(policyId, request, brokerEmail));

        assertHasError(ex, ErrorCode.POLICY_CANCELLATION_DATE_OUT_OF_RANGE);

        verify(policyFacade).validateCancelOrThrow(policy);
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void cancelPolicy_whenNotFound_shouldThrow_andStop() {
        Long policyId = 404L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.empty());

        PolicyCancelRequestDTO request = new PolicyCancelRequestDTO("x");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.cancelPolicy(policyId, request, brokerEmail));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verifyNoInteractions(policyFacade, policyMapper);
    }


    // --------------------- GET ---------------------

    @Test
    void listPolicies_shouldCallRepositoryWithSpecAndPageable_andMapResults() {
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        PolicyListFilterDTO filter = new PolicyListFilterDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());

        Policy p1 = aDraftPolicyWithAllMandatoryFields(1L);
        Policy p2 = aDraftPolicyWithAllMandatoryFields(2L);

        Page<Policy> repoPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findAll(ArgumentMatchers.<Specification<Policy>>any(), eq(pageable))).thenReturn(repoPage);

        PolicyListItemDTO dto1 = new PolicyListItemDTO(1L, "POL-1", PolicyStatus.DRAFT, null, "RON", "c1", "addr1", p1.getStartDate(), p1.getEndDate());
        PolicyListItemDTO dto2 = new PolicyListItemDTO(2L, "POL-2", PolicyStatus.DRAFT, null, "RON", "c2", "addr2", p2.getStartDate(), p2.getEndDate());

        when(policyMapper.toListItem(p1)).thenReturn(dto1);
        when(policyMapper.toListItem(p2)).thenReturn(dto2);

        Page<PolicyListItemDTO> res = policyService.listPolicies(filter, pageable, brokerEmail);

        verify(policyRepository).findAll(specCaptor.capture(), eq(pageable));
        assertNotNull(specCaptor.getValue());

        assertEquals(2, res.getContent().size());
        assertSame(dto1, res.getContent().get(0));
        assertSame(dto2, res.getContent().get(1));
    }

    @Test
    void getPolicyDetails_shouldLoadPolicy_andReturnDetailsMappedResponse() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);
        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);

        PolicyDetailsResponseDTO expected = mock(PolicyDetailsResponseDTO.class);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findDetailsByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));
        when(policyMapper.toDetailsResponse(policy)).thenReturn(expected);

        PolicyDetailsResponseDTO response = policyService.getPolicyDetails(policyId, brokerEmail);

        assertSame(expected, response);

        verify(policyRepository).findDetailsByIdAndBrokerId(policyId, brokerId);
        verify(policyMapper).toDetailsResponse(policy);
    }

    @Test
    void getPolicyDetails_whenNotFound_shouldThrowPolicyNotFound() {
        Long policyId = 404L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findDetailsByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.getPolicyDetails(policyId, brokerEmail));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verify(policyRepository).findDetailsByIdAndBrokerId(policyId, brokerId);
        verifyNoInteractions(policyMapper);
    }

    // --------------------- PATCH DRAFT ---------------------

    @Test
    void patchDraftPolicy_happyPath_shouldResolveBroker_loadPolicy_validate_patch_recalc_save_andReturnResponse() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newBuildingId = 77L;
        Long newCurrencyId = 55L;
        Building newBuilding = aBuildingWithId(newBuildingId);
        Currency newCurrency = aCurrencyWithId(newCurrencyId);

        LocalDate newStart = LocalDate.of(2026, 2, 10);
        LocalDate newEnd = LocalDate.of(2026, 3, 10);
        BigDecimal newBase = new BigDecimal("150.00");

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                newBuildingId,
                newCurrencyId,
                newStart,
                newEnd,
                newBase
        );

        var ctx = new PolicyFacade.PolicyPatchContext(newBuilding, newCurrency);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));
        when(policyFacade.validatePatchOrThrow(policy, req)).thenReturn(ctx);

        doAnswer(inv -> {
            PolicyPatchRequestDTO request = inv.getArgument(0);
            Building building = inv.getArgument(1);
            Currency currency = inv.getArgument(2);
            Policy pol = inv.getArgument(3);

            if (request.basePremiumAmount() != null) pol.setBasePremiumAmount(request.basePremiumAmount());
            if (request.startDate() != null) pol.setStartDate(request.startDate());
            if (request.endDate() != null) pol.setEndDate(request.endDate());
            pol.setBuilding(building);
            pol.setCurrency(currency);
            return null;
        }).when(policyMapper).patchDraftFromDto(any(), any(), any(), any());

        PolicyCalculationResponseDTO calcResp = new PolicyCalculationResponseDTO(new BigDecimal("200.00"), new BigDecimal("30"), List.of());
        when(policyCalculationService.calculatePremium(any())).thenReturn(calcResp);

        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);
        when(policyMapper.toResponse(any(Policy.class), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        PolicyResponseDTO res = policyService.patchDraftPolicy(policyId, req, brokerEmail);

        assertSame(response, res);

        verify(brokerRepository).findByEmailIgnoreCase(brokerEmail);
        verify(policyRepository).findByIdAndBrokerId(policyId, brokerId);

        verify(policyFacade).validatePatchOrThrow(policy, req);
        verify(policyMapper).patchDraftFromDto(req, newBuilding, newCurrency, policy);

        verify(policyCalculationService).calculatePremium(calcReqCaptor.capture());
        PolicyCalculationRequestDTO calcReq = calcReqCaptor.getValue();

        assertAll(
                () -> assertEquals(newBase, calcReq.basePremium()),
                () -> assertEquals(newBuildingId, calcReq.buildingId()),
                () -> assertEquals(newCurrencyId, calcReq.currencyId()),
                () -> assertEquals(newStart, calcReq.now())
        );

        verify(policyRepository).save(policyCaptor.capture());
        Policy saved = policyCaptor.getValue();

        assertAll(
                () -> assertEquals(new BigDecimal("200.00"), saved.getFinalPremiumAmount()),
                () -> assertSame(newBuilding, saved.getBuilding()),
                () -> assertSame(newCurrency, saved.getCurrency()),
                () -> assertEquals(newStart, saved.getStartDate()),
                () -> assertEquals(newEnd, saved.getEndDate()),
                () -> assertEquals(newBase, saved.getBasePremiumAmount())
        );

        verify(policyMapper).toResponse(saved, calcResp);
    }

    @Test
    void patchDraftPolicy_whenBrokerNotFound_shouldThrow_andStop() {
        Long policyId = 10L;
        String brokerEmail = "missing@email.com";

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.empty());

        PolicyPatchRequestDTO request =  new PolicyPatchRequestDTO(null, null, null, null, null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.patchDraftPolicy(policyId,request, brokerEmail));

        assertHasError(ex, ErrorCode.BROKER_NOT_FOUND);

        verifyNoInteractions(policyRepository, policyFacade, policyCalculationService, policyMapper);
    }

    @Test
    void patchDraftPolicy_whenPolicyNotFound_shouldThrow_andStop() {
        Long policyId = 404L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.empty());

        PolicyPatchRequestDTO request =  new PolicyPatchRequestDTO(null, null, null, null, null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.patchDraftPolicy(policyId, request, brokerEmail));

        assertHasError(ex, ErrorCode.POLICY_NOT_FOUND);

        verifyNoInteractions(policyFacade, policyCalculationService, policyMapper);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void patchDraftPolicy_whenFacadeValidationThrows_shouldPropagate_andNotPatchNotRecalcNotSave() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);
        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(77L, null, null, null, null);

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));

        doThrow(new BusinessException(ErrorCode.POLICY_NOT_DRAFT)).when(policyFacade).validatePatchOrThrow(policy, req);

        assertThrows(BusinessException.class, () -> policyService.patchDraftPolicy(policyId, req, brokerEmail));

        verify(policyFacade).validatePatchOrThrow(policy, req);

        verifyNoInteractions(policyCalculationService);
        verify(policyMapper, never()).patchDraftFromDto(any(), any(), any(), any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void patchDraftPolicy_whenCalculationThrows_shouldPropagate_andNotSaveNotReturnResponse() {
        Long policyId = 10L;
        Long brokerId = 99L;
        String brokerEmail = "broker@email.com";

        Broker broker = aBrokerWithId(brokerId);
        Policy policy = aDraftPolicyWithAllMandatoryFields(policyId);

        Building building = aBuildingWithId(77L);
        Currency currency = aCurrencyWithId(55L);
        var ctx = new PolicyFacade.PolicyPatchContext(building, currency);

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(77L, 55L, LocalDate.of(2026, 2, 10), null, new BigDecimal("100.00"));

        when(brokerRepository.findByEmailIgnoreCase(brokerEmail)).thenReturn(Optional.of(broker));
        when(policyRepository.findByIdAndBrokerId(policyId, brokerId)).thenReturn(Optional.of(policy));
        when(policyFacade.validatePatchOrThrow(policy, req)).thenReturn(ctx);

        doAnswer(inv -> {
            Policy p = inv.getArgument(3);
            p.setBuilding(inv.getArgument(1));
            p.setCurrency(inv.getArgument(2));
            p.setStartDate(req.startDate());
            p.setBasePremiumAmount(req.basePremiumAmount());
            return null;
        }).when(policyMapper).patchDraftFromDto(any(), any(), any(), any());

        when(policyCalculationService.calculatePremium(any())).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> policyService.patchDraftPolicy(policyId, req, brokerEmail));

        verify(policyCalculationService).calculatePremium(any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

}
