package ro.petitii.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petitii.schedule")
public class SchedulerConfig {
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public String emailCronPattern() {
        return email;
    }
}
