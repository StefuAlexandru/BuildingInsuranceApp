package ro.insurance.buildinginsurance.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.application.mapper.CityMapper;
import ro.insurance.buildinginsurance.application.mapper.CountryMapper;
import ro.insurance.buildinginsurance.application.mapper.CountyMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryRequest;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GeographyIT {

    @Autowired MockMvc mockMvc;

    @Autowired CountryRepository countryRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CityRepository cityRepository;

    @Autowired CountryMapper countryMapper;
    @Autowired CountyMapper countyMapper;
    @Autowired CityMapper cityMapper;

    private void seedGeo() {
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        CityRequestDTO cityRequestDTO = aCityRequest();

        Country country = countryRepository.save(countryMapper.toEntity(countryRequestDTO));
        County county = countyRepository.save(countyMapper.toEntity(countyRequestDTO, country));
        cityRepository.save(cityMapper.toEntity(cityRequestDTO, county));
    }

    @ParameterizedTest(name = "GET {0} returns paged content")
    @ValueSource(strings = {
            "/api/geography/countries",
            "/api/geography/counties",
            "/api/geography/cities"
    })
    void getGeography(String uri) throws Exception {
        seedGeo();

        mockMvc.perform(get(uri)
                        .with(httpBasic("broker", "pass"))
                        .queryParam("page", "0")
                        .queryParam("size", "5"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content.length()").value(greaterThan(0)),
                        jsonPath("$.content[0].id").exists(),
                        jsonPath("$.content[0].name").exists()
                );
    }
}
