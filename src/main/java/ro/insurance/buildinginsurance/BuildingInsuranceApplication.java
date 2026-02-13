package ro.insurance.buildinginsurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ro.insurance.buildinginsurance.application.security.AppSecurityProps;
import ro.insurance.buildinginsurance.application.security.jwt.JwtProperties;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({AppSecurityProps.class, JwtProperties.class})
public class BuildingInsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildingInsuranceApplication.class, args);
    }

}
