package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.TestSecurityUsersConfig;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityUsersConfig.class)
@Transactional
class CurrencyIT {

    @Autowired MockMvc mockMvc;
    @Autowired CurrencyRepository currencyRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createCurrency_shouldReturn201_andIsActiveTrue() throws Exception {
        CurrencyRequestDTO request = aCurrencyRequest();

        mockMvc.perform(post("/api/admin/currencies")
                        .with(httpBasic("admin", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", is("RON")))
                .andExpect(jsonPath("$.name", is("Romanian Leu")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void deactivateCurrency_shouldReturn200_andSetInactive_whenNoActivePolicies() throws Exception {
        Currency currency = currencyRepository.save(aCurrencyWithoutId());

        mockMvc.perform(post("/api/admin/currencies/{id}/deactivate", currency.getId())
                        .with(httpBasic("admin", "pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(currency.getId().intValue())))
                .andExpect(jsonPath("$.code", is("RON")))
                .andExpect(jsonPath("$.isActive", is(false)));

        Currency reloaded = currencyRepository.findById(currency.getId()).orElseThrow();
        assertFalse(reloaded.isActive());
    }
}

