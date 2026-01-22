package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingUpdateRequestDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.entity.*;
import ro.insurance.buildinginsurance.mapper.*;
import ro.insurance.buildinginsurance.repository.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class BuildingControllerIT {

    @Autowired MockMvc mockMvc;

    @Autowired BuildingRepository buildingRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    @Autowired ClientMapper clientMapper;
    @Autowired CityMapper cityMapper;
    @Autowired CountyMapper countyMapper;
    @Autowired CountryMapper countryMapper;

    ObjectMapper objectMapper = new ObjectMapper();

    // ----------------------  HELPERS  ----------------------

    private record Seed(Country country, County county, City city, Client client) {}

    private Seed seedClientAndCity() {
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        CityRequestDTO cityRequestDTO = aCityRequest();
        ClientRequestDTO clientRequestDTO = aClientRequest();

        Country country = countryRepository.save(countryMapper.toEntity(countryRequestDTO));
        County county = countyRepository.save(countyMapper.toEntity(countyRequestDTO, country));
        City city = cityRepository.save(cityMapper.toEntity(cityRequestDTO, county));
        Client client = clientRepository.save(clientMapper.toEntity(clientRequestDTO, city));

        return new Seed(country, county, city, client);
    }

    private Long createBuilding(Long clientId, Long cityId) throws Exception {
        BuildingRequestDTO base = aBuildingRequest();

        BuildingRequestDTO req = new BuildingRequestDTO(
                clientId,
                cityId,
                base.street(),
                base.number(),
                base.constructionYear(),
                base.buildingType(),
                base.floors(),
                base.surfaceArea(),
                base.insuredValue(),
                base.floodZone(),
                base.earthquakeZone()
        );

        var result = mockMvc.perform(post("/api/brokers/buildings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void createBuilding_success_201_persistsAndLinksClientAndCity() throws Exception {
        Seed seed = seedClientAndCity();

        Long buildingId = createBuilding(seed.client.getId(), seed.city.getId());

        Building saved = buildingRepository.findById(buildingId).orElseThrow(() -> new AssertionError("Building was NOT persisted"));

        assertThat(saved.getClient()).isNotNull();
        assertThat(saved.getClient().getId()).isEqualTo(seed.client.getId());

        assertThat(saved.getCity()).isNotNull();
        assertThat(saved.getCity().getId()).isEqualTo(seed.city.getId());
    }

    @Test
    void getBuildingById_success_200() throws Exception {
        Seed seed = seedClientAndCity();

        Long buildingId = createBuilding(seed.client.getId(), seed.city.getId());

        mockMvc.perform(get("/api/brokers/buildings/{id}", buildingId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingId.intValue()),
                        jsonPath("$.city.id").value(seed.city.getId().intValue())
                );
    }

    @Test
    void getBuildingsByClientId_paged_containsCreatedBuilding() throws Exception {
        Seed seed = seedClientAndCity();

        Long buildingId = createBuilding(seed.client.getId(), seed.city.getId());

        mockMvc.perform(get("/api/brokers/buildings")
                        .queryParam("clientId", String.valueOf(seed.client.getId()))
                        .queryParam("page", "0")
                        .queryParam("size", "5"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content[*].id", hasItem(buildingId.intValue()))
                );
    }

    @Test
    void updateBuilding_success_200_cityChanges() throws Exception {
        Seed seed = seedClientAndCity();

        CityRequestDTO city2Dto = new CityRequestDTO("Iasi",1L);
        City city2 = cityRepository.save(cityMapper.toEntity(city2Dto, seed.county));

        Long buildingId = createBuilding(seed.client.getId(), seed.city.getId());

        BuildingUpdateRequestDTO baseUpd = aBuildingUpdateRequest();
        BuildingUpdateRequestDTO upd = new BuildingUpdateRequestDTO(
                city2.getId(),
                baseUpd.street(),
                baseUpd.number(),
                baseUpd.constructionYear(),
                baseUpd.buildingType(),
                baseUpd.floors(),
                baseUpd.surfaceArea(),
                baseUpd.insuredValue(),
                baseUpd.floodZone(),
                baseUpd.earthquakeZone()
        );

        mockMvc.perform(put("/api/brokers/buildings/{id}", buildingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(buildingId.intValue()),
                        jsonPath("$.city.id").value(city2.getId().intValue())
                );

        Building reloaded = buildingRepository.findById(buildingId).orElseThrow(() -> new AssertionError("Building not found after update"));

        assertThat(reloaded.getCity().getId()).isEqualTo(city2.getId());
    }

    @Test
    void deleteBuilding_existing_thenGet_returnsNotFound() throws Exception {
        Seed seed = seedClientAndCity();

        Long buildingId = createBuilding(seed.client().getId(), seed.city().getId());

        mockMvc.perform(delete("/api/brokers/buildings/{id}", buildingId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/brokers/buildings/{id}", buildingId))
                .andExpect(status().isNotFound());
    }

}
