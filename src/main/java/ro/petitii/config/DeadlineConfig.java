package ro.petitii.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petitii.deadline")
public class DeadlineConfig {
    private int days;
    private float yellowAlert;
    private float redAlert;

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public float getYellowAlert() {
        return yellowAlert;
    }

    public int yellowAlert() {
        return Math.round((1 - yellowAlert) * days);
    }

    public void setYellowAlert(float yellowAlert) {
        this.yellowAlert = yellowAlert;
    }

    public float getRedAlert() {
        return redAlert;
    }

    public int redAlert() {
        return Math.round((1 - redAlert) * days);
    }

    public void setRedAlert(float redAlert) {
        this.redAlert = redAlert;
    }
}
