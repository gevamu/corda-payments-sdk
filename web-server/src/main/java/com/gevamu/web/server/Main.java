package com.gevamu.web.server;

import com.gevamu.web.server.config.CordaRpcClientConnection;
import com.gevamu.web.server.config.Participants;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    CordaRpcClientConnection.class,
    Participants.class
})
public class Main {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
            .main(Main.class)
            .sources(Main.class)
            .bannerMode(Banner.Mode.OFF)
            .run(args);
    }
}
