package com.example.tracker.dto;

import java.util.Map;

public class DashboardStatsResponse {
    private long totalActivities;
    private long completedActivities;
    private long ongoingActivities;
    private long totalUsers;
    private Map<String, Long> categoryDistribution;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalActivities, long completedActivities, long ongoingActivities, long totalUsers, Map<String, Long> categoryDistribution) {
        this.totalActivities = totalActivities;
        this.completedActivities = completedActivities;
        this.ongoingActivities = ongoingActivities;
        this.totalUsers = totalUsers;
        this.categoryDistribution = categoryDistribution;
    }

    public long getTotalActivities() { return totalActivities; }
    public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }

    public long getCompletedActivities() { return completedActivities; }
    public void setCompletedActivities(long completedActivities) { this.completedActivities = completedActivities; }

    public long getOngoingActivities() { return ongoingActivities; }
    public void setOngoingActivities(long ongoingActivities) { this.ongoingActivities = ongoingActivities; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
}