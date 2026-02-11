package ro.insurance.buildinginsurance.application.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.IReportService;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.web.dto.report_dto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService implements IReportService {

    private final EntityManager entityManager;

    private static final String COUNTRY_NAME = "cn.name";
    private static final String COUNTY_NAME  = "co.name";
    private static final String CITY_NAME    = "c.name";
    private static final String CURRENCY_CODE = "cur.code";
    private static final String BROKER_NAME  = "br.name";
    private static final String START_DATE   = "p.startDate";

    private record QueryParts(String jpql) {}

    @Override
    public List<ReportRowDTO> getPoliciesReport(GroupBy groupBy,ReportRequestDTO request) {
        validateRequest(request);

        QueryParts queryParts = buildQuery(groupBy,request);

        TypedQuery<Object[]> query = entityManager.createQuery(queryParts.jpql, Object[].class)
                .setParameter("from", request.from())
                .setParameter("to", request.to())
                .setParameter("currencyId", request.currencyId());

        if (request.status() != null) query.setParameter("status", request.status());
        if (request.buildingType() != null) query.setParameter("buildingType", request.buildingType());

       return query.getResultList().stream()
               .map(this::mapRow)
               .toList();
    }

    // ------------------- VALIDATION -------------------

    private void validateRequest(ReportRequestDTO request) {
        if (request.from().isAfter(request.to())) {
            throw new BusinessException(ErrorCode.REPORT_INVALID_REQUEST);
        }
    }

    // ------------------- QUERY BUILDING -------------------
    private QueryParts buildQuery(GroupBy groupBy,ReportRequestDTO request) {
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
        List<String> group  = new ArrayList<>();
        List<String> order  = new ArrayList<>();


        // GroupBy columns
        switch (groupBy) {
            case COUNTRY -> {
                select.add("cast(cn.id as string)");
                select.add(COUNTRY_NAME);

                group.add("cn.id");
                group.add(COUNTRY_NAME);

                order.add(COUNTRY_NAME);
            }
            case COUNTY -> {
                select.add("cast(co.id as string)");
                select.add(COUNTY_NAME);

                group.add("co.id");
                group.add(COUNTY_NAME);

                order.add(COUNTY_NAME);
            }
            case CITY -> {
                select.add("cast(c.id as string)");
                select.add(CITY_NAME);

                group.add("c.id");
                group.add(CITY_NAME);

                order.add(CITY_NAME);
            }
            case BROKER -> {
                select.add("cast(br.id as string)");
                select.add(BROKER_NAME);

                group.add("br.id");
                group.add(BROKER_NAME);

                order.add(BROKER_NAME);
            }
            default -> throw new BusinessException(ErrorCode.REPORT_INVALID_REQUEST);
        }

        // Currency is always part of grouping
        select.add("cur.id");
        select.add(CURRENCY_CODE);
        group.add("cur.id");
        group.add(CURRENCY_CODE);
        order.add(CURRENCY_CODE);

        // Aggregates
        select.add("count(p.id)");
        select.add("coalesce(sum(p.finalPremiumAmount), 0)");
        select.add("coalesce(sum(p.finalPremiumAmount * cur.exchangeRateToBase), 0)");

        List<String> where = new ArrayList<>();
        where.add(START_DATE + " >= :from");
        where.add(START_DATE + " <= :to");
        where.add("p.currency.id = :currencyId");

        if (request.status() != null) where.add("p.status = :status");
        if (request.buildingType() != null) where.add("b.buildingType = :buildingType");

        String jpql = "select " + String.join(", ", select) + "\n"
                + from + "\n"
                + "where " + String.join(" and ", where) + "\n"
                + "group by " + String.join(", ", group) + "\n"
                + "order by " + String.join(", ", order);

        return new QueryParts(jpql);
    }

    // ------------------- MAPPING -------------------

    private ReportRowDTO mapRow(Object[] row) {
        int idx = 0;

        String groupKey = Objects.toString(row[idx++], "");
        String groupName = Objects.toString(row[idx++], "");

        Long currencyId = ((Number) row[idx++]).longValue();
        String currencyCode = Objects.toString(row[idx++], "");

        long policyCount = ((Number) row[idx++]).longValue();
        BigDecimal totalFinalPremium = (BigDecimal) row[idx++];
        BigDecimal totalFinalPremiumInBase = (BigDecimal) row[idx];

        return new ReportRowDTO(
                groupKey,
                groupName,
                currencyId,
                currencyCode,
                policyCount,
                totalFinalPremium,
                totalFinalPremiumInBase
        );
    }
}
