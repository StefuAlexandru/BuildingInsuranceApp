package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.config.FixedClockConfig;
import ro.insurance.buildinginsurance.config.TestSecurityUsersConfig;
import ro.insurance.buildinginsurance.application.mapper.CityMapper;
import ro.insurance.buildinginsurance.application.mapper.CountryMapper;
import ro.insurance.buildinginsurance.application.mapper.CountyMapper;
import ro.insurance.buildinginsurance.application.mapper.CurrencyMapper;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRowDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerRequest;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientRequestWithAddress;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryRequest;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyRequest;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyRequest;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityUsersConfig.class, FixedClockConfig.class})
@Transactional
class AdminReportControllerIT {

    @Autowired MockMvc mockMvc;

    @Autowired CountryRepository countryRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CityRepository cityRepository;
    @Autowired CurrencyRepository currencyRepository;
    @Autowired BrokerRepository brokerRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired BuildingRepository buildingRepository;
    @Autowired PolicyRepository policyRepository;

    @Autowired CountryMapper countryMapper;
    @Autowired CountyMapper countyMapper;
    @Autowired CityMapper cityMapper;
    @Autowired CurrencyMapper currencyMapper;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String ADMIN = "admin";
    private static final String USER  = "broker@email.com";
    private static final String PASS  = "pass";

    private static final String BASE = "/api/admin/reports";

    private static final LocalDate FROM = LocalDate.of(2026, 2, 1);
    private static final LocalDate TO   = LocalDate.of(2026, 2, 28);

    private record SeedGeo(Country country, County c1, County c2, City city1, City city2) {}
    private record Seed(Broker brokerA, Broker brokerB, Currency eur, long clientId, long buildingResidentialId, long buildingIndustrialId, SeedGeo geo) {}

    private Seed seed;

    @BeforeEach
    void setup() throws Exception {
        SeedGeo geo = seedGeographyTwoCountiesTwoCities();
        Currency eur = seedCurrencyEUR();

        Broker brokerA = seedBroker("BR-001", "Broker A", "broker@email.com");
        Broker brokerB = seedBroker("BR-002", "Broker B", "broker2@email.com");

        long clientId = seedClientAndGetId(geo);

        long residentialId = seedBuildingAndGetId(clientId, geo.city1().getId(), BuildingType.RESIDENTIAL);
        long industrialId   = seedBuildingAndGetId(clientId, geo.city2().getId(), BuildingType.INDUSTRIAL);

        // policies (EUR) within range:
        createPolicy("POL-1", PolicyStatus.ACTIVE,    LocalDate.of(2026, 2, 10), residentialId, clientId, brokerA.getId(), eur.getId(), new BigDecimal("100.00"));
        createPolicy("POL-2", PolicyStatus.ACTIVE,    LocalDate.of(2026, 2, 11), industrialId,   clientId, brokerB.getId(), eur.getId(), new BigDecimal("50.00"));
        createPolicy("POL-3", PolicyStatus.CANCELLED, LocalDate.of(2026, 2, 12), industrialId,   clientId, brokerA.getId(), eur.getId(), new BigDecimal("30.00"));

        // policy outside interval (must NOT be counted)
        createPolicy("POL-OUT", PolicyStatus.ACTIVE, LocalDate.of(2026, 3, 10), residentialId, clientId, brokerA.getId(), eur.getId(), new BigDecimal("999.00"));

        this.seed = new Seed(brokerA, brokerB, eur, clientId, residentialId, industrialId, geo);
    }

