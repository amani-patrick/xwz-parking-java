package com.amnii.parking.notification.controller;

import com.amnii.parking.notification.entity.NotificationPreference;
import com.amnii.parking.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceRepository preferenceRepository;

    @GetMapping
    public NotificationPreference getPreferences(@RequestHeader("X-User-Email") String email) {
        return preferenceRepository.findById(email)
                .orElse(NotificationPreference.builder().email(email).build());
    }

    @PutMapping
    public NotificationPreference updatePreferences(
            @RequestHeader("X-User-Email") String email,
            @RequestBody NotificationPreference prefs) {
        prefs.setEmail(email);
        return preferenceRepository.save(prefs);
    }
}
