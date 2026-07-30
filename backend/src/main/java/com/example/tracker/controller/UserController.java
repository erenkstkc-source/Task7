package com.example.tracker.controller;

import com.example.tracker.entity.Role;
import com.example.tracker.entity.User;
import com.example.tracker.repository.ActivityInvitationRepository;
import com.example.tracker.repository.NotificationRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracker/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ActivityInvitationRepository invitationRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String newRoleStr = request.get("role");
        user.setRole(Role.valueOf(newRoleStr));
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        notificationRepository.deleteByUserId(id);
        invitationRepository.deleteByUserId(id);

        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}