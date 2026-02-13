package ro.insurance.buildinginsurance.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.ReportService;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRowDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;
import static ro.insurance.buildinginsurance.util.data_factory.ReportTestDataFactory.*;

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
    void getPoliciesReport_COUNTRY_noOptionals_shouldBuildQuery_setParams_andMapRows() {
        ReportRequestDTO request = aReportReq_minRequired(2L);

        List<Object[]> dbRows = List.of(
                // groupKey, groupName, currencyId, currencyCode, policyCount, totalFinalPremium, totalFinalPremiumInBase
                new Object[]{"7", "Romania", 2L, "EUR", 1L, new BigDecimal("206.39"), new BigDecimal("1023.45")},
                new Object[]{"8", "Moldova", 2L, "EUR", 3L, new BigDecimal("10.00"),  new BigDecimal("49.50")}
        );
        when(typedQuery.getResultList()).thenReturn(dbRows);

        List<ReportRowDTO> response = reportService.getPoliciesReport(GroupBy.COUNTRY, request);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertAll(
                () -> assertTrue(jpql.contains("from Policy p")),
                () -> assertTrue(jpql.contains("join p.currency cur")),
                () -> assertTrue(jpql.contains("p.startDate >= :from")),
                () -> assertTrue(jpql.contains("p.startDate <= :to")),
                () -> assertTrue(jpql.contains("p.currency.id = :currencyId")),

                () -> assertFalse(jpql.contains(":status")),
                () -> assertFalse(jpql.contains(":buildingType"))
        );

        verify(typedQuery).setParameter("from", request.from());
        verify(typedQuery).setParameter("to", request.to());
        verify(typedQuery).setParameter("currencyId", request.currencyId());
        verify(typedQuery, never()).setParameter(eq("status"), any());
        verify(typedQuery, never()).setParameter(eq("buildingType"), any());

        assertEquals(2, response.size());

        ReportRowDTO r1 = response.getFirst();

        assertAll(
                () -> assertEquals("7", r1.groupKey()),
                () -> assertEquals("Romania", r1.groupName()),
                () -> assertEquals(2L, r1.currencyId()),
                () -> assertEquals("EUR", r1.currencyCode()),
                () -> assertEquals(1L, r1.policyCount()),
                () -> assertEquals(0, new BigDecimal("206.39").compareTo(r1.totalFinalPremium())),
                () -> assertEquals(0, new BigDecimal("1023.45").compareTo(r1.totalFinalPremiumInBaseCurrency()))
        );
    }

    @Test
    void getPoliciesReport_BROKER_withAllOptionals_shouldAddWhereAndSetAllParams() {
        BuildingType anyType = BuildingType.values()[0];
        ReportRequestDTO request = new ReportRequestDTO(
                aReportReq_minRequired(1L).from(),
                aReportReq_minRequired(1L).to(),
                1L,
                PolicyStatus.ACTIVE,
                anyType
        );

        List<Object[]> dbRows = List.<Object[]>of(
                new Object[]{"10", "Broker X", 1L, "RON", 5L, new BigDecimal("100.00"), new BigDecimal("100.00")}
        );
        when(typedQuery.getResultList()).thenReturn(dbRows);

        List<ReportRowDTO> response = reportService.getPoliciesReport(GroupBy.BROKER, request);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertAll(
                () -> assertTrue(jpql.contains("p.status = :status")),
                () -> assertTrue(jpql.contains("b.buildingType = :buildingType")),
                () -> assertTrue(jpql.contains("p.currency.id = :currencyId"))
        );

        verify(typedQuery).setParameter("from", request.from());
        verify(typedQuery).setParameter("to", request.to());
        verify(typedQuery).setParameter("currencyId", request.currencyId());
        verify(typedQuery).setParameter("status", request.status());
        verify(typedQuery).setParameter("buildingType", request.buildingType());

        assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals("10", response.getFirst().groupKey()),
                () -> assertEquals("Broker X", response.getFirst().groupName())
        );
    }

    @Test
    void getPoliciesReport_whenFromAfterTo_shouldThrow() {
        ReportRequestDTO request = aReportReq_invalid_fromAfterTo(1L);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reportService.getPoliciesReport(GroupBy.CITY, request)
        );
        assertHasError(ex, ErrorCode.REPORT_INVALID_REQUEST);

        verifyNoInteractions(entityManager);
    }

    @Test
    void getPoliciesReport_COUNTY_shouldBuildQueryWithCountySelectGroupOrder() {
        ReportRequestDTO req = aReportReq_minRequired(2L);

        when(typedQuery.getResultList()).thenReturn(List.of());

        reportService.getPoliciesReport(GroupBy.COUNTY, req);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertAll(
                () -> assertTrue(jpql.contains("cast(co.id as string)")),
                () -> assertTrue(jpql.contains("co.name")),
                () -> assertTrue(jpql.contains("group by co.id")),
                () -> assertTrue(jpql.contains("group by") && jpql.contains("co.name")),

                () -> assertTrue(jpql.contains("cur.id")),
                () -> assertTrue(jpql.contains("cur.code")),

                () -> assertTrue(jpql.contains("p.currency.id = :currencyId")),
                () -> assertTrue(jpql.contains("p.startDate >= :from")),
                () -> assertTrue(jpql.contains("p.startDate <= :to"))
        );

        verify(typedQuery).setParameter("from", req.from());
        verify(typedQuery).setParameter("to", req.to());
        verify(typedQuery).setParameter("currencyId", req.currencyId());
    }

    @Test
    void getPoliciesReport_CITY_shouldBuildQueryWithCitySelectGroupOrder() {
        ReportRequestDTO req = aReportReq_minRequired(2L);

        when(typedQuery.getResultList()).thenReturn(List.of());

        reportService.getPoliciesReport(GroupBy.CITY, req);

        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(Object[].class));
        String jpql = jpqlCaptor.getValue();

        assertAll(
                () -> assertTrue(jpql.contains("cast(c.id as string)")),
                () -> assertTrue(jpql.contains("c.name")),
                () -> assertTrue(jpql.contains("group by c.id")),
                () -> assertTrue(jpql.contains("group by") && jpql.contains("c.name")),

                () -> assertTrue(jpql.contains("cur.id")),
                () -> assertTrue(jpql.contains("cur.code")),

                () -> assertTrue(jpql.contains("p.currency.id = :currencyId"))
        );

        verify(typedQuery).setParameter("from", req.from());
        verify(typedQuery).setParameter("to", req.to());
        verify(typedQuery).setParameter("currencyId", req.currencyId());
    }
}
