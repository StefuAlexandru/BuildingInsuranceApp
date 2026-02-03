package ro.insurance.buildinginsurance.infrastructure.persistance.spec;

import org.springframework.data.jpa.domain.Specification;
import ro.insurance.buildinginsurance.domain.entity.Policy;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.time.LocalDate;

public final class PolicySpecifications {
    private PolicySpecifications() {}

    public static Specification<Policy> hasClientId(Long clientId) {
        return (root, query, criteriaBuilder) ->
                clientId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("client").get("id"), clientId);
    }

    public static Specification<Policy> hasBrokerId(Long brokerId) {
        return (root, query, criteriaBuilder) ->
                brokerId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("broker").get("id"), brokerId);
    }

    public static Specification<Policy> hasStatus(PolicyStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    // policy.startDate >= filter.startDate
    public static Specification<Policy> startDateFrom(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
                startDate == null ? criteriaBuilder.conjunction() : criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    // policy.endDate <= filter.endDate
    public static Specification<Policy> endDateTo(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
                endDate == null ? criteriaBuilder.conjunction() : criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate);
    }
}
