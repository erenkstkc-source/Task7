package com.example.tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ankaref.com}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Ankaref Faaliyet Takip - Hesap Doğrulama Kodu");
            message.setText("Merhaba,\n\n"
                    + "Ankaref Faaliyet Takip sistemine hoş geldiniz. "
                    + "Hesabınızı doğrulamak ve giriş yapabilmek için 6 haneli güvenlik kodunuz:\n\n"
                    + "👉 " + verificationCode + " 👈\n\n"
                    + "Bu kod 15 dakika boyunca geçerlidir.\n\nİyi çalışmalar dileriz.");

            if (mailSender != null) {
                mailSender.send(message);
                System.out.println("✅ E-posta başarıyla gönderildi: " + toEmail);
            }
        } catch (Exception e) {
            System.err.println("⚠️ E-posta gönderilemedi! Hata detayı: " + e.getMessage());
            System.out.println("📌 TEST İÇİN DOĞRULAMA KODU: " + verificationCode);
        }
    }
}