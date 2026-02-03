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
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.*;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientWithId;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyMapper policyMapper;
    @Mock
    private PolicyFacade policyFacade;
    @Mock
    private IPolicyCalculationService policyCalculationService;
    @Mock
    private PolicyNumberGenerator policyNumberGenerator;

    @InjectMocks
    private PolicyService policyService;

    @Captor
    private ArgumentCaptor<PolicyCalculationRequestDTO> calcReqCaptor;

    @Captor
    private ArgumentCaptor<PolicyCalculationResponseDTO> calcRespCaptor;

    @Captor
    private ArgumentCaptor<Policy> policyCaptor;

    @Captor
    private ArgumentCaptor<Specification<Policy>> specCaptor;


    // --------------------- CREATE ---------------------


    @Test
    void createDraftPolicy_shouldResolveContext_map_calculate_setFields_save_andReturnResponse() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());

        var policyCreateContext = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        Policy entity = aPolicyEntityDraft();
        Policy saved = new Policy();
        saved.setId(99L);

        PolicyResponseDTO response = aPolicyResponse();

        when(policyFacade.resolveCreateContext(request)).thenReturn(policyCreateContext);
        when(policyMapper.toEntity(request, client, building, broker, currency)).thenReturn(entity);

        when(policyCalculationService.calculatePremium(any(PolicyCalculationRequestDTO.class)))
                .thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));

        when(policyNumberGenerator.generatePolicyNumber()).thenReturn("POL-20260202-AAAAAAAAAA");
        when(policyRepository.save(any(Policy.class))).thenReturn(saved);
        when(policyMapper.toResponse(eq(saved), any(PolicyCalculationResponseDTO.class)))
                .thenReturn(response);


        // Act
        PolicyResponseDTO res = policyService.createDraftPolicy(request);

        // Assert
        assertSame(response, res);

        verify(policyFacade).resolveCreateContext(request);
        verify(policyMapper).toEntity(request, client, building, broker, currency);

        verify(policyCalculationService).calculatePremium(calcReqCaptor.capture());
        PolicyCalculationRequestDTO calcReq = calcReqCaptor.getValue();

        assertAll(
                () -> assertEquals(request.basePremiumAmount(), calcReq.basePremium()),
                () -> assertEquals(request.buildingId(), calcReq.buildingId()),
                () -> assertEquals(request.currencyId(), calcReq.currencyId()),
                () -> assertEquals(LocalDate.now(), calcReq.now())
        );

        verify(policyNumberGenerator).generatePolicyNumber();

        verify(policyRepository).save(policyCaptor.capture());
        Policy savedArg = policyCaptor.getValue();

        assertAll(
                () -> assertEquals(new BigDecimal("130.00"), savedArg.getFinalPremiumAmount()),
                () -> assertEquals("POL-20260202-AAAAAAAAAA", savedArg.getPolicyNumber())
        );

        verify(policyMapper).toResponse(eq(saved), any(PolicyCalculationResponseDTO.class));
        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade, policyCalculationService, policyNumberGenerator);
    }

    @Test
    void createDraftPolicy_whenCalculationThrows_shouldPropagate_andNotSave() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());
        var policyCreateContext = new PolicyFacade.PolicyCreateContext(client, broker, building, currency);

        Policy entity = aPolicyEntityDraft();

        when(policyFacade.resolveCreateContext(request)).thenReturn(policyCreateContext);
        when(policyMapper.toEntity(request, client, building, broker, currency)).thenReturn(entity);
        when(policyCalculationService.calculatePremium(any())).thenThrow(new RuntimeException("boom"));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> policyService.createDraftPolicy(request));

        verify(policyFacade).resolveCreateContext(request);
        verify(policyMapper).toEntity(request, client, building, broker, currency);
        verify(policyCalculationService).calculatePremium(any());

        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(),any());
    }

    // --------------------- UPDATE ---------------------

    @Test
    void activatePolicy_happyPath_shouldValidate_recalc_applyActivation_save_andReturnResponse() {
        // Arrange
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        Policy saved = new Policy();
        PolicyResponseDTO response = mock(PolicyResponseDTO.class);

        PolicyActivateRequestDTO req = new PolicyActivateRequestDTO(OffsetDateTime.parse("2026-02-02T10:00:00Z"));

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyCalculationService.calculatePremium(any()))
                .thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));
        when(policyRepository.save(any())).thenReturn(saved);
        when(policyMapper.toResponse(eq(saved), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        // Act
        PolicyResponseDTO res = policyService.activatePolicy(id, req);

        // Assert
        assertSame(response, res);

        verify(policyRepository).findById(id);
        verify(policyFacade).validateActivateOrThrow(policy, req);

        verify(policyCalculationService).calculatePremium(calcReqCaptor.capture());
        PolicyCalculationRequestDTO calcReq = calcReqCaptor.getValue();
        assertAll(
                () -> assertEquals(new BigDecimal("100.00"), calcReq.basePremium()),
                () -> assertEquals(policy.getBuilding().getId(), calcReq.buildingId()),
                () -> assertEquals(policy.getCurrency().getId(), calcReq.currencyId()),
                () -> assertEquals(LocalDate.now(), calcReq.now())
        );

        verify(policyMapper).applyActivation(req, policy);

        verify(policyRepository).save(policyCaptor.capture());
        Policy savedArg = policyCaptor.getValue();

        assertEquals(new BigDecimal("130.00"), savedArg.getFinalPremiumAmount());
        assertEquals(new BigDecimal("10.00"), savedArg.getBrokerCommissionPct());
        assertEquals(new BigDecimal("13.00"), savedArg.getBrokerCommissionAmount()); // 130 * 10% = 13.00

        assertNotNull(savedArg.getActivatedAt());

        verify(policyMapper).toResponse(eq(saved), any(PolicyCalculationResponseDTO.class));
        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade, policyCalculationService);
    }

    @Test
    void activatePolicy_whenNotFound_shouldThrow_andDoNothingElse() {
        // Arrange
        Long id = 404L;
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyService.activatePolicy(id, null));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_NOT_FOUND.name().equals(e.getCode())));

        verify(policyRepository).findById(id);
        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade, policyCalculationService);
    }

    @Test
    void activatePolicy_whenFacadeValidationThrows_shouldPropagate_andNotRecalcNotSave() {
        // Arrange
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));

        doThrow(new BusinessException(ErrorCode.POLICY_NOT_ACTIVATABLE)).when(policyFacade).validateActivateOrThrow(eq(policy), any());

        // Act + Assert
        assertThrows(BusinessException.class, () -> policyService.activatePolicy(id, null));

        verify(policyRepository).findById(id);
        verify(policyFacade).validateActivateOrThrow(eq(policy), isNull());

        verifyNoInteractions(policyCalculationService);
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).applyActivation(any(), any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void activatePolicy_whenMapperDoesNotSetActivatedAt_shouldSetNow() {
        // Arrange
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        Policy saved = new Policy();

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyCalculationService.calculatePremium(any())).thenReturn(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), new BigDecimal("30"), List.of()));
        when(policyRepository.save(any())).thenReturn(saved);
        when(policyMapper.toResponse(eq(saved), any(PolicyCalculationResponseDTO.class))).thenReturn(mock(PolicyResponseDTO.class));

        // Act
        policyService.activatePolicy(id, new PolicyActivateRequestDTO(null));

        // Assert
        assertNotNull(policy.getActivatedAt());
        assertEquals(0, policy.getBrokerCommissionPct().compareTo(new BigDecimal("10.00")));
        assertEquals(0, policy.getBrokerCommissionAmount().compareTo(new BigDecimal("13.00")));

        verify(policyMapper).toResponse(eq(saved), any(PolicyCalculationResponseDTO.class));
    }

    @Test
    void cancelPolicy_happyPath_withProvidedCancelledAt_shouldValidate_applyCancel_save_andReturnResponse() {
        // Arrange
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));
        policy.setFinalPremiumAmount(new BigDecimal("130.00"));

        OffsetDateTime cancelledAt = OffsetDateTime.parse("2026-02-02T10:00:00Z");
        PolicyCancelRequestDTO req = new PolicyCancelRequestDTO("Client request", cancelledAt);

        Policy saved = new Policy();
        saved.setId(id);
        saved.setStatus(PolicyStatus.CANCELLED);
        saved.setCancelledAt(cancelledAt);

        PolicyResponseDTO response = mock(PolicyResponseDTO.class);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(saved);
        when(policyMapper.toResponse(eq(saved), any(PolicyCalculationResponseDTO.class))).thenReturn(response);

        // Act
        PolicyResponseDTO res = policyService.cancelPolicy(id, req);

        // Assert
        assertSame(response, res);

        verify(policyRepository).findById(id);
        verify(policyFacade).validateCancelOrThrow(policy, req);

        verify(policyMapper).applyCancel(eq(req), eq(cancelledAt), eq(policy));

        verify(policyRepository).save(policyCaptor.capture());
        assertSame(policy, policyCaptor.getValue());

        verify(policyMapper).toResponse(eq(saved), argThat(cr ->
                cr != null && new BigDecimal("130.00").compareTo(cr.finalPremium()) == 0
        ));

        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade);
    }

    @Test
    void cancelPolicy_happyPath_whenCancelledAtNull_shouldUseNow_andApplyCancelWithComputedDate() {
        // Arrange
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));
        policy.setFinalPremiumAmount(new BigDecimal("130.00"));

        PolicyCancelRequestDTO req = new PolicyCancelRequestDTO("Client request", null);

        Policy saved = new Policy();
        saved.setId(id);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(saved);
        when(policyMapper.toResponse(eq(saved), any(PolicyCalculationResponseDTO.class))).thenReturn(mock(PolicyResponseDTO.class));

        ArgumentCaptor<OffsetDateTime> cancelledAtCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        OffsetDateTime before = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Act
        policyService.cancelPolicy(id, req);

        OffsetDateTime after = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Assert
        verify(policyFacade).validateCancelOrThrow(policy, req);

        verify(policyMapper).applyCancel(eq(req), cancelledAtCaptor.capture(), eq(policy));
        OffsetDateTime used = cancelledAtCaptor.getValue().truncatedTo(ChronoUnit.SECONDS);

        // used should be between before and after
        assertFalse(used.isBefore(before));
        assertFalse(used.isAfter(after));

        verify(policyRepository).save(any());
        verify(policyMapper).toResponse(eq(saved), any(PolicyCalculationResponseDTO.class));
    }

    @Test
    void cancelPolicy_whenCancelledAtOutOfRange_shouldThrow_andNotApplyCancelNotSaveNotMapToResponse() {
        // Arrange
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));

        // out of range: before start
        OffsetDateTime cancelledAt = OffsetDateTime.of(2025, 12, 31, 23, 0, 0, 0, ZoneOffset.UTC);
        PolicyCancelRequestDTO req = new PolicyCancelRequestDTO("Client request", cancelledAt);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyService.cancelPolicy(id, req));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_CANCELLATION_DATE_OUT_OF_RANGE.name().equals(e.getCode())));

        verify(policyRepository).findById(id);
        verify(policyFacade).validateCancelOrThrow(policy, req);

        verify(policyMapper, never()).applyCancel(any(), any(), any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void cancelPolicy_whenFacadeValidationThrows_shouldPropagate_andNotApplyCancelNotSave() {
        // Arrange
        Long id = 10L;

        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));

        PolicyCancelRequestDTO req = new PolicyCancelRequestDTO("Client request", OffsetDateTime.parse("2026-02-02T10:00:00Z"));

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        doThrow(new BusinessException(ErrorCode.POLICY_NOT_CANCELLABLE))
                .when(policyFacade).validateCancelOrThrow(policy, req);

        // Act + Assert
        assertThrows(BusinessException.class, () -> policyService.cancelPolicy(id, req));

        verify(policyRepository).findById(id);
        verify(policyFacade).validateCancelOrThrow(policy, req);

        verify(policyMapper, never()).applyCancel(any(), any(), any());
        verify(policyRepository, never()).save(any());
        verify(policyMapper, never()).toResponse(any(), any());
    }

    @Test
    void cancelPolicy_whenNotFound_shouldThrow_andDoNothingElse() {
        // Arrange
        Long id = 404L;
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyService.cancelPolicy(id, new PolicyCancelRequestDTO("x", null)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_NOT_FOUND.name().equals(e.getCode())));

        verify(policyRepository).findById(id);
        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade, policyCalculationService, policyNumberGenerator);
    }

    // --------------------- GET ---------------------


    @Test
    void listPolicies_shouldCallRepositoryWithSpecAndPageable_andMapResults() {
        // Arrange
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

        // Act
        Page<PolicyListItemDTO> res = policyService.listPolicies(filter, pageable);

        // Assert
        verify(policyRepository).findAll(specCaptor.capture(), eq(pageable));
        assertNotNull(specCaptor.getValue());

        verify(policyMapper).toListItem(p1);
        verify(policyMapper).toListItem(p2);

        assertEquals(2, res.getContent().size());
        assertSame(dto1, res.getContent().get(0));
        assertSame(dto2, res.getContent().get(1));
        assertEquals(2, res.getTotalElements());
        assertEquals(1, res.getTotalPages());
    }

    @Test
    void listPolicies_withFilters_shouldCallRepository_andMapResults() {
        // Arrange
        PolicyListFilterDTO filter = new PolicyListFilterDTO(
                1L,
                2L,
                PolicyStatus.ACTIVE,
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 12, 31)
        );

        Pageable pageable = PageRequest.of(1, 10);
        Policy p = aDraftPolicyWithAllMandatoryFields(10L);

        Page<Policy> repoPage = new PageImpl<>(List.of(p), pageable, 11);

        when(policyRepository.findAll(ArgumentMatchers.<Specification<Policy>>any(), eq(pageable)))
                .thenReturn(repoPage);

        PolicyListItemDTO policyListItemDTO = new PolicyListItemDTO(10L, "POL-10", PolicyStatus.ACTIVE, null, "RON", "c", "addr", p.getStartDate(), p.getEndDate());
        when(policyMapper.toListItem(p)).thenReturn(policyListItemDTO);

        // Act
        Page<PolicyListItemDTO> result = policyService.listPolicies(filter, pageable);

        // Assert
        verify(policyRepository).findAll(Mockito.<Specification<Policy>>any(), eq(pageable));
        verify(policyMapper).toListItem(p);

        assertEquals(1, result.getNumberOfElements());
        assertEquals(11, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertSame(policyListItemDTO, result.getContent().getFirst());
    }

    @Test
    void listPolicies_whenEmptyPage_shouldReturnEmpty_andNotCallMapper() {
        // Arrange
        PolicyListFilterDTO filter = new PolicyListFilterDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        Page<Policy> empty = Page.empty(pageable);

        when(policyRepository.findAll(Mockito.<Specification<Policy>>any(), eq(pageable))).thenReturn(empty);

        // Act
        Page<PolicyListItemDTO> result = policyService.listPolicies(filter, pageable);

        // Assert
        verify(policyRepository).findAll(Mockito.<Specification<Policy>>any(), eq(pageable));
        verify(policyMapper, never()).toListItem(any());

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getPolicyDetails_shouldLoadPolicy_buildCalcResponse_fromFinalPremium_andReturnMappedResponse() {
        // Arrange
        Long id = 10L;
        Policy policy = aDraftPolicyWithAllMandatoryFields(id);
        policy.setFinalPremiumAmount(new BigDecimal("130.00"));

        PolicyResponseDTO expected = mock(PolicyResponseDTO.class);

        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policyMapper.toResponse(eq(policy), any(PolicyCalculationResponseDTO.class))).thenReturn(expected);

        // Act
        PolicyResponseDTO res = policyService.getPolicyDetails(id);

        // Assert
        assertSame(expected, res);

        verify(policyRepository).findById(id);
        verify(policyMapper).toResponse(eq(policy), calcRespCaptor.capture());

        PolicyCalculationResponseDTO captured = calcRespCaptor.getValue();
        assertEquals(new BigDecimal("130.00"), captured.finalPremium());
        assertEquals(new PolicyCalculationResponseDTO(new BigDecimal("130.00"), null, null), captured);

        verifyNoMoreInteractions(policyRepository, policyMapper, policyFacade, policyCalculationService, policyNumberGenerator);
    }



}