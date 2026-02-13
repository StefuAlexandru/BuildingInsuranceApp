package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Mock PolicyRepository policyRepository;

    @Test
    void expirePolicies_shouldUseClockDate_andReturnUpdatedCount() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-04T10:00:00Z"), ZoneOffset.UTC);
        PolicyExpirationService svc = new PolicyExpirationService(policyRepository, clock);

        when(policyRepository.expireActivePolicies(any(), eq(ACTIVE), eq(EXPIRED))).thenReturn(7);

        int updated = svc.expirePolicies();

        assertEquals(7, updated);

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(policyRepository).expireActivePolicies(dateCaptor.capture(), eq(ACTIVE), eq(EXPIRED));
        assertEquals(LocalDate.of(2026, 2, 4), dateCaptor.getValue());

        verifyNoMoreInteractions(policyRepository);
    }
}

