package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.integration.support.AbstractIntegrationTest;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyRequestDTO;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.data_factory.CurrencyTestDataFactory.*;

class CurrencyControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired CurrencyRepository currencyRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE = "/api/admin/currencies";

    @Test
    void createThenDeactivateCurrency_shouldPersistStatusChange() throws Exception {
        // -------- CREATE ----------
        CurrencyRequestDTO request = aCurrencyRequest();

        var createResult = mockMvc.perform(post(BASE)
                        .header(HttpHeaders.AUTHORIZATION,adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", is("RON")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").asLong();

        // -------- DEACTIVATE ----------
        mockMvc.perform(post(BASE + "/{id}/deactivate", id)
                        .header(HttpHeaders.AUTHORIZATION,adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.code", is("RON")))
                .andExpect(jsonPath("$.isActive", is(false)));

        // -------- ACTIVATE ----------
        mockMvc.perform(post(BASE + "/{id}/activate", id)
                        .header(HttpHeaders.AUTHORIZATION,adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.code", is("RON")))
                .andExpect(jsonPath("$.isActive", is(true)));

        // -------- VERIFY IN DB ----------
        Currency reloaded = currencyRepository.findById(id).orElseThrow();
        assertTrue(reloaded.isActive());
    }
}

