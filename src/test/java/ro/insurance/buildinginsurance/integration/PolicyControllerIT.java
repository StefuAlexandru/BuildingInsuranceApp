package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    private static final String USER = "broker";
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
        var clientRequest = aClientRequestWithAddress(seed.city().getId(), "Str. Gheo", "10A");

        var clientResponse = mockMvc.perform(post("/api/brokers/clients")
                .with(httpBasic(USER,PASS))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").exists()
                ).andReturn();

        long clientId = objectMapper.readTree(clientResponse.getResponse().getContentAsByteArray()).get("id").asLong();

        // ---------- CREATE BUILDING ----------
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

        var buildingRes = mockMvc.perform(post("/api/brokers/buildings")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        long buildingId = objectMapper.readTree(buildingRes.getResponse().getContentAsByteArray())
                .get("id").asLong();

        PolicyCreateRequestDTO createRequest = aPolicyCreateRequest(
                clientId,
                buildingId,
                broker.getId(),
                currency.getId(),
                LocalDate.of(2026, 2, 5),   // > today din FixedClockConfig (2026-02-04)
                LocalDate.of(2026, 3, 5)
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

                        jsonPath("$.startDate").value("2026-02-05"),
                        jsonPath("$.endDate").value("2026-03-05"),
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

        assertEquals(LocalDate.of(2026, 2, 5), draft.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 5), draft.getEndDate());
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

}
