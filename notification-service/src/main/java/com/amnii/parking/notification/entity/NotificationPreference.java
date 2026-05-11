package com.amnii.parking.notification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    private String email; // Primary key is the user's email

    @Builder.Default
    private boolean emailEnabled = true;

    @Builder.Default
    private boolean inAppEnabled = true;
}
