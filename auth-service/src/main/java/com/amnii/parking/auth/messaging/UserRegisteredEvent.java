package com.amnii.parking.auth.messaging;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRegisteredEvent {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String eventType = "USER_REGISTERED";
}
