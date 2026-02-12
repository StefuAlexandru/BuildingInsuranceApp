package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.PolicyExpirationService;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.domain.enums.PolicyStatus.ACTIVE;
import static ro.insurance.buildinginsurance.domain.enums.PolicyStatus.EXPIRED;

@ExtendWith(MockitoExtension.class)
class PolicyExpirationServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Test
    void expirePolicies_shouldUseClockToday_andReturnUpdatedCount() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-02-12T10:00:00Z"), ZoneOffset.UTC);
        PolicyExpirationService policyExpirationService = new PolicyExpirationService(policyRepository, fixedClock);

        when(policyRepository.expireActivePolicies(LocalDate.of(2026, 2, 12), ACTIVE, EXPIRED)).thenReturn(7);

        int updated = policyExpirationService.expirePolicies();

        assertEquals(7, updated);

        verify(policyRepository).expireActivePolicies(LocalDate.of(2026, 2, 12), ACTIVE, EXPIRED);
        verifyNoMoreInteractions(policyRepository);
    }
}
