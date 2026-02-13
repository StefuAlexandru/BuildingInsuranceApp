package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.integration.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.data_factory.CityTestDataFactory.aCityWithoutId;
import static ro.insurance.buildinginsurance.util.data_factory.CountryTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.data_factory.CountyTestDataFactory.aCountyWithoutId;
import static ro.insurance.buildinginsurance.util.data_factory.RiskFactorConfigurationTestDataFactory.createRiskFactorRequestBody;

class RiskFactorConfigurationControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    private static final String BASE = "/api/admin/risk-factors";

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createRiskFactor_shouldReturn201_andPersist_thenUpdate_andPersist() throws Exception {
        // --------- SETUP GEO ----------
        Country country = aCountryWithoutId();
        country = countryRepository.save(country);

        County county = aCountyWithoutId();
        county.setCountry(country);
        county = countyRepository.save(county);

        City city = aCityWithoutId();
        city.setCounty(county);
        city = cityRepository.save(city);

        Long cityId = city.getId();

        // --------- CREATE ----------
        String body = createRiskFactorRequestBody(
                objectMapper,
                RiskFactorLevel.CITY,
                cityId,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 29),
                LocalDate.of(2026, 12, 31)
        );

        var createResult = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION,adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.level").value("CITY"))
                .andExpect(jsonPath("$.referenceId").value(cityId))
                .andExpect(jsonPath("$.buildingType").doesNotExist())
                .andExpect(jsonPath("$.effectiveFrom").value("2026-01-29"))
                .andExpect(jsonPath("$.effectiveTo").value("2026-12-31"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long id = created.get("id").asLong();

        // --------- VERIFY IN DB ----------
        RiskFactorConfiguration fromDbAfterCreate = riskFactorConfigurationRepository.findById(id).orElseThrow();

        assertAll(
                () -> assertEquals(RiskFactorLevel.CITY, fromDbAfterCreate.getLevel()),
                () -> assertEquals(cityId, fromDbAfterCreate.getReferenceId()),
                () -> assertNull(fromDbAfterCreate.getBuildingType()),
                () -> assertEquals(0, new BigDecimal("0.1000").compareTo(fromDbAfterCreate.getAdjustmentPercentage())),
                () -> assertEquals(LocalDate.of(2026, 1, 29), fromDbAfterCreate.getEffectiveFrom()),
                () -> assertEquals(LocalDate.of(2026, 12, 31), fromDbAfterCreate.getEffectiveTo())
        );

        // --------- UPDATE ----------
        String updateBody = createRiskFactorRequestBody(
                objectMapper,
                RiskFactorLevel.CITY,
                cityId,
                null,
                new BigDecimal("0.0700"),
                LocalDate.of(2026, 2, 1),
                null
        );

        mockMvc.perform(put(BASE + "/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION,adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.level").value("CITY"))
                .andExpect(jsonPath("$.referenceId").value(cityId))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-02-01"))
                .andExpect(jsonPath("$.effectiveTo").doesNotExist());

        // --------- VERIFY IN DB ----------
        RiskFactorConfiguration fromDbAfterUpdate = riskFactorConfigurationRepository.findById(id).orElseThrow();

        assertAll(
                () -> assertEquals(RiskFactorLevel.CITY, fromDbAfterUpdate.getLevel()),
                () -> assertEquals(cityId, fromDbAfterUpdate.getReferenceId()),
                () -> assertNull(fromDbAfterUpdate.getBuildingType()),
                () -> assertEquals(0, new BigDecimal("0.0700").compareTo(fromDbAfterUpdate.getAdjustmentPercentage())),
                () -> assertEquals(LocalDate.of(2026, 2, 1), fromDbAfterUpdate.getEffectiveFrom()),
                () -> assertNull(fromDbAfterUpdate.getEffectiveTo())
        );
    }
}
