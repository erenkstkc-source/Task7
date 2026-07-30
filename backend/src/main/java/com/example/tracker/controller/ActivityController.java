package com.example.tracker.controller;

import com.example.tracker.dto.*;
import com.example.tracker.entity.*;
import com.example.tracker.repository.*;
import com.example.tracker.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired private ActivityService activityService;
    @Autowired private ActivityInvitationRepository invitationRepository;
    @Autowired private NotificationRepository notificationRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(activityService.getDashboardStats());
    }

    @GetMapping("/activities/search")
    public ResponseEntity<Page<Activity>> searchActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId, // <-- YENİ EKLENDİ
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Activity> result = activityService.searchActivities(keyword, categoryId, status, userId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @PostMapping("/activities")
    public ResponseEntity<?> createActivity(@RequestBody ActivityCreateRequest request) {
        try {
            Activity created = activityService.createActivity(request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(activityService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            return ResponseEntity.ok(activityService.createCategory(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invitations/user/{userId}")
    public ResponseEntity<?> getUserInvitations(@PathVariable Long userId) {
        return ResponseEntity.ok(invitationRepository.findByUserId(userId));
    }

    @RequestMapping(value = "/invitations/{id}/respond", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> respondToInvitation(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String finalStatus = status != null ? status : (body != null ? body.get("status") : "ACCEPTED");
            ActivityInvitation updatedInvitation = activityService.respondToInvitation(id, finalStatus);
            return ResponseEntity.ok(updatedInvitation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findAll().stream()
                .filter(n -> n.getUser() != null && n.getUser().getId().equals(userId))
                .sorted((n1, n2) -> n2.getId().compareTo(n1.getId()))
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/notifications/user/{userId}")
    @Transactional
    public ResponseEntity<?> clearUserNotifications(@PathVariable Long userId) {
        notificationRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }
}