    @Test
    void policiesByCountry_shouldAggregateCorrectly() throws Exception {
        var mvc = mockMvc.perform(get(BASE + "/policies-by-country")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("currency", seed.eur().getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        ReportRowDTO[] rows = objectMapper.readValue(mvc.getResponse().getContentAsByteArray(), ReportRowDTO[].class);
        assertEquals(1, rows.length);

        ReportRowDTO r = rows[0];

        assertEquals(seed.geo().country().getId().toString(), r.groupKey());
        assertEquals(seed.geo().country().getName(), r.groupName());

        assertEquals(seed.eur().getId(), r.currencyId());
        assertEquals(seed.eur().getCode(), r.currencyCode());

        assertEquals(3L, r.policyCount());
        assertEquals(0, new BigDecimal("180.00").compareTo(r.totalFinalPremium()));
        // EUR rateToBase = 2.00 -> 180 * 2 = 360
        assertEquals(0, new BigDecimal("360.00").compareTo(r.totalFinalPremiumInBaseCurrency()));
    }

    @Test
    void policiesByCounty_shouldReturnTwoRows_withCorrectSums() throws Exception {
        var mvc = mockMvc.perform(get(BASE + "/policies-by-county")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("currency", seed.eur().getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        ReportRowDTO[] arr = objectMapper.readValue(mvc.getResponse().getContentAsByteArray(), ReportRowDTO[].class);
        assertEquals(2, arr.length);

        Map<String, ReportRowDTO> rows = Arrays.stream(arr)
                .collect(Collectors.toMap(ReportRowDTO::groupKey, Function.identity()));

        // county1 (residential -> POL-1)
        ReportRowDTO c1 = rows.get(seed.geo().c1().getId().toString());
        assertNotNull(c1);
        assertEquals(seed.geo().c1().getName(), c1.groupName());
        assertEquals(1L, c1.policyCount());
        assertEquals(0, new BigDecimal("100.00").compareTo(c1.totalFinalPremium()));
        assertEquals(0, new BigDecimal("200.00").compareTo(c1.totalFinalPremiumInBaseCurrency()));

        // county2 (industrial -> POL-2 + POL-3)
        ReportRowDTO c2 = rows.get(seed.geo().c2().getId().toString());
        assertNotNull(c2);
        assertEquals(seed.geo().c2().getName(), c2.groupName());
        assertEquals(2L, c2.policyCount());
        assertEquals(0, new BigDecimal("80.00").compareTo(c2.totalFinalPremium()));
        assertEquals(0, new BigDecimal("160.00").compareTo(c2.totalFinalPremiumInBaseCurrency()));
    }

    @Test
    void policiesByCity_withBuildingTypeFilter_shouldRestrictToResidential() throws Exception {
        var mvc = mockMvc.perform(get(BASE + "/policies-by-city")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("currency", seed.eur().getId().toString())
                        .param("buildingType", BuildingType.RESIDENTIAL.name()))
                .andExpect(status().isOk())
                .andReturn();

        ReportRowDTO[] rows = objectMapper.readValue(mvc.getResponse().getContentAsByteArray(), ReportRowDTO[].class);
        assertEquals(1, rows.length);

        ReportRowDTO reportRowDTO = rows[0];
        assertEquals(seed.geo().city1().getId().toString(), reportRowDTO.groupKey());
        assertEquals(seed.geo().city1().getName(), reportRowDTO.groupName());
        assertEquals(1L, reportRowDTO.policyCount());
        assertEquals(0, new BigDecimal("100.00").compareTo(reportRowDTO.totalFinalPremium()));
        assertEquals(0, new BigDecimal("200.00").compareTo(reportRowDTO.totalFinalPremiumInBaseCurrency()));
    }

    @Test
    void policiesByBroker_withStatusFilter_shouldReturnOnlyCancelled() throws Exception {
        var mvc = mockMvc.perform(get(BASE + "/policies-by-broker")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("currency", seed.eur().getId().toString())
                        .param("status", PolicyStatus.CANCELLED.name()))
                .andExpect(status().isOk())
                .andReturn();

        ReportRowDTO[] rows = objectMapper.readValue(mvc.getResponse().getContentAsByteArray(), ReportRowDTO[].class);
        assertEquals(1, rows.length);

        ReportRowDTO r = rows[0];
        assertEquals(seed.brokerA().getId().toString(), r.groupKey());
        assertEquals(seed.brokerA().getName(), r.groupName());
        assertEquals(1L, r.policyCount());
        assertEquals(0, new BigDecimal("30.00").compareTo(r.totalFinalPremium()));
        assertEquals(0, new BigDecimal("60.00").compareTo(r.totalFinalPremiumInBaseCurrency()));
    }

    @Test
    void reports_missingCurrency_shouldReturn400() throws Exception {
        mockMvc.perform(get(BASE + "/policies-by-city")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", FROM.toString())
                        .param("to", TO.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reports_whenFromAfterTo_shouldReturn400_andContainErrorCode() throws Exception {
        var res = mockMvc.perform(get(BASE + "/policies-by-city")
                        .with(httpBasic(ADMIN, PASS))
                        .param("from", LocalDate.of(2026, 3, 1).toString())
                        .param("to", LocalDate.of(2026, 2, 1).toString())
                        .param("currency", seed.eur().getId().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertTrue(body.contains("REPORT_INVALID_REQUEST") || body.toLowerCase().contains("invalid"),
                "Expected REPORT_INVALID_REQUEST in body, got: " + body);
    }

    // ------------------- seed helpers -------------------

    private SeedGeo seedGeographyTwoCountiesTwoCities() {
        var countryReq = aCountryRequest();
        Country country = countryRepository.save(countryMapper.toEntity(countryReq));

        var countyRequest1 = aCountyRequest();
        County county1 = countyMapper.toEntity(countyRequest1, country);
        county1 = countyRepository.save(county1);

        var countyRequest2 = aCountyRequest();
        County county2 = countyMapper.toEntity(countyRequest2, country);
        // force uniqueness
        county2.setName(county1.getName() + " 2");
        county2 = countyRepository.save(county2);

        var cityRequest1 = aCityRequest();
        City city1 = cityMapper.toEntity(cityRequest1, county1);
        city1 = cityRepository.save(city1);

        var cityRequest2 = aCityRequest();
        City city2 = cityMapper.toEntity(cityRequest2, county2);
        // force uniqueness
        city2.setName(city1.getName() + " 2");
        city2 = cityRepository.save(city2);

        return new SeedGeo(country, county1, county2, city1, city2);
    }

    private Currency seedCurrencyEUR() {
        var req = aCurrencyRequest();
        Currency eur = currencyMapper.toEntity(req);

        eur.setCode("EUR");
        eur.setName("Euro");
        eur.setExchangeRateToBase(new BigDecimal("2.00"));
        eur.setActive(true);

        return currencyRepository.save(eur);
    }

    private Broker seedBroker(String brokerCode, String name, String email) throws Exception {
        BrokerRequestDTO base = aBrokerRequest();
        ObjectNode payload = objectMapper.valueToTree(base);
        payload.put("brokerCode", brokerCode);
        payload.put("name", name);
        payload.put("email", email);

        var res = mockMvc.perform(post("/api/admin/brokers")
                        .with(httpBasic(ADMIN, PASS))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        long id = objectMapper.readTree(res.getResponse().getContentAsByteArray()).get("id").asLong();
        return brokerRepository.findById(id).orElseThrow();
    }

    private long seedClientAndGetId(SeedGeo geo) throws Exception {
        var clientReq = aClientRequestWithAddress(geo.city1().getId(), "Str. Test", "10A");

        var res = mockMvc.perform(post("/api/brokers/clients")
                        .with(httpBasic(USER, PASS))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientReq)))
                .andExpectAll(status().isCreated(), jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private long seedBuildingAndGetId(long clientId, long cityId, BuildingType buildingType) throws Exception {
        BuildingRequestDTO b = aBuildingRequest();

        BuildingRequestDTO buildingRequestDTO = new BuildingRequestDTO(
                clientId, cityId, b.street(), b.number(), b.constructionYear(),
                buildingType, b.floors(), b.surfaceArea(), b.insuredValue(), b.floodZone(), b.earthquakeZone()
        );

        var res = mockMvc.perform(post("/api/brokers/buildings")
                        .with(httpBasic(USER, PASS))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequestDTO)))
                .andExpectAll(status().isCreated(), jsonPath("$.id").exists())
                .andReturn();

        return objectMapper.readTree(res.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private void createPolicy(String policyNumber,
                              PolicyStatus status,
                              LocalDate startDate,
                              long buildingId,
                              long clientId,
                              long brokerId,
                              long currencyId,
                              BigDecimal finalPremium) {

        Building building = buildingRepository.findById(buildingId).orElseThrow();
        Client client = clientRepository.findById(clientId).orElseThrow();
        Broker broker = brokerRepository.findById(brokerId).orElseThrow();
        Currency currency = currencyRepository.findById(currencyId).orElseThrow();

        Policy p = new Policy();
        p.setPolicyNumber(policyNumber);
        p.setStatus(status);

        p.setClient(client);
        p.setBuilding(building);
        p.setBroker(broker);
        p.setCurrency(currency);

        p.setStartDate(startDate);
        p.setEndDate(startDate.plusMonths(1));

        p.setBasePremiumAmount(finalPremium);
        p.setFinalPremiumAmount(finalPremium);

        policyRepository.save(p);
    }
}
