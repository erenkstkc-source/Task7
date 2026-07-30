package com.example.tracker.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityCreateRequest {
    private String title;
    private String description;
    private LocalDateTime activityDate;
    private LocalDateTime completionDate;
    private Long categoryId;
    private Long creatorId;
    private List<Long> invitedUserIds;

    public ActivityCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDateTime activityDate) { this.activityDate = activityDate; }

    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public List<Long> getInvitedUserIds() { return invitedUserIds; }
    public void setInvitedUserIds(List<Long> invitedUserIds) { this.invitedUserIds = invitedUserIds; }
}
