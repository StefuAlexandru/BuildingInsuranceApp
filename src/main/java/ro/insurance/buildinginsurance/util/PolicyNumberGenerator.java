package ro.insurance.buildinginsurance.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class PolicyNumberGenerator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;

    public String generatePolicyNumber() {
        String date = LocalDateTime.now().format(formatter);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0,12).toUpperCase();
        return "POL-" + date + "-" + suffix;
    }
}
