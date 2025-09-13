package com.bozntouran.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.Map;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        //SpringApplication.run(EurekaServerApplication.class, args);

        ConfigurableApplicationContext context = SpringApplication.run(EurekaServerApplication.class, args);

        ConfigurableEnvironment env = context.getEnvironment();

        System.out.println("==== ALL SPRING BOOT PROPERTIES ====");
        for (PropertySource<?> propertySource : env.getPropertySources()) {
            if (propertySource.getSource() instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) propertySource.getSource();
                map.forEach((key, value) -> {
                    System.out.println(key + " = " + value);
                });
            }
        }
        System.out.println("====================================");
    }

}

