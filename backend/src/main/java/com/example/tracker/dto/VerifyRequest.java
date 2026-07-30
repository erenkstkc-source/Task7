package com.example.tracker.dto;

public class VerifyRequest {
    private String email;
    private String verificationCode;

    public VerifyRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
}
