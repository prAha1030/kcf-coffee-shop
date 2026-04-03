package com.kcfcoffeeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class KcfCoffeeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(KcfCoffeeShopApplication.class, args);
    }

}
