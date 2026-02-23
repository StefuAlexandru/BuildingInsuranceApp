package ro.insurance.buildinginsurance.infrastructure.persistance.spec;

import org.springframework.data.jpa.domain.Specification;
import ro.insurance.buildinginsurance.domain.entity.Policy;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicySearchFilterDTO;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.persistence.criteria.Predicate;

public final class PolicySpecifications {
    private PolicySpecifications() {}

//    Using specification the way below can be efficient for dynamic queries, but for scalability adding a new method for each filter can lead to a large number of methods
//
//    -------------------------------------------------------------------------------------------  I CAN DO IT BETTER!
//
//    public static Specification<Policy> hasClientId(Long clientId) {
//        return (root, query, criteriaBuilder) ->
//                clientId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("client").get("id"), clientId);
//    }
//    public static Specification<Policy> hasBrokerId(Long brokerId) {
//        return (root, query, criteriaBuilder) ->
//                brokerId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("broker").get("id"), brokerId);
//    }
//    public static Specification<Policy> hasStatus(PolicyStatus status) {
//        return (root, query, criteriaBuilder) ->
//                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
//    }
//    public static Specification<Policy> startDateFrom(LocalDate startDate) {
//        return (root, query, criteriaBuilder) ->
//                startDate == null ? criteriaBuilder.conjunction() : criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate);
//    }
//    public static Specification<Policy> endDateTo(LocalDate endDate) {
//        return (root, query, criteriaBuilder) ->
//                endDate == null ? criteriaBuilder.conjunction() : criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate);
//    }

    public static Specification<Policy> byFilter(PolicySearchFilterDTO policyFilter){
        return (root,query,criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();

            addIfNotNull(predicateList, policyFilter.clientId(), v -> criteriaBuilder.equal(root.get("client").get("id"), v));
            addIfNotNull(predicateList, policyFilter.brokerId(), v -> criteriaBuilder.equal(root.get("broker").get("id"), v));
            addIfNotNull(predicateList, policyFilter.status(), v -> criteriaBuilder.equal(root.get("status"), v));
            addIfNotNull(predicateList, policyFilter.startDate(), v -> criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), v));
            addIfNotNull(predicateList, policyFilter.endDate(), v -> criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), v));

            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }

    private static <T> void addIfNotNull(List<Predicate> predicateList, T value, Function<T,Predicate> predicateFunction) {
        if(value != null) {predicateList.add(predicateFunction.apply(value));}
    }
}
