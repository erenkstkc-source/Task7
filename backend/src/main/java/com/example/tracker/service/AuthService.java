package com.example.tracker.service;

import com.example.tracker.dto.LoginRequest;
import com.example.tracker.dto.RegisterRequest;
import com.example.tracker.dto.VerifyRequest;
import com.example.tracker.entity.Role;
import com.example.tracker.entity.User;
import com.example.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi sistemde zaten kayıtlı!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (userRepository.count() == 0) {
            user.setRole(Role.ROLE_ADMIN);
            System.out.println("Sistemdeki ilk kullanıcı kaydoldu, ADMİN yetkisi verildi!");
        } else {
            user.setRole(Role.ROLE_USER);
        }
        user.setVerified(false);

        String verificationCode = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return "Kayıt başarılı! Lütfen e-posta adresinize gönderilen 6 haneli doğrulama kodunu giriniz.";
    }

    public String verifyAccount(VerifyRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Kullanıcı bulunamadı!");
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            return "Hesap zaten doğrulanmış!";
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş! Lütfen yeni kod talep edin.");
        }

        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new RuntimeException("Hatalı doğrulama kodu!");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        return "Hesabınız başarıyla doğrulandı! Artık giriş yapabilirsiniz.";
    }

    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("E-posta veya şifre hatalı!");
        }

        User user = userOpt.get();

        if (!user.isVerified()) {
            throw new RuntimeException("Lütfen giriş yapmadan önce e-posta adresinize gelen kod ile hesabınızı doğrulayın!");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("E-posta veya şifre hatalı!");
        }

        return user;
    }
}
