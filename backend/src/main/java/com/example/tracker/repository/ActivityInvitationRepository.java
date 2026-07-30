package com.example.tracker.repository;
import com.example.tracker.entity.ActivityInvitation;
import com.example.tracker.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface ActivityInvitationRepository extends JpaRepository<ActivityInvitation, Long> {
    List<ActivityInvitation> findByUserId(Long userId);

    List<ActivityInvitation> findByUserIdAndStatus(Long userId, InvitationStatus status);

    List<ActivityInvitation> findByActivityId(Long activityId);

    @Transactional
    void deleteByUserId(Long userId);
}
