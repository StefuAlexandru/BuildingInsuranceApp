package ro.insurance.buildinginsurance.application.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyActivateRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCancelRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCreateRequestDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientWithId;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.aDraftPolicyWithAllMandatoryFields;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.aPolicyCreateRequest;

@ExtendWith(MockitoExtension.class)
class PolicyFacadeTest {

    @Mock private ClientRepository clientRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private BrokerRepository brokerRepository;
    @Mock private CurrencyRepository currencyRepository;

    @InjectMocks
    private PolicyFacade policyFacade;

    @Test
    void resolveCreateContext_happyPath_shouldLoadAll_validateOwnership_currency_dates_andReturnContext() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        // Act
        PolicyFacade.PolicyCreateContext ctx = policyFacade.resolveCreateContext(request);

        // Assert
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
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(false);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_OWNED_BY_CLIENT.name().equals(e.getCode())));

        verify(brokerRepository, never()).findById(any());
        verify(currencyRepository, never()).findById(any());
    }

    @Test
    void resolveCreateContext_whenCurrencyInactive_shouldThrow() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(false);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CURRENCY_IS_NOT_ACTIVE.name().equals(e.getCode())));
    }

    @Test
    void resolveCreateContext_whenStartAfterEnd_shouldThrow() {
        // Arrange
        PolicyCreateRequestDTO request = new PolicyCreateRequestDTO(
                1L, 1L, 1L, 1L,
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                new BigDecimal("100.00")
        );

        Client client = aClientWithId(request.clientId());
        Building building = aBuildingWithId(request.buildingId());
        Broker broker = aBrokerWithId(request.brokerId());
        Currency currency = aCurrencyWithId(request.currencyId());
        currency.setActive(true);

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(building));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(broker));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.of(currency));

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_DATES.name().equals(e.getCode())));
    }

    @Test
    void resolveCreateContext_whenClientNotFound_shouldThrow_andStop() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();
        when(clientRepository.findById(request.clientId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode())));

        verify(clientRepository).findById(request.clientId());
        verifyNoMoreInteractions(clientRepository);
        verifyNoInteractions(buildingRepository, brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenBuildingNotFound_shouldThrow_andStop() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_FOUND.name().equals(e.getCode())));

        verify(clientRepository).findById(request.clientId());
        verify(buildingRepository).findById(request.buildingId());

        verify(buildingRepository, never()).existsByIdAndClientId(anyLong(), anyLong());
        verifyNoInteractions(brokerRepository, currencyRepository);
    }

    @Test
    void resolveCreateContext_whenBrokerNotFound_shouldThrow_andStop() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BROKER_NOT_FOUND.name().equals(e.getCode())));

        verify(clientRepository).findById(request.clientId());
        verify(buildingRepository).findById(request.buildingId());
        verify(buildingRepository).existsByIdAndClientId(request.buildingId(), request.clientId());
        verify(brokerRepository).findById(request.brokerId());

        verifyNoInteractions(currencyRepository);
    }

    @Test
    void resolveCreateContext_whenCurrencyNotFound_shouldThrow() {
        // Arrange
        PolicyCreateRequestDTO request = aPolicyCreateRequest();

        when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(aClientWithId(request.clientId())));
        when(buildingRepository.findById(request.buildingId())).thenReturn(Optional.of(aBuildingWithId(request.buildingId())));
        when(buildingRepository.existsByIdAndClientId(request.buildingId(), request.clientId())).thenReturn(true);
        when(brokerRepository.findById(request.brokerId())).thenReturn(Optional.of(aBrokerWithId(request.brokerId())));
        when(currencyRepository.findById(request.currencyId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyFacade.resolveCreateContext(request));
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CURRENCY_NOT_FOUND.name().equals(e.getCode())));

        verify(clientRepository).findById(request.clientId());
        verify(buildingRepository).findById(request.buildingId());
        verify(buildingRepository).existsByIdAndClientId(request.buildingId(), request.clientId());
        verify(brokerRepository).findById(request.brokerId());
        verify(currencyRepository).findById(request.currencyId());
    }

    @Test
    void validateActivateOrThrow_whenStatusNotDraft_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy, new PolicyActivateRequestDTO(null)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_NOT_ACTIVATABLE.name().equals(e.getCode())));
    }

    @Test
    void validateActivateOrThrow_whenStartDateInPast_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.now().minusDays(1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy, new PolicyActivateRequestDTO(null)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_START_DATE_IN_PAST.name().equals(e.getCode())));
    }

    @Test
    void validateActivateOrThrow_whenMissingMandatoryFields_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.now().plusDays(1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy, new PolicyActivateRequestDTO(null)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_INVALID_STATE.name().equals(e.getCode())));
    }

    @Test
    void validateActivateOrThrow_whenCurrencyInactive_shouldThrow() {
        // Arrange
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(LocalDate.now().plusDays(1)); // safe
        policy.getCurrency().setActive(false);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateActivateOrThrow(policy, new PolicyActivateRequestDTO(null)));

        assertTrue(ex.getErrors().stream()
                .anyMatch(e -> ErrorCode.CURRENCY_IS_NOT_ACTIVE.name().equals(e.getCode())));
    }

    @Test
    void validateActivateOrThrow_happyPath_shouldNotThrow() {
        // Arrange
        Policy policy = aDraftPolicyWithAllMandatoryFields(10L);
        policy.setStartDate(LocalDate.now().plusDays(1));
        policy.getCurrency().setActive(true);

        // Act + Assert
        assertDoesNotThrow(() ->
                policyFacade.validateActivateOrThrow(policy, new PolicyActivateRequestDTO(OffsetDateTime.now())));
    }


    @Test
    void validateCancelOrThrow_whenNotActive_shouldThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.DRAFT);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> policyFacade.validateCancelOrThrow(policy, new PolicyCancelRequestDTO("x", null)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.POLICY_NOT_CANCELLABLE.name().equals(e.getCode())));
    }

    @Test
    void validateCancelOrThrow_whenActive_shouldNotThrow() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setStatus(PolicyStatus.ACTIVE);

        assertDoesNotThrow(() -> policyFacade.validateCancelOrThrow(policy, new PolicyCancelRequestDTO("x", null)));
    }
}
