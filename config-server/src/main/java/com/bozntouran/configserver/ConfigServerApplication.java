package com.bozntouran.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext ctx =
                SpringApplication.run(ConfigServerApplication.class, args);
        String repoLocation = ctx.getEnvironment().getProperty("spring.cloud.config.server.native.searchLocations");
        String code = ctx.getEnvironment().getProperty("PWD");
        System.out.println("Serving configurations from folder: " + repoLocation );
    }

}
