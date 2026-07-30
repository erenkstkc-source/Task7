import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent {
  currentTab: 'login' | 'register' | 'verify' = 'login';

  fullName = '';
  email = '';
  password = '';
  verificationCode = '';

  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(private apiService: ApiService, private router: Router) {}

  onRegister(): void {
    this.resetMessages();
    if (!this.fullName || !this.email || !this.password) {
      this.errorMessage = 'Lütfen tüm alanları doldurunuz.';
      return;
    }

    this.isLoading = true;
    this.apiService.register({ fullName: this.fullName, email: this.email, password: this.password })
      .subscribe({
        next: (res: any) => {
          this.isLoading = false;
          this.successMessage = 'Kayıt başarılı! E-posta adresinize (veya konsola) gelen 6 haneli kodu giriniz.';
          this.currentTab = 'verify';
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error?.error || 'Kayıt olurken bir hata oluştu!';
        }
      });
  }

  onVerify(): void {
    this.resetMessages();
    if (!this.verificationCode) {
      this.errorMessage = 'Lütfen 6 haneli doğrulama kodunu giriniz.';
      return;
    }

    this.isLoading = true;
    this.apiService.verify(this.email, this.verificationCode)
      .subscribe({
        next: (res: any) => {
          this.isLoading = false;
          this.successMessage = 'Hesabınız doğrulandı! Şimdi giriş yapabilirsiniz.';
          this.currentTab = 'login';
          this.password = '';
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error?.error || 'Doğrulama kodu hatalı veya süresi dolmuş!';
        }
      });
  }

  onLogin(): void {
    this.resetMessages();
    if (!this.email || !this.password) {
      this.errorMessage = 'Lütfen e-posta ve şifrenizi giriniz.';
      return;
    }

    this.isLoading = true;
    this.apiService.login({ email: this.email, password: this.password })
      .subscribe({
        next: (user: any) => {
          this.isLoading = false;
          this.apiService.saveCurrentUser(user);
          this.router.navigate(['/dashboard']);
        },
        error: (err: any) => {
          this.isLoading = false;
          this.errorMessage = err.error?.error || 'Giriş yapılamadı! Bilgilerinizi kontrol edin.';
        }
      });
  }

  switchTab(tab: 'login' | 'register'): void {
    this.currentTab = tab;
    this.resetMessages();
  }

  private resetMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
