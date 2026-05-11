package com.amnii.parking.entryservice.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ParkingServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.parking-service-url:http://localhost:8082}")
    private String parkingServiceUrl;

    public ParkingInfo getParkingInfo(String code) {
        try {
            ResponseEntity<ParkingApiResponse> resp = restTemplate.getForEntity(
                    parkingServiceUrl + "/api/parkings/" + code, ParkingApiResponse.class);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                return resp.getBody().getData();
            }
        } catch (Exception e) {
            // will be handled as null
        }
        return null;
    }

    public void decrementSpace(String code) {
        try {
            restTemplate.patchForObject(
                    parkingServiceUrl + "/api/parkings/" + code + "/decrement", null, Void.class);
        } catch (Exception ignored) {}
    }

    public void incrementSpace(String code) {
        try {
            restTemplate.patchForObject(
                    parkingServiceUrl + "/api/parkings/" + code + "/increment", null, Void.class);
        } catch (Exception ignored) {}
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ParkingInfo {
        private String code;
        private String name;
        private String location;
        private int availableSpaces;
        private BigDecimal feePerHour;
    }

    @Getter @Setter @NoArgsConstructor
    public static class ParkingApiResponse {
        private boolean success;
        private ParkingInfo data;
    }
}
