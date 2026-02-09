package ro.insurance.buildinginsurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ro.insurance.buildinginsurance.infrastructure.config.AppSecurityProps;

@SpringBootApplication
@EnableConfigurationProperties(AppSecurityProps.class)
public class BuildingInsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildingInsuranceApplication.class, args);
    }

}
