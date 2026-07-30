package com.example.tracker.service;

import com.example.tracker.dto.*;
import com.example.tracker.entity.*;
import com.example.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    @Autowired private ActivityRepository activityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ActivityInvitationRepository invitationRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public void autoCompleteExpiredActivities() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> expired = activityRepository.findAll().stream()
                .filter(a -> a.getCompletionDate() == null && a.getActivityDate() != null && a.getActivityDate().isBefore(now))
                .collect(Collectors.toList());

        for (Activity a : expired) {
            a.setCompletionDate(a.getActivityDate());
            activityRepository.save(a);
            System.out.println("⏳ Süresi dolan faaliyet otomatik kapatıldı: " + a.getTitle());
        }
    }

    public DashboardStatsResponse getDashboardStats() {
        autoCompleteExpiredActivities();

        long totalAct = activityRepository.count();
        long completedAct = activityRepository.countByCompletionDateIsNotNull();
        long ongoingAct = activityRepository.countByCompletionDateIsNull();
        long totalUsers = userRepository.count();

        Map<String, Long> catDist = activityRepository.findAll().stream()
                .filter(a -> a.getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getCategory().getName(), Collectors.counting()));

        return new DashboardStatsResponse(totalAct, completedAct, ongoingAct, totalUsers, catDist);
    }

    public Page<Activity> searchActivities(String keyword, Long categoryId, String status, Long userId, int page, int size) {
        autoCompleteExpiredActivities();

        Pageable pageable = PageRequest.of(page, size, Sort.by("activityDate").descending());
        return activityRepository.searchActivities(keyword, categoryId, status, userId, InvitationStatus.ACCEPTED, pageable);
    }

    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return categoryRepository.save(category);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Activity createActivity(ActivityCreateRequest request) {
        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setActivityDate(request.getActivityDate());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
            activity.setCategory(category);
        }

        if (request.getCreatorId() != null) {
            User creator = userRepository.findById(request.getCreatorId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
            activity.setCreator(creator);
        }

        return createActivity(activity, request.getInvitedUserIds());
    }

    @Transactional
    public Activity createActivity(Activity activity, List<Long> invitedUserIds) {
        Activity savedActivity = activityRepository.save(activity);

        if (invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    ActivityInvitation invitation = new ActivityInvitation();
                    invitation.setActivity(savedActivity);
                    invitation.setUser(user);

                    invitation.setStatus(InvitationStatus.PENDING);

                    invitationRepository.save(invitation);

                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setMessage("Yeni bir faaliyete davet edildiniz: '" + savedActivity.getTitle() + "'. Lütfen onay veya ret veriniz.");
                    notification.setCreatedAt(LocalDateTime.now());
                    notification.setRead(false);
                    notificationRepository.save(notification);

                    messagingTemplate.convertAndSend("/topic/user/" + user.getId(), notification);
                }
            }
        }
        return savedActivity;
    }

    @Transactional
    public ActivityInvitation respondToInvitation(Long invitationId, String statusStr) {
        InvitationStatus statusEnum;
        try {
            statusEnum = InvitationStatus.valueOf(statusStr);
        } catch (Exception e) {
            statusEnum = InvitationStatus.PENDING;
        }
        return respondToInvitation(invitationId, statusEnum);
    }

    @Transactional
    public ActivityInvitation respondToInvitation(Long invitationId, InvitationStatus status) {
        ActivityInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Davet bulunamadı"));

        invitation.setStatus(status);
        invitation.setRespondedAt(LocalDateTime.now());
        ActivityInvitation savedInv = invitationRepository.save(invitation);

        User creator = invitation.getActivity().getCreator();
        if (creator != null) {
            Notification notification = new Notification();
            notification.setUser(creator);
            notification.setMessage(invitation.getUser().getFullName() + ", '" + invitation.getActivity().getTitle() + "' davetinizi " + (status == InvitationStatus.ACCEPTED ? "onayladı ✔" : "reddedildi ✖") + ".");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notificationRepository.save(notification);

            messagingTemplate.convertAndSend("/topic/user/" + creator.getId(), notification);
        }

        return savedInv;
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}