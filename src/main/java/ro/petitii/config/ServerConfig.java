package ro.petitii.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    @Autowired
    private Environment environment;

    public String serverPort() {
        return environment.getProperty("local.server.port");
    }

    public String serverHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Could not determine de server hostname. Returning null ...");
            return null;
        }
    }

    public String serverProtocol() {
        //todo; do something here when https is configured
        return "http";
    }

    public String serverUrl() {
        return serverProtocol() + "://" + serverHostname() + ":" + serverPort();
    }
}
