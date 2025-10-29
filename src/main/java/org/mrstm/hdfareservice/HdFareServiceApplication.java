package org.mrstm.hdfareservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("org.mrstm.uberentityservice.models")
@EnableDiscoveryClient
public class HdFareServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HdFareServiceApplication.class, args);
    }

}
