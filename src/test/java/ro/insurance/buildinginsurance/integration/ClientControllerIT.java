package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.ClientRepository;
import ro.insurance.buildinginsurance.repository.CountryRepository;
import ro.insurance.buildinginsurance.repository.CountyRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithoutId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.createClientRequestBody;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryWithoutId;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyWithoutId;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ClientControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    CountyRepository countyRepository;

    @Autowired
    CountryRepository countryRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createClient_success_withoutAddress_trimsFields() throws Exception {
        String body = createClientRequestBody("   John Doe   ", ClientType.INDIVIDUAL, "   CNP123   ", "jonx@x.com", "0783728349", null, null, null);

        var result = mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        Client saved = clientRepository.findById(id).orElseThrow(() -> new AssertionError("Client was NOT persisted"));

        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getIdentificationNumber()).isEqualTo("CNP123");
        assertThat(saved.getEmail()).isEqualTo("jonx@x.com");
        assertThat(saved.getPhone()).isEqualTo("0783728349");

        assertThat(saved.getAddressStreet()).isNull();
        assertThat(saved.getAddressNumber()).isNull();
        assertThat(saved.getAddressCity()).isNull();
    }

    @Test
    void createClient_success_withAddress_trimsFields() throws Exception {
        countryRepository.save(aCountryWithoutId());
        countyRepository.save(aCountyWithoutId());
        City city = cityRepository.save(aCityWithoutId());

        String body = createClientRequestBody("   John Doe   ", ClientType.INDIVIDUAL, "   CNP123   ", "jonx@x.com", "0783728349", city.getId(), "Str. test", "10A");

        var result = mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        Client saved = clientRepository.findById(id).orElseThrow(() -> new AssertionError("Client was NOT persisted"));

        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getIdentificationNumber()).isEqualTo("CNP123");
        assertThat(saved.getEmail()).isEqualTo("jonx@x.com");
        assertThat(saved.getPhone()).isEqualTo("0783728349");

        assertThat(saved.getAddressStreet()).isEqualTo("Str. test");
        assertThat(saved.getAddressNumber()).isEqualTo("10A");
        assertThat(saved.getAddressCity()).isNotNull();
        assertThat(saved.getAddressCity().getId()).isEqualTo(city.getId());
    }

}
