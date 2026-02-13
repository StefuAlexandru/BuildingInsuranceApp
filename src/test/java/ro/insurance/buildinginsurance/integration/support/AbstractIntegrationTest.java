package ro.insurance.buildinginsurance.integration.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.security.AppSecurityProps;
import ro.insurance.buildinginsurance.application.security.jwt.JwtService;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired protected JwtService jwtService;
    @Autowired protected AppSecurityProps appSecurityProps;

    protected String adminToken() {
        return "Bearer " + jwtService.generateAccessToken(appSecurityProps.admin().username(), List.of("ADMIN"));
    }

    protected String brokerToken(String email) {
        return "Bearer " + jwtService.generateAccessToken(email, List.of("BROKER"));
    }
}
