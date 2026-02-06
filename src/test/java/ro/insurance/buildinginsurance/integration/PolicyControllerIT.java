package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.mapper.*;
import ro.insurance.buildinginsurance.config.FixedClockConfig;
import ro.insurance.buildinginsurance.config.TestSecurityUsersConfig;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.buildingDto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCreateRequestDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerRequest;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingRequest;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientRequestWithAddress;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryRequest;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyRequest;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyRequest;
import static ro.insurance.buildinginsurance.util.PolicyTestDataFactory.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityUsersConfig.class, FixedClockConfig.class})
@Transactional
class PolicyControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired PolicyRepository policyRepository;
    @Autowired BrokerRepository brokerRepository;
    @Autowired CurrencyRepository currencyRepository;

    @Autowired CountryRepository countryRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CityRepository cityRepository;

    @Autowired CountryMapper countryMapper;
    @Autowired CountyMapper countyMapper;
    @Autowired CityMapper cityMapper;

    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @Autowired BrokerMapper brokerMapper;
    @Autowired CurrencyMapper currencyMapper;

    private static final String ADMIN = "admin";
    private static final String USER = "broker@email.com";
    private static final String PASS = "pass";
    private static final String BASE = "/api/brokers/policies";

    private record Seed(Country country, County county, City city) {}

    private Seed seedGeography() {
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        CityRequestDTO cityRequestDTO = aCityRequest();

        Country country = countryRepository.save(countryMapper.toEntity(countryRequestDTO));
        County county = countyRepository.save(countyMapper.toEntity(countyRequestDTO, country));
        City city = cityRepository.save(cityMapper.toEntity(cityRequestDTO, county));

        return new Seed(country, county, city);
    }

    private Broker seedBroker() {
        BrokerRequestDTO brokerRequestDTO = aBrokerRequest();
        return brokerRepository.save(brokerMapper.toEntity(brokerRequestDTO));
    }

    private Currency seedCurrency() {
        CurrencyRequestDTO currencyRequestDTO = aCurrencyRequest();
        return currencyRepository.save(currencyMapper.toEntity(currencyRequestDTO));
    }

    @Test
    void createDraft_thenActivate_fullFlow() throws Exception {
        Seed seed = seedGeography();
        Broker broker = seedBroker();
        Currency currency = seedCurrency();

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed);

        // ---------- CREATE BUILDING ----------
        long buildingId = seedBuildingAndGetId(clientId, seed);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                clientId,
                buildingId,
                broker.getId(),
                currency.getId(),
                start,
                end
        );

        // ---------- CREATE DRAFT ----------
        var draftResponse = mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").exists(),
                        jsonPath("$.policyNumber").exists(),
                        jsonPath("$.status").value("DRAFT"),

                        jsonPath("$.clientId").value((int) clientId),
                        jsonPath("$.buildingId").value((int) buildingId),
                        jsonPath("$.brokerId").value(broker.getId().intValue()),
                        jsonPath("$.currencyId").value(currency.getId().intValue()),

                        jsonPath("$.startDate").value(start.toString()),
                        jsonPath("$.endDate").value(end.toString()),
                        jsonPath("$.basePremiumAmount").value(createRequest.basePremiumAmount().doubleValue()),

                        jsonPath("$.calculationResponse.finalPremium").exists(),

                        jsonPath("$.activatedAt").doesNotExist(),
                        jsonPath("$.brokerCommissionPct").doesNotExist(),
                        jsonPath("$.brokerCommissionAmount").doesNotExist()
                )
                .andReturn();

        long policyId = objectMapper.readTree(draftResponse.getResponse().getContentAsByteArray()).
                get("id").asLong();


        // DB after CREATE
        Policy draft = policyRepository.findById(policyId).orElseThrow();
        assertEquals(PolicyStatus.DRAFT, draft.getStatus());
        assertNotNull(draft.getPolicyNumber());

        assertEquals(clientId, draft.getClient().getId());
        assertEquals(buildingId, draft.getBuilding().getId());
        assertEquals(broker.getId(), draft.getBroker().getId());
        assertEquals(currency.getId(), draft.getCurrency().getId());

        assertEquals(start, draft.getStartDate());
        assertEquals(end, draft.getEndDate());
        assertEquals(0, createRequest.basePremiumAmount().compareTo(draft.getBasePremiumAmount()));

        assertNotNull(draft.getFinalPremiumAmount());
        assertNull(draft.getActivatedAt());
        assertNull(draft.getBrokerCommissionPct());
        assertNull(draft.getBrokerCommissionAmount());

        // ---------- ACTIVATE ----------
        mockMvc.perform(post(BASE + "/{id}/activate", policyId)
                        .with(httpBasic(USER, PASS)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value((int) policyId),
                        jsonPath("$.status").value("ACTIVE"),
                        jsonPath("$.activatedAt").exists(),

                        jsonPath("$.calculationResponse.finalPremium").exists(),

                        jsonPath("$.brokerCommissionPct").exists(),
                        jsonPath("$.brokerCommissionAmount").exists()
                );

        // DB after ACTIVATE
        Policy active = policyRepository.findById(policyId).orElseThrow();
        assertEquals(PolicyStatus.ACTIVE, active.getStatus());
        assertNotNull(active.getActivatedAt());
        assertNotNull(active.getFinalPremiumAmount());

        assertEquals(0, broker.getCommissionPct().compareTo(active.getBrokerCommissionPct()));

        BigDecimal expectedCommission = active.getFinalPremiumAmount()
                .multiply(active.getBrokerCommissionPct())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        assertEquals(0, expectedCommission.compareTo(active.getBrokerCommissionAmount()));
    }

    @Test
    void createDraft_withNoExistingClient_shouldThrow_andNotPersist() throws Exception {
        Seed seed = seedGeography();
        Broker broker = seedBroker();
        Currency currency = seedCurrency();

        long before = policyRepository.count();

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed);

        // ---------- CREATE BUILDING ----------
        long buildingId = seedBuildingAndGetId(clientId,seed);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                999L, // Not in db
                buildingId,
                broker.getId(),
                currency.getId(),
                start,
                end
        );

        var result = mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals(before, policyRepository.count());

        String body = result.getResponse().getContentAsString().toLowerCase();
        assertTrue(body.contains("client") || body.contains("not found"));
    }

    @Test
    void createDraft_withNoExistingBuilding_shouldThrow_andNotPersist() throws Exception {
        Seed seed = seedGeography();
        Broker broker = seedBroker();
        Currency currency = seedCurrency();

        long before = policyRepository.count();

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed);

        // ---------- DO NOT CREATE BUILDING ----------

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                clientId,
                999L,
                broker.getId(),
                currency.getId(),
                start,
                end
        );

        var result = mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals(before, policyRepository.count());

        String body = result.getResponse().getContentAsString().toLowerCase();
        assertTrue(body.contains("building") || body.contains("not found"));
    }

    @Test
    void createPolicy_shouldReturn400_whenPremiumNegative() throws Exception {
        Seed seed = seedGeography();
        Broker broker = seedBroker();
        Currency currency = seedCurrency();

        long clientId = seedClientAndGetId(seed);
        long buildingId = seedBuildingAndGetId(clientId, seed);

        long before = policyRepository.count();

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO request = aPolicyCreateRequest(
                clientId,
                buildingId,
                broker.getId(),
                currency.getId(),
                start,
                end
        );

        ObjectNode payload = objectMapper.valueToTree(request);
        payload.put("basePremiumAmount", new BigDecimal("-100.00"));

        var result = mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(before, policyRepository.count());

        String body = result.getResponse().getContentAsString().toLowerCase();

        assertTrue(body.contains("base premium amount should be greater than zero"));
    }

    @Test
    void deactivateBroker_thenCreatingPolicyWithThatBrokerIsRejected_existingPoliciesUnaffected() throws Exception {
        Seed seed = seedGeography();
        Currency currency = seedCurrency();

        // ---------- CREATE BROKER  ----------
        long brokerId = seedBrokerAndGetId();

        // ---------- CREATE CLIENT  ----------
        long clientId = seedClientAndGetId(seed);

        // ---------- CREATE BUILDING  ----------
        long buildingId = seedBuildingAndGetId(clientId, seed);

        // ---------- CREATE POLICY  ----------
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);
        PolicyCreateRequestDTO createRequest1 = aPolicyCreateRequest(
                clientId,
                buildingId,
                brokerId,
                currency.getId(),
                start,
                end
        );

        var policyResponse1 = mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        long policyId1 = objectMapper.readTree(policyResponse1.getResponse().getContentAsByteArray()).get("id").asLong();

        // ---------- DEACTIVATE BROKER  ----------
        mockMvc.perform(post("/api/admin/brokers/deactivate/{id}", brokerId)
                        .with(httpBasic(ADMIN, PASS)))
                .andExpect(status().isOk());

        long before = policyRepository.count();

        PolicyCreateRequestDTO createRequest2 = aPolicyCreateRequest(
                clientId,
                buildingId,
                brokerId,                 // same broker, now deactivated
                currency.getId(),
                LocalDate.of(2026, 2, 6),
                LocalDate.of(2026, 3, 6)
        );

        mockMvc.perform(post(BASE)
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2)))
                .andExpectAll(
                        status().isBadRequest(),
                        content().string(containsString("Broker is not active"))
                ).andReturn();

        assertEquals(before, policyRepository.count(), "No new policy should be created");

        // Existing policy remains unaffected
        Policy existing = policyRepository.findById(policyId1).orElseThrow();
        assertEquals(PolicyStatus.DRAFT, existing.getStatus());
        assertEquals(brokerId, existing.getBroker().getId());
    }

    // -------------------  HELPERS  -------------------

    private long seedBuildingAndGetId(long clientId, Seed seed) throws Exception {
        var buildingRequest = aBuildingRequest();
        BuildingRequestDTO buildingRequestDTO = new BuildingRequestDTO(
                clientId,
                seed.city().getId(),
                buildingRequest.street(),
                buildingRequest.number(),
                buildingRequest.constructionYear(),
                buildingRequest.buildingType(),
                buildingRequest.floors(),
                buildingRequest.surfaceArea(),
                buildingRequest.insuredValue(),
                buildingRequest.floodZone(),
                buildingRequest.earthquakeZone()
        );

        var buildingResponse = mockMvc.perform(post("/api/brokers/buildings")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(buildingResponse.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private long seedClientAndGetId(Seed seed) throws Exception {
        var clientRequest = aClientRequestWithAddress(seed.city().getId(), "Str. Gheo", "10A");

        var clientResponse = mockMvc.perform(post("/api/brokers/clients")
                        .with(httpBasic(USER,PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").exists()
                ).andReturn();

        return objectMapper.readTree(clientResponse.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private long seedBrokerAndGetId() throws Exception {
        BrokerRequestDTO brokerRequest = aBrokerRequest();

        var brokerResponse = mockMvc.perform(post("/api/admin/brokers")
                        .with(httpBasic(ADMIN, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brokerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(brokerResponse.getResponse().getContentAsByteArray()).get("id").asLong();
    }

}
