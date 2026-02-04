package ro.insurance.buildinginsurance.application.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.IReportService;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.web.dto.reportDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService implements IReportService {

    private final EntityManager entityManager;

    @Override
    public ReportResponseDTO getPoliciesReport(ReportRequestDTO request) {
        validateRequest(request);

        QueryParts queryParts = buildQuery(request);

        TypedQuery<Object[]> query = entityManager.createQuery(queryParts.jpql, Object[].class)
                .setParameter("from", request.from())
                .setParameter("to", request.to());

        if (request.status() != null) query.setParameter("status", request.status());
        if (request.currencyId() != null) query.setParameter("currencyId", request.currencyId());
        if (request.brokerId() != null) query.setParameter("brokerId", request.brokerId());

        List<Object[]> rows = query.getResultList();

        List<ReportRowDTO> mapped = rows.stream()
                .map(r -> mapRow(r, request))
                .toList();

        ReportTotalsDTO totals = totals(mapped);

        return new ReportResponseDTO(mapped, totals);
    }

    // ------------------- VALIDATION -------------------

    private void validateRequest(ReportRequestDTO request) {
        if (request.from().isAfter(request.to())) {
            throw new BusinessException(ErrorCode.REPORT_INVALID_REQUEST);
        }
    }

    // ------------------- QUERY BUILDING -------------------

    private record QueryParts(String jpql) {}

    private QueryParts buildQuery(ReportRequestDTO request) {
        String from = """
                from Policy p
                  join p.building b
                  join p.broker br
                  join p.currency cur
                  join b.city c
                  join c.county co
                  join co.country cn
                """;

        List<String> select = new ArrayList<>();
        List<String> groupBy = new ArrayList<>();
        List<String> orderBy = new ArrayList<>();

        // GroupBy columns
        for (GroupBy g : request.groupBy()) {
            switch (g) {
                case COUNTRY -> {
                    select.add("cast(cn.id as string)");
                    select.add("cn.name");
                    groupBy.add("cn.id");
                    groupBy.add("cn.name");
                    orderBy.add("cn.name");
                }
                case COUNTY -> {
                    select.add("cast(co.id as string)");
                    select.add("co.name");
                    groupBy.add("co.id");
                    groupBy.add("co.name");
                    orderBy.add("co.name");
                }
                case CITY -> {
                    select.add("cast(c.id as string)");
                    select.add("c.name");
                    groupBy.add("c.id");
                    groupBy.add("c.name");
                    orderBy.add("c.name");
                }
                case BROKER -> {
                    select.add("cast(br.id as string)");
                    select.add("br.name");
                    groupBy.add("br.id");
                    groupBy.add("br.name");
                    orderBy.add("br.name");
                }
            }
        }

        // Period columns
        switch (request.granularity()) {
            case NONE -> {
                // nothing
            }
            case DAY -> {
                select.add("p.startDate");
                groupBy.add("p.startDate");
                orderBy.add("p.startDate");
            }
            case MONTH -> {
                select.add("year(p.startDate)");
                select.add("month(p.startDate)");
                groupBy.add("year(p.startDate)");
                groupBy.add("month(p.startDate)");
                orderBy.add("year(p.startDate)");
                orderBy.add("month(p.startDate)");
            }
        }

        // Aggregates
        select.add("count(p.id)");
        select.add("coalesce(sum(p.finalPremiumAmount * cur.exchangeRateToBase), 0)");
        select.add("coalesce(sum(b.insuredValue), 0)");

        List<String> where = new ArrayList<>();
        where.add("p.startDate >= :from");
        where.add("p.startDate <= :to");

        if (request.status() != null) where.add("p.status = :status");
        if (request.currencyId() != null) where.add("p.currency.id = :currencyId");
        if (request.brokerId() != null) where.add("br.id = :brokerId");

        String jpql = "select " + String.join(", ", select) + "\n"
                + from + "\n"
                + "where " + String.join(" and ", where) + "\n"
                + "group by " + String.join(", ", groupBy) + "\n"
                + "order by " + String.join(", ", orderBy);

        return new QueryParts(jpql);
    }

    // ------------------- MAPPING -------------------

    private ReportRowDTO mapRow(Object[] object, ReportRequestDTO request) {
        int idx = 0;

        int groupCount = request.groupBy().size();

        List<String> keys = new ArrayList<>(groupCount);
        List<String> names = new ArrayList<>(groupCount);

        for (int i = 0; i < groupCount; i++) {
            keys.add(Objects.toString(object[idx++], ""));
            names.add(Objects.toString(object[idx++], ""));
        }

        LocalDate periodStart = null;
        switch (request.granularity()) {
            case NONE -> { /* null */ }
            case DAY -> periodStart = (LocalDate) object[idx++];
            case MONTH -> {
                Integer year = (Integer) object[idx++];
                Integer month = (Integer) object[idx++];
                periodStart = LocalDate.of(year, month, 1);
            }
        }

        long policyCount = ((Number) object[idx++]).longValue();
        BigDecimal totalPremium = (BigDecimal) object[idx++];
        BigDecimal totalInsuredValue = (BigDecimal) object[idx];

        return new ReportRowDTO(
                String.join("|", keys),
                String.join(" / ", names),
                periodStart,
                policyCount,
                totalPremium,
                totalInsuredValue
        );
    }

    private ReportTotalsDTO totals(List<ReportRowDTO> rows) {
        long count = rows.stream().mapToLong(ReportRowDTO::policyCount).sum();
        BigDecimal premium = rows.stream()
                .map(ReportRowDTO::totalPremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal insured = rows.stream()
                .map(ReportRowDTO::totalInsuredValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportTotalsDTO(count, premium, insured);
    }
}
