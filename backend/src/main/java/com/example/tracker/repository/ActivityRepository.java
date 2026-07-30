package com.example.tracker.repository;

import com.example.tracker.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    long countByCompletionDateIsNotNull();
    long countByCompletionDateIsNull();

    @Query("SELECT a FROM Activity a WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR a.category.id = :categoryId) " +
            "AND (:status IS NULL OR :status = '' OR (:status = 'COMPLETED' AND a.completionDate IS NOT NULL) OR (:status = 'ONGOING' AND a.completionDate IS NULL)) " +
            "AND (:userId IS NULL OR a.creator.id = :userId OR a.id IN (SELECT i.activity.id FROM ActivityInvitation i WHERE i.user.id = :userId AND i.status = :accStatus))")
    Page<Activity> searchActivities(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("userId") Long userId,
            @Param("accStatus") com.example.tracker.entity.InvitationStatus accStatus,
            Pageable pageable);
}