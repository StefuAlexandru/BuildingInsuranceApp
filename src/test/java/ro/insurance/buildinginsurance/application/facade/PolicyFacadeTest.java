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
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCreateRequestDTO;
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

    private static void assertHasError(BusinessException ex, ErrorCode code) {
        assertTrue(ex.getErrors().stream().anyMatch(e -> code.name().equals(e.getCode())));
    }

    // ---------------- resolveCreateContext ----------------

    @Test
    void resolveCreateContext_happyPath_shouldLoadAll_validateOwnership_currency_dates_andReturnContext() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(true);

        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        PolicyFacade.PolicyCreateContext ctx = policyFacade.resolveCreateContext(request);

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

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.BUILDING_NOT_OWNED_BY_CLIENT);

        verify(brokerRepository, never()).findById(any());
        verify(currencyRepository, never()).findById(any());
    }

    @Test
    void resolveCreateContext_whenCurrencyInactive_shouldThrow() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(true);

        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(false);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.CURRENCY_IS_NOT_ACTIVE);
    }

    @Test
    void resolveCreateContext_whenStartAfterEnd_shouldThrow() {
        PolicyCreateRequestDTO request = new PolicyCreateRequestDTO(
                1L, 1L, 1L, 1L,
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                new BigDecimal("100.00")
        );

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(true);

        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.INVALID_DATES);
    }

    @Test
    void resolveCreateContext_whenClientNotFound_shouldThrow_andStop() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        when(clientRepository.findById(request.clientId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.CLIENT_NOT_FOUND);

        verifyNoInteractions(buildingRepository, brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenBuildingNotFound_shouldThrow_andStop() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(buildingRepository, never()).existsByIdAndClientId(anyLong(), anyLong());
        verifyNoInteractions(brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenBrokerNotFound_shouldThrow_andStop() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.BROKER_NOT_FOUND);

        verifyNoInteractions(currencyRepository);
    }

    @Test
    void resolveCreateContext_whenCurrencyNotFound_shouldThrow() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.CURRENCY_NOT_FOUND);
    }

    @Test
    void resolveCreateContext_whenBrokerInactive_shouldThrow_andStopBeforeCurrencyLoad() {
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(false);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));

        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertHasError(ex, ErrorCode.BROKER_IS_NOT_ACTIVE);

        verify(currencyRepository, never()).findById(anyLong());
    }

    @Test
    void resolveCreateContext_whenStartEqualsEnd_shouldNotThrow() {
        LocalDate date = LocalDate.of(2026, 2, 10);
        PolicyCreateRequestDTO request = new PolicyCreateRequestDTO(
                1L, 1L, 1L, 1L,
                date, date,
                new BigDecimal("100.00")
        );

        Broker broker = aBrokerWithId(request.brokerId());
        broker.setActive(true);

        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        assertDoesNotThrow(() -> policyFacade.resolveCreateContext(request));
    }

    // ---------------- validateActivateOrThrow ----------------

    @Test
    void validateActivateOrThrow_whenStatusNotDraft_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy));

        assertHasError(ex, ErrorCode.POLICY_NOT_ACTIVATABLE);
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
}
