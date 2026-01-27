package ro.insurance.buildinginsurance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityUsersConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}pass")
                .roles("ADMIN")
                .build();

        UserDetails broker = User.withUsername("broker")
                .password("{noop}pass")
                .roles("BROKER")
                .build();

        return new InMemoryUserDetailsManager(admin, broker);
    }
}

