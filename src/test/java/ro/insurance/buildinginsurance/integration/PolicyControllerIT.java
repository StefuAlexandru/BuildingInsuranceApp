package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.insurance.buildinginsurance.application.mapper.*;
import ro.insurance.buildinginsurance.config.FixedClockConfig;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.integration.support.AbstractIntegrationTest;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyCreateRequestDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.data_factory.BrokerTestDataFactory.aBrokerRequest;
import static ro.insurance.buildinginsurance.util.data_factory.BuildingTestDataFactory.aBuildingRequest;
import static ro.insurance.buildinginsurance.util.data_factory.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.data_factory.ClientTestDataFactory.aClientRequestWithAddress;
import static ro.insurance.buildinginsurance.util.data_factory.CountryTestDataFactory.aCountryRequest;
import static ro.insurance.buildinginsurance.util.data_factory.CountyTestDataFactory.aCountyRequest;
import static ro.insurance.buildinginsurance.util.data_factory.CurrencyTestDataFactory.aCurrencyRequest;
import static ro.insurance.buildinginsurance.util.data_factory.PolicyTestDataFactory.*;

@Import(FixedClockConfig.class)
class PolicyControllerIT extends AbstractIntegrationTest {

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

        String auth = brokerToken(broker.getEmail());

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed,broker.getEmail());

        // ---------- CREATE BUILDING ----------
        long buildingId = seedBuildingAndGetId(clientId, seed,broker.getEmail());

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                clientId,
                buildingId,
                currency.getId(),
                start,
                end
        );

        // ---------- CREATE DRAFT ----------
        var draftResponse = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, auth)
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
                        .header(HttpHeaders.AUTHORIZATION, auth))
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

        String auth = brokerToken(broker.getEmail());

        long before = policyRepository.count();

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed,broker.getEmail());

        // ---------- CREATE BUILDING ----------
        long buildingId = seedBuildingAndGetId(clientId,seed,broker.getEmail());

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                999L, // Not in db
                buildingId,
                currency.getId(),
                start,
                end
        );

        var result = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, auth)
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

        String auth = brokerToken(broker.getEmail());

        long before = policyRepository.count();

        // ---------- CREATE CLIENT ----------
        long clientId = seedClientAndGetId(seed,broker.getEmail());

        // ---------- DO NOT CREATE BUILDING ----------

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                clientId,
                999L,
                currency.getId(),
                start,
                end
        );

        var result = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, auth)
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

        String auth = brokerToken(broker.getEmail());

        long clientId = seedClientAndGetId(seed,broker.getEmail());
        long buildingId = seedBuildingAndGetId(clientId, seed,broker.getEmail());

        long before = policyRepository.count();

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        PolicyCreateRequestDTO request = aPolicyCreateRequest(
                clientId,
                buildingId,
                currency.getId(),
                start,
                end
        );

        ObjectNode payload = objectMapper.valueToTree(request);
        payload.put("basePremiumAmount", new BigDecimal("-100.00"));

        var result = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, auth)
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
        // seed
        Seed seed = seedGeography();
        Currency currency = seedCurrency();

        Broker broker = seedBrokerAndGet();
        String brokerAuth = brokerToken(broker.getEmail());

        long clientId = seedClientAndGetId(seed, broker.getEmail());
        long buildingId = seedBuildingAndGetId(clientId, seed, broker.getEmail());

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusMonths(1);

        // create first policy (ok)
        PolicyCreateRequestDTO create1 = aPolicyCreateRequest(clientId, buildingId, currency.getId(), start, end);

        var res1 = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, brokerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        long policyId1 = objectMapper.readTree(res1.getResponse().getContentAsByteArray()).get("id").asLong();

        // deactivate broker (admin)
        mockMvc.perform(post("/api/admin/brokers/deactivate/{id}", broker.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk());

        long before = policyRepository.count();

        // create second policy => should be rejected
        PolicyCreateRequestDTO create2 = aPolicyCreateRequest(clientId, buildingId, currency.getId(), start, end);

        mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, brokerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create2)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertEquals(before, policyRepository.count());

        // existing policy unaffected
        Policy existing = policyRepository.findById(policyId1).orElseThrow();
        assertEquals(PolicyStatus.DRAFT, existing.getStatus());
        assertEquals(broker.getId(), existing.getBroker().getId());
    }

    // -------------------  HELPERS  -------------------

    private long seedBuildingAndGetId(long clientId, Seed seed,String brokerEmail) throws Exception {
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
                        .header(HttpHeaders.AUTHORIZATION,brokerToken(brokerEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(buildingResponse.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private long seedClientAndGetId(Seed seed,String brokerEmail) throws Exception {
        var clientRequest = aClientRequestWithAddress(seed.city().getId(), "Str. Gheo", "10A");

        var clientResponse = mockMvc.perform(post("/api/brokers/clients")
                        .header(HttpHeaders.AUTHORIZATION,brokerToken(brokerEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").exists()
                ).andDo(print()).andReturn();

        return objectMapper.readTree(clientResponse.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private Broker seedBrokerAndGet() throws Exception {
        BrokerRequestDTO brokerRequest = aBrokerRequest();

        var brokerResponse = mockMvc.perform(post("/api/admin/brokers")
                        .header(HttpHeaders.AUTHORIZATION,adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brokerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        long id = objectMapper.readTree(brokerResponse.getResponse().getContentAsByteArray()).get("id").asLong();
        return brokerRepository.findById(id).orElseThrow();
    }

}
