package com.example.tracker.dto;

import com.example.tracker.entity.InvitationStatus;

public class InvitationRespondRequest {
    private InvitationStatus status;

    public InvitationRespondRequest() {}

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
}
