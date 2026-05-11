package com.amnii.parking.entryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EntryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntryServiceApplication.class, args);
    }
}
