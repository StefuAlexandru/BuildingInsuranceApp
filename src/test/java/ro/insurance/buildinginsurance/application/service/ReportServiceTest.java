package ro.insurance.buildinginsurance.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.ReportService;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.dto.report_dto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;
import static ro.insurance.buildinginsurance.util.ReportTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private EntityManager entityManager;
    @Mock private TypedQuery<Object[]> typedQuery;

    @InjectMocks private ReportService reportService;

    @Captor private ArgumentCaptor<String> jpqlCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
    }

    @Test
    void getPoliciesReport_NONE_groupBy3_noOptionals_shouldBuildQuery_setFromTo_mapRows_andTotals() {
        ReportRequestDTO req = aReportReq_countryCityBroker_none();

        List<Object[]> dbRows = List.of(
                new Object[]{"7","Romania","3","Rediu","1","Test", 1L, new BigDecimal("206.3995"), new BigDecimal("55887.23")},
                new Object[]{"7","Romania","2","Wolf Valley","1","Test", 3L, new BigDecimal("67631.6096"), new BigDecimal("1500000.00")}
        );
        when(typedQuery.getResultList()).thenReturn(dbRows);

        ReportResponseDTO res = reportService.getPoliciesReport(req);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertTrue(jpql.contains("from Policy p"));
        assertTrue(jpql.contains("join p.currency cur"));
        assertTrue(jpql.contains("p.startDate >= :from"));
        assertTrue(jpql.contains("p.startDate <= :to"));
        assertFalse(jpql.contains(":status"));
        assertFalse(jpql.contains(":currencyId"));
        assertFalse(jpql.contains(":brokerId"));

        verify(typedQuery).setParameter("from", req.from());
        verify(typedQuery).setParameter("to", req.to());
        verify(typedQuery, never()).setParameter(eq("status"), any());
        verify(typedQuery, never()).setParameter(eq("currencyId"), any());
        verify(typedQuery, never()).setParameter(eq("brokerId"), any());

        assertEquals(2, res.rows().size());

        ReportRowDTO r1 = res.rows().getFirst();
        assertEquals("7|3|1", r1.groupKey());
        assertEquals("Romania / Rediu / Test", r1.groupName());
        assertNull(r1.periodStart());
        assertEquals(1L, r1.policyCount());
        assertEquals(0, new BigDecimal("206.3995").compareTo(r1.totalPremium()));
        assertEquals(0, new BigDecimal("55887.23").compareTo(r1.totalInsuredValue()));

        assertEquals(4L, res.totals().policyCount());
        assertEquals(0, new BigDecimal("67838.0091").compareTo(res.totals().totalPremium()));
        assertEquals(0, new BigDecimal("1555887.23").compareTo(res.totals().totalInsuredValue()));
    }

    @Test
    void getPoliciesReport_withAllOptionals_shouldAddWhereAndSetAllParams() {
        ReportRequestDTO req = aReportReq_fullFilters_month(2L, 1L);

        List<Object[]> dbRows = List.<Object[]>of(
                new Object[]{"7","Romania","3","Rediu","1","Test", 2026, 2, 2L, new BigDecimal("100.00"), new BigDecimal("500000.00")}
        );
        when(typedQuery.getResultList()).thenReturn(dbRows);

        ReportResponseDTO res = reportService.getPoliciesReport(req);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertTrue(jpql.contains("p.status = :status"));
        assertTrue(jpql.contains("p.currency.id = :currencyId"));
        assertTrue(jpql.contains("br.id = :brokerId"));

        verify(typedQuery).setParameter("from", req.from());
        verify(typedQuery).setParameter("to", req.to());
        verify(typedQuery).setParameter("status", req.status());
        verify(typedQuery).setParameter("currencyId", req.currencyId());
        verify(typedQuery).setParameter("brokerId", req.brokerId());

        assertEquals(1, res.rows().size());
        assertEquals(LocalDate.of(2026, 2, 1), res.rows().getFirst().periodStart());
    }

    @Test
    void getPoliciesReport_DAY_shouldMapPeriodStartAsStartDate() {
        ReportRequestDTO req = aReportReq_countryCity_day();
        LocalDate d = LocalDate.of(2026, 2, 10);

        List<Object[]> dbRows = List.<Object[]>of(
                new Object[]{"7","Romania","3","Rediu", d, 5L, new BigDecimal("10.00"), new BigDecimal("20.00")}
        );
        when(typedQuery.getResultList()).thenReturn(dbRows);

        ReportResponseDTO res = reportService.getPoliciesReport(req);

        assertEquals(1, res.rows().size());
        assertEquals(d, res.rows().getFirst().periodStart());
        assertEquals(5L, res.rows().getFirst().policyCount());
    }

    @Test
    void getPoliciesReport_whenFromAfterTo_shouldThrow() {
        ReportRequestDTO req = aReportReq_invalid_fromAfterTo();

        BusinessException ex = assertThrows(BusinessException.class, () -> reportService.getPoliciesReport(req));
        assertHasError(ex,ErrorCode.REPORT_INVALID_REQUEST);

        verifyNoInteractions(entityManager);
    }
}
