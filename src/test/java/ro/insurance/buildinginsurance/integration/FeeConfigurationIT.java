package ro.insurance.buildinginsurance.integration;

import com.fasterxml.jackson.databind.JsonNode;
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
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static ro.insurance.buildinginsurance.util.FeeConfigurationTestDataFactory.createFeeRequestBody;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityUsersConfig.class)
@Transactional
class FeeConfigurationIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    FeeConfigurationRepository feeConfigurationRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createFee_shouldReturn201_andPersist_whenUpdate() throws Exception {
        // --------- CREATE ----------
        String body = createFeeRequestBody(
                objectMapper,
                "Standard admin fee",
                FeeType.ADMIN_FEE,
                new BigDecimal("5.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );


        var createResult = mockMvc.perform(post("/api/admin/fees")
                        .with(httpBasic("admin", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Standard admin fee"))
                .andExpect(jsonPath("$.feeType").value("ADMIN_FEE"))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-01-01"))
                .andExpect(jsonPath("$.effectiveTo").value("2026-12-31"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long id = created.get("id").asLong();

        // --------- UPDATE ----------
        String updateBody = createFeeRequestBody(
                objectMapper,
                "Standard admin fee UPDATED",
                FeeType.ADMIN_FEE,
                new BigDecimal("6.00"),
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2027, 12, 31)
        );


        mockMvc.perform(put("/api/admin/fees/{id}", id)
                        .with(httpBasic("admin", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Standard admin fee UPDATED"))
                .andExpect(jsonPath("$.feeType").value("ADMIN_FEE"))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-02-02"))
                .andExpect(jsonPath("$.effectiveTo").value("2027-12-31"));

        // --------- VERIFY IN DB ----------
        FeeConfiguration fromDb = feeConfigurationRepository.findById(id).orElseThrow();

        assertAll(
                () -> assertEquals("Standard admin fee UPDATED", fromDb.getName()),
                () -> assertEquals(FeeType.ADMIN_FEE, fromDb.getFeeType()),
                () -> assertEquals(0, new BigDecimal("6.00").compareTo(fromDb.getPercentage())),
                () -> assertEquals(LocalDate.of(2026, 2, 2), fromDb.getEffectiveFrom()),
                () -> assertEquals(LocalDate.of(2027, 12, 31), fromDb.getEffectiveTo())
        );
    }



}
