package ro.petitii.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petitii.schedule")
public class SchedulerConfig {
    private String email;
    private String deadline;

    public void setEmail(String email) {
        this.email = email;
    }

    public String emailCronPattern() {
        return email;
    }

    public String deadlineCronPattern() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
