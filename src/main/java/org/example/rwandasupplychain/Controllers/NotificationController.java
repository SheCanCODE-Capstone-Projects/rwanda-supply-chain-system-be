package org.example.rwandasupplychain.Controllers;

import org.example.rwandasupplychain.DTOs.NotificationDtos.NotificationResponse;
import org.example.rwandasupplychain.Services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> inbox(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return notificationService.getInbox(unreadOnly);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", notificationService.countUnread());
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        return notificationService.markAsRead(id);
    }

    @PostMapping("/run-now")
    public ResponseEntity<Void> runNow() {
        notificationService.runAlertChecksNow();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}