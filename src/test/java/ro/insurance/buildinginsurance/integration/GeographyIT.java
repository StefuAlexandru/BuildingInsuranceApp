package ro.insurance.buildinginsurance.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.entity.Country;
import ro.insurance.buildinginsurance.entity.County;
import ro.insurance.buildinginsurance.mapper.CityMapper;
import ro.insurance.buildinginsurance.mapper.CountryMapper;
import ro.insurance.buildinginsurance.mapper.CountyMapper;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.CountryRepository;
import ro.insurance.buildinginsurance.repository.CountyRepository;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryRequest;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyRequest;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    void getCountries() throws Exception {
        seedGeo();

        mockMvc.perform(get("/api/brokers/countries")
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

    @Test
    void getCounties() throws Exception {
        seedGeo();

        mockMvc.perform(get("/api/brokers/counties")
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

    @Test
    void getCities() throws Exception {
        seedGeo();

        mockMvc.perform(get("/api/brokers/cities")
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
