package org.example.rwandasupplychain.Controllers;

import org.example.rwandasupplychain.DTOs.NotificationDtos.NotificationResponse;
import org.example.rwandasupplychain.Services.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getForUser(@RequestParam UUID userId,
                                                  @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return notificationService.getForUser(userId, unreadOnly);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        return notificationService.markRead(id);
    }
}
