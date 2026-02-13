package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.integration.support.AbstractIntegrationTest;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientRequestDTO;
import ro.insurance.buildinginsurance.domain.enums.ClientType;
import ro.insurance.buildinginsurance.application.mapper.CityMapper;
import ro.insurance.buildinginsurance.application.mapper.ClientMapper;
import ro.insurance.buildinginsurance.application.mapper.CountryMapper;
import ro.insurance.buildinginsurance.application.mapper.CountyMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ro.insurance.buildinginsurance.util.data_factory.CityTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.data_factory.ClientTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.data_factory.CountryTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.data_factory.CountyTestDataFactory.*;

class ClientControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired
    ClientRepository clientRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    CountyRepository countyRepository;
    @Autowired
    CountryRepository countryRepository;
    @Autowired
    ClientChangeHistoryRepository clientChangeHistoryRepository;

    @Autowired ClientMapper clientMapper;
    @Autowired CityMapper cityMapper;
    @Autowired CountyMapper countyMapper;
    @Autowired CountryMapper countryMapper;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE = "/api/brokers/clients";

    private record Seed(Country country, County county, City city, Client client) {}
    private record GeoSeed(Country country, County county, City city) {}

    private Seed seedClientAndCity() {
        Country country = countryRepository.save(countryMapper.toEntity(aCountryRequest()));
        County county = countyRepository.save(countyMapper.toEntity(aCountyRequest(), country));
        City city = cityRepository.save(cityMapper.toEntity(aCityRequest(), county));

        ClientRequestDTO clientRequestDTO = aClientRequest();
        Client client = clientRepository.save(clientMapper.toEntity(clientRequestDTO, city));

        return new Seed(country, county, city, client);
    }

    private GeoSeed seedGeoOnly() {
        Country country = countryRepository.save(countryMapper.toEntity(aCountryRequest()));
        County county = countyRepository.save(countyMapper.toEntity(aCountyRequest(), country));
        City city = cityRepository.save(cityMapper.toEntity(aCityRequest(), county));
        return new GeoSeed(country, county, city);
    }

    // ----------------------  CREATE  ----------------------

    @Test
    void createClient_success_withoutAddress_trimsFields() throws Exception {
        String body = createClientRequestBody("   John Doe   ", ClientType.INDIVIDUAL, "   CNP123   ", "jonx@x.com", "0783728349", null, null, null);

        var result = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, adminToken())
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
        GeoSeed geo = seedGeoOnly();

        String body = createClientRequestBody("   John Doe   ", ClientType.INDIVIDUAL, "   CNP123   ", "jonx@x.com", "0783728349", geo.city.getId(), "Str. test", "10A");

        var result = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, adminToken())
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
        assertThat(saved.getAddressCity().getId()).isEqualTo(geo.city.getId());
    }

    @Test
    void createClient_shouldReturn400_whenInvalid_email_phone_required() throws Exception {
        String body = createClientRequestBody("  ", ClientType.INDIVIDUAL, "   CNP123   ", "  bad -email", "abc", null, null, null);

        mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION, adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isBadRequest(),

                        jsonPath("$").isArray(),
                        jsonPath("$.length()").value(3),

                        jsonPath("$[*].code").exists(),
                        jsonPath("$[*].message").exists(),

                        jsonPath("$[*].code", hasItem("NotBlank")),
                        jsonPath("$[*].code", hasItem("Email")),
                        jsonPath("$[*].code", hasItem("Pattern"))
                );
    }

    // ----------------------  GET  ----------------------

    @Test
    void getClientById_success() throws Exception {
        Seed s = seedClientAndCity();

        mockMvc.perform(get(BASE + "/{id}", s.client.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(s.client.getId().intValue()),
                        jsonPath("$.clientType").value(s.client.getClientType().name()),
                        jsonPath("$.name").value(s.client.getName()),
                        jsonPath("$.identificationNumber").value(s.client.getIdentificationNumber()),
                        jsonPath("$.email").value(s.client.getEmail()),
                        jsonPath("$.phone").value(s.client.getPhone()),

                        jsonPath("$.city.id").value(s.city.getId().intValue()),
                        jsonPath("$.city.name").value(s.city.getName()),
                        jsonPath("$.city.countyName").value(s.county.getName()),
                        jsonPath("$.city.countryName").value(s.country.getName()),

                        jsonPath("$.addressStreet").value(s.client.getAddressStreet()),
                        jsonPath("$.addressNumber").value(s.client.getAddressNumber())
                );
    }

    // ----------------------  UPDATE  ----------------------

    @Test
    void updateClient_success() throws Exception {
        Seed s = seedClientAndCity();

        String body = createClientUpdateRequestBody("   John Doe   ", "jonUpdated@xa.ro", "0777777777", s.city.getId(), "   Str. test   ", "  10A  ");

        mockMvc.perform(put(BASE + "/{id}", s.client.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(s.client.getId().intValue()),
                        jsonPath("$.name").value("John Doe"),
                        jsonPath("$.email").value("jonUpdated@xa.ro"),
                        jsonPath("$.phone").value("0777777777"),
                        jsonPath("$.addressStreet").value("Str. test"),
                        jsonPath("$.addressNumber").value("10A"),
                        jsonPath("$.city.id").value(s.city.getId().intValue())
                );

        Client updated = clientRepository.findById(s.client.getId()).orElseThrow(() -> new AssertionError("Client not found after update"));

        assertThat(updated.getName()).isEqualTo("John Doe");
        assertThat(updated.getEmail()).isEqualTo("jonUpdated@xa.ro");
        assertThat(updated.getPhone()).isEqualTo("0777777777");
        assertThat(updated.getAddressStreet()).isEqualTo("Str. test");
        assertThat(updated.getAddressNumber()).isEqualTo("10A");
        assertThat(updated.getAddressCity().getId()).isEqualTo(s.city.getId());

        assertThat(updated.getIdentificationNumber()).isEqualTo(s.client.getIdentificationNumber());
        assertThat(updated.getClientType()).isEqualTo(s.client.getClientType());
    }

    // ----------------------  PATCH  ----------------------

    @Test
    void patchIdentity_success_changesTypeAndId_andWritesHistoryRows() throws Exception {
        Seed s = seedClientAndCity();

        String oldType = s.client.getClientType().name();
        String oldIdNo = s.client.getIdentificationNumber();

        String body = """
            {
              "newClientType": "COMPANY",
              "newIdentificationNumber": "   CUI999   ",
              "reason": "  correction  "
            }
            """;

        mockMvc.perform(patch(BASE + "/{id}/identity", s.client.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.clientType").value("COMPANY"),
                        jsonPath("$.identificationNumber").value("CUI999")
                );

        Client reloaded = clientRepository.findById(s.client.getId()).orElseThrow(() -> new AssertionError("Client not found after patch"));

        assertThat(reloaded.getClientType()).isEqualTo(ClientType.COMPANY);
        assertThat(reloaded.getIdentificationNumber()).isEqualTo("CUI999");

        var rows = clientChangeHistoryRepository.findAll().stream()
                .filter(r -> r.getClientId().equals(s.client.getId()))
                .toList();

        assertThat(rows).hasSize(2);

        var typeRow = rows.stream()
                .filter(r -> r.getField() == ClientChangeHistory.Field.CLIENT_TYPE)
                .findFirst().orElseThrow();

        assertThat(typeRow.getOldValue()).isEqualTo(oldType);
        assertThat(typeRow.getNewValue()).isEqualTo("COMPANY");
        assertThat(typeRow.getReason()).isEqualTo("correction");

        var idRow = rows.stream()
                .filter(r -> r.getField() == ClientChangeHistory.Field.IDENTIFICATION_NUMBER)
                .findFirst().orElseThrow();

        assertThat(idRow.getOldValue()).isEqualTo(oldIdNo);
        assertThat(idRow.getNewValue()).isEqualTo("CUI999");
        assertThat(idRow.getReason()).isEqualTo("correction");
    }

    // ----------------------  DELETE  ----------------------

    @Test
    void deleteClient_success_thenGet_returnsNotFound() throws Exception {
        Seed s = seedClientAndCity();

        mockMvc.perform(delete(BASE + "/{id}", s.client.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(BASE + "/{id}", s.client.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isNotFound());
    }
}
