package ro.petitii.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@ConfigurationProperties(prefix = "petitii.baseUrl")
public class ServerConfig {
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String serverUrl() {
        return url;
    }
}
