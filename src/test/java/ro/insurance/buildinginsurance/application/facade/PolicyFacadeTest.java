package ro.insurance.buildinginsurance.application.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyCreateRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyPatchRequestDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.aDraftPolicyWithAllMandatoryFields;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.aPolicyCreateRequest;

@ExtendWith(MockitoExtension.class)
class PolicyFacadeTest {

    @Mock private ClientRepository clientRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private BrokerRepository brokerRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private Clock clock;

    @InjectMocks
    private PolicyFacade policyFacade;

    private LocalDate stubClockUtcAndGetToday() {
        Instant instant = Instant.parse("2026-02-04T10:00:00Z");
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(instant);
        return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    }

    static Stream<Arguments> missingMandatoryPolicyMutations() {
        return Stream.of(
                Arguments.of((Consumer<Policy>) p -> p.setClient(null)),
                Arguments.of((Consumer<Policy>) p -> p.setBuilding(null)),
                Arguments.of((Consumer<Policy>) p -> p.setBroker(null)),
                Arguments.of((Consumer<Policy>) p -> p.setCurrency(null)),
                Arguments.of((Consumer<Policy>) p -> p.setEndDate(null)),
                Arguments.of((Consumer<Policy>) p -> p.setBasePremiumAmount(null))
        );
    }

    // ---------------- resolveCreateContext ----------------

    @Test
    void resolveCreateContext_happyPath_shouldLoadAll_validateOwnership_currency_dates_andReturnContext() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Long brokerId = 1L;

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(brokerId);

        Currency currency = aCurrencyWithId(request.currencyId());

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        PolicyFacade.PolicyCreateContext ctx = policyFacade.resolveCreateContext(request,broker);

        assertAll(
                () -> assertSame(client, ctx.client()),
                () -> assertSame(building, ctx.building()),
                () -> assertSame(broker, ctx.broker()),
                () -> assertSame(currency, ctx.currency())
        );

