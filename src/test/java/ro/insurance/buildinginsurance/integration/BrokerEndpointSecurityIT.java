package ro.insurance.buildinginsurance.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ro.insurance.buildinginsurance.config.FixedClockConfig;
import ro.insurance.buildinginsurance.config.TestSecurityUsersConfig;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityUsersConfig.class, FixedClockConfig.class})
class BrokerEndpointSecurityIT {

    @Autowired
    MockMvc mockMvc;

    private static final String BASE = "/api/brokers/policies";
    private static final String BROKER_USER = "broker@email.com";
    private static final String PASS = "pass";
    private static final String ADMIN = "admin";
    private static final String ADMIN_PASS = "pass";

    @Test
    void brokerEndpoint_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void brokerEndpoint_withWrongPassword_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE).with(httpBasic(BROKER_USER, "nope")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void brokerEndpoint_withBrokerAuth_shouldNotBe401or403() throws Exception {
        var res = mockMvc.perform(get(BASE).with(httpBasic(BROKER_USER, PASS)))
                .andReturn();

        int status = res.getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }


    @Test
    void brokerEndpoint_withAdmin_shouldBe403_ifYouRestrictToBROKEROnly() throws Exception {
        mockMvc.perform(get(BASE).with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isForbidden());
    }
}

