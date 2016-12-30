package ro.petitii.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petitii.smtp")
public class SmtpConfig {
    private String server;
    private Number port;
    private Boolean ssl;
    private String username;
    private String password;
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpConfig.class);

    public SmtpConfig() {
        LOGGER.debug("Smtp config initialised");
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Number getPort() {
        return port;
    }

    public void setPort(Number port) {
        this.port = port;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