        verify(buildingRepository).existsByIdAndClientId(request.buildingId(), request.clientId());
    }

    @Test
    void resolveCreateContext_whenBuildingNotOwned_shouldThrow() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Broker broker = aBrokerWithId(1L);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.BUILDING_NOT_OWNED_BY_CLIENT);

        verify(brokerRepository, never()).findById(any());
        verify(currencyRepository, never()).findById(any());
    }

    @Test
    void resolveCreateContext_whenCurrencyInactive_shouldThrow() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Long brokerId = 1L;

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(brokerId);

        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(false);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.CURRENCY_IS_NOT_ACTIVE);
    }

    @Test
    void resolveCreateContext_whenStartAfterEnd_shouldThrow() {
        PolicyCreateRequestDTO request = new PolicyCreateRequestDTO(
                1L, 1L, 1L,
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                new BigDecimal("100.00")
        );

        Long brokerId = 1L;

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(brokerId);

        Currency currency = aCurrencyWithId(request.currencyId());

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.INVALID_DATES);
    }

    @Test
    void resolveCreateContext_whenClientNotFound_shouldThrow_andStop() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Broker broker = aBrokerWithId(1L);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.CLIENT_NOT_FOUND);

        verifyNoInteractions(buildingRepository, brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenBuildingNotFound_shouldThrow_andStop() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Broker broker = aBrokerWithId(1L);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(buildingRepository, never()).existsByIdAndClientId(anyLong(), anyLong());
        verifyNoInteractions(brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenCurrencyNotFound_shouldThrow() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Long brokerId = 1L;
        Broker broker = aBrokerWithId(brokerId);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.CURRENCY_NOT_FOUND);
    }

    @Test
    void resolveCreateContext_whenBrokerInactive_shouldThrow_andStopBeforeCurrencyLoad() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        Long brokerId = 1L;
        Broker broker = aBrokerWithId(brokerId);
        broker.setActive(false);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request,broker));
        assertHasError(ex, ErrorCode.BROKER_IS_NOT_ACTIVE);

        verify(currencyRepository, never()).findById(anyLong());
    }

    @Test
    void resolveCreateContext_whenStartEqualsEnd_shouldNotThrow() {
        LocalDate date = LocalDate.of(2026, 2, 10);
        PolicyCreateRequestDTO request = new PolicyCreateRequestDTO(
                1L, 1L, 1L,
                date, date,
                new BigDecimal("100.00")
        );

        Long brokerId = 1L;

        Broker broker = aBrokerWithId(brokerId);
        Currency currency = aCurrencyWithId(request.currencyId());

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        assertDoesNotThrow(() -> policyFacade.resolveCreateContext(request,broker));
    }

    // ---------------- validateActivateOrThrow ----------------

    @Test
    void validateActivateOrThrow_whenStatusNotDraft_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_NOT_DRAFT);
    }

    @Test
    void validateActivateOrThrow_whenStartDateInPast_shouldThrow() {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today.minusDays(1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_START_DATE_IN_PAST);
    }

    @Test
    void validateActivateOrThrow_whenStartDateIsToday_shouldNotThrow() {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today);
        policy.getBroker().setActive(true);
        policy.getCurrency().setActive(true);

        assertDoesNotThrow(() -> policyFacade.validateActivateOrThrow(policy));
    }

    @Test
    void validateActivateOrThrow_whenBrokerInactive_shouldThrow() {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today.plusDays(1));
        policy.getBroker().setActive(false);
        policy.getCurrency().setActive(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.BROKER_IS_NOT_ACTIVE);
    }

    @Test
    void validateActivateOrThrow_whenCurrencyInactive_shouldThrow() {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today.plusDays(1));
        policy.getBroker().setActive(true);
        policy.getCurrency().setActive(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.CURRENCY_IS_NOT_ACTIVE);
    }

    @Test
    void validateActivateOrThrow_happyPath_shouldNotThrow() {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today.plusDays(1));
        policy.getBroker().setActive(true);
        policy.getCurrency().setActive(true);

        assertDoesNotThrow(() -> policyFacade.validateActivateOrThrow(policy));
    }

    @ParameterizedTest(name = "missing {0} -> POLICY_INVALID_STATE")
    @MethodSource("missingMandatoryPolicyMutations")
    void validateActivateOrThrow_whenAnyMandatoryFieldMissing_shouldThrow(Consumer<Policy> mutate) {
        LocalDate today = stubClockUtcAndGetToday();

        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(today.plusDays(1));
        policy.getBroker().setActive(true);
        policy.getCurrency().setActive(true);

        mutate.accept(policy);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_INVALID_STATE);
    }

    // ---------------- validateCancelOrThrow ----------------

    @Test
    void validateCancelOrThrow_whenNotActive_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateCancelOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_NOT_CANCELLABLE);
    }

    @Test
    void validateCancelOrThrow_whenAlreadyCancelled_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setCancelledAt(OffsetDateTime.parse("2026-02-01T10:00:00Z"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateCancelOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_NOT_CANCELLABLE);
    }

    @Test
    void validateCancelOrThrow_whenActiveAndNotCancelled_shouldNotThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setCancelledAt(null);

        assertDoesNotThrow(() -> policyFacade.validateCancelOrThrow(policy));
    }

    // ---------------- validatePatchOrThrow ----------------

    @Test
    void validatePatchOrThrow_whenStatusNotDraft_shouldThrow() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.ACTIVE);

        PolicyPatchRequestDTO request = new PolicyPatchRequestDTO(
                null, null, null, null, null
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, request));

        assertHasError(ex, ErrorCode.POLICY_NOT_DRAFT);

        verifyNoInteractions(buildingRepository, currencyRepository);
    }

    @Test
    void validatePatchOrThrow_whenNoFieldsProvided_shouldUseExistingBuildingCurrency_andValidateDates() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.of(2026, 2, 10));
        policy.setEndDate(LocalDate.of(2026, 3, 10));

        PolicyPatchRequestDTO request = new PolicyPatchRequestDTO(
                null, null, null, null, null
        );

        PolicyFacade.PolicyPatchContext ctx = policyFacade.validatePatchOrThrow(policy, request);

        assertAll(
                () -> assertSame(policy.getBuilding(), ctx.building()),
                () -> assertSame(policy.getCurrency(), ctx.currency())
        );

        verifyNoInteractions(buildingRepository, currencyRepository);
    }

    @Test
    void validatePatchOrThrow_whenBuildingIdProvided_shouldLoadBuilding_andValidateOwnership_andReturnContext() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.of(2026, 2, 10));
        policy.setEndDate(LocalDate.of(2026, 3, 10));

        Long newBuildingId = 77L;
        Building newBuilding = aBuildingWithId(newBuildingId);

        when(buildingRepository.findById(newBuildingId)).thenReturn(Optional.of(newBuilding));
        when(buildingRepository.existsByIdAndClientId(newBuildingId, policy.getClient().getId())).thenReturn(true);

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                newBuildingId, null, null, null, null
        );

        PolicyFacade.PolicyPatchContext ctx = policyFacade.validatePatchOrThrow(policy, req);

        assertSame(newBuilding, ctx.building());
        assertSame(policy.getCurrency(), ctx.currency());

        verify(buildingRepository).findById(newBuildingId);
        verify(buildingRepository).existsByIdAndClientId(newBuildingId, policy.getClient().getId());
        verifyNoInteractions(currencyRepository);
    }

    @Test
    void validatePatchOrThrow_whenBuildingNotFound_shouldThrow_andStop() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newBuildingId = 77L;

        when(buildingRepository.findById(newBuildingId)).thenReturn(Optional.empty());

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                newBuildingId, null, null, null, null
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(buildingRepository).findById(newBuildingId);
        verify(buildingRepository, never()).existsByIdAndClientId(anyLong(), anyLong());
        verifyNoInteractions(currencyRepository);
    }

    @Test
    void validatePatchOrThrow_whenBuildingNotOwned_shouldThrow() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newBuildingId = 77L;
        Building newBuilding = aBuildingWithId(newBuildingId);

        when(buildingRepository.findById(newBuildingId)).thenReturn(Optional.of(newBuilding));
        when(buildingRepository.existsByIdAndClientId(newBuildingId, policy.getClient().getId())).thenReturn(false);

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                newBuildingId, null, null, null, null
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.BUILDING_NOT_OWNED_BY_CLIENT);

        verify(buildingRepository).findById(newBuildingId);
        verify(buildingRepository).existsByIdAndClientId(newBuildingId, policy.getClient().getId());
        verifyNoInteractions(currencyRepository);
    }

    @Test
    void validatePatchOrThrow_whenCurrencyIdProvidedButBasePremiumMissing_shouldThrow_andNotLoadCurrency() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newCurrencyId = 55L;

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                null, newCurrencyId, null, null, null // basePremiumAmount missing
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.BASE_PREMIUM_REQUIRED_WHEN_CHANGING_CURRENCY);

        verifyNoInteractions(currencyRepository);
        verifyNoInteractions(buildingRepository);
    }

    @Test
    void validatePatchOrThrow_whenCurrencyNotFound_shouldThrow() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newCurrencyId = 55L;

        when(currencyRepository.findById(newCurrencyId)).thenReturn(Optional.empty());

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                null, newCurrencyId, null, null, new BigDecimal("100.00")
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.CURRENCY_NOT_FOUND);

        verify(currencyRepository).findById(newCurrencyId);
        verifyNoInteractions(buildingRepository);
    }

    @Test
    void validatePatchOrThrow_whenCurrencyInactive_shouldThrow() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        Long newCurrencyId = 55L;
        Currency inactive = aCurrencyWithId(newCurrencyId);
        inactive.setActive(false);

        when(currencyRepository.findById(newCurrencyId)).thenReturn(Optional.of(inactive));

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                null, newCurrencyId, null, null, new BigDecimal("100.00")
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.CURRENCY_IS_NOT_ACTIVE);

        verify(currencyRepository).findById(newCurrencyId);
        verifyNoInteractions(buildingRepository);
    }

    @Test
    void validatePatchOrThrow_whenCurrencyIdProvided_shouldReturnContextWithNewCurrency() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.of(2026, 2, 10));
        policy.setEndDate(LocalDate.of(2026, 3, 10));

        Long newCurrencyId = 55L;
        Currency currency = aCurrencyWithId(newCurrencyId);
        currency.setActive(true);

        when(currencyRepository.findById(newCurrencyId)).thenReturn(Optional.of(currency));

        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                null, newCurrencyId, null, null, new BigDecimal("100.00")
        );

        PolicyFacade.PolicyPatchContext ctx = policyFacade.validatePatchOrThrow(policy, req);

        assertSame(policy.getBuilding(), ctx.building());
        assertSame(currency, ctx.currency());

        verify(currencyRepository).findById(newCurrencyId);
        verifyNoInteractions(buildingRepository);
    }

    @Test
    void validatePatchOrThrow_whenDatesInvalid_shouldThrow_invalidDates() {
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.of(2026, 2, 10));
        policy.setEndDate(LocalDate.of(2026, 2, 20));

        // startDate after endDate
        PolicyPatchRequestDTO req = new PolicyPatchRequestDTO(
                null, null,
                LocalDate.of(2026, 3, 1),
                null,
                null
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.validatePatchOrThrow(policy, req));

        assertHasError(ex, ErrorCode.INVALID_DATES);

        verifyNoInteractions(buildingRepository, currencyRepository);
    }
}
