import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

(window as any).global = window;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  currentUser: any = null;
  isAdmin: boolean = false;
  activeTab: 'activities' | 'create' | 'notifications' | 'users' = 'activities';

  activities: any[] = [];
  categories: any[] = [];
  users: any[] = [];
  notifications: any[] = [];
  invitations: any[] = [];

  stats: any = { totalActivities: 0, completedActivities: 0, ongoingActivities: 0, totalUsers: 0 };

  searchKeyword = '';
  selectedFilterCategory: any = '';
  currentPage = 0;
  pageSize = 5;
  totalPages = 0;

  newCategoryName = '';
  newActivity = {
    title: '',
    description: '',
    activityDate: '',
    completionDate: '',
    categoryId: null,
    creatorId: null,
    invitedUserIds: [] as number[]
  };

  selectedActivityForFiles: any = null;
  activityFiles: any[] = [];
  selectedFileToUpload: File | null = null;
  isUploading = false;

  private stompClient: Client | null = null;

  message = '';
  error = '';
  selectedFilterStatus = 'ONGOING';

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.currentUser = this.apiService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.isAdmin = this.currentUser.role === 'ROLE_ADMIN';
    this.newActivity.creatorId = this.currentUser.id;

    this.loadData();
    this.connectWebSocket();
  }

  loadData(): void {
    this.apiService.getStats().subscribe({ next: (res: any) => this.stats = res });
    this.loadActivities(0);

    this.apiService.getCategories().subscribe({ next: (res: any) => this.categories = res });
    this.apiService.getNotifications(this.currentUser.id).subscribe({ next: (res: any) => this.notifications = res });
    this.apiService.getInvitations(this.currentUser.id).subscribe({ next: (res: any) => this.invitations = res });

    if (this.isAdmin) {
      this.apiService.getUsers().subscribe({ next: (res: any) => this.users = res });
    }
  }

  loadActivities(page: number = 0): void {
    this.currentPage = page;
    const filterUserId = this.isAdmin ? null : this.currentUser.id;

    this.apiService.searchActivities(
      this.searchKeyword,
      this.selectedFilterCategory,
      this.selectedFilterStatus,
      filterUserId,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (res: any) => {
        this.activities = res.content || [];
        this.totalPages = res.totalPages || 0;
      },
      error: () => this.error = 'Faaliyetler yüklenirken hata oluştu.'
    });
  }

  createCategory(): void {
    this.resetAlerts();
    if (!this.newCategoryName) return;

    this.apiService.createCategory(this.newCategoryName).subscribe({
      next: () => {
        this.message = 'Kategori başarıyla eklendi!';
        this.newCategoryName = '';
        this.loadData();
      },
      error: (err: any) => this.error = err.error?.error || 'Kategori eklenemedi!'
    });
  }

  createActivity(): void {
    this.resetAlerts();
    if (!this.newActivity.title || !this.newActivity.categoryId || !this.newActivity.activityDate) {
      this.error = 'Lütfen başlık, kategori ve tarih alanlarını doldurunuz.';
      return;
    }

    this.apiService.createActivity(this.newActivity).subscribe({
      next: () => {
        this.message = 'Faaliyet başarıyla oluşturuldu ve seçilen üyelere bildirim gönderildi!';
        this.newActivity.title = '';
        this.newActivity.description = '';
        this.newActivity.invitedUserIds = [];
        this.activeTab = 'activities';
        this.loadData();
      },
      error: (err: any) => this.error = err.error?.error || 'Faaliyet oluşturulamadı!'
    });
  }

  toggleUserSelection(userId: number, event: any): void {
    if (event.target.checked) {
      this.newActivity.invitedUserIds.push(userId);
    } else {
      this.newActivity.invitedUserIds = this.newActivity.invitedUserIds.filter(id => id !== userId);
    }
  }

  // --- DÜZELTİLMİŞ VE TEKİLLEŞTİRİLMİŞ YANITLAMA METODU ---
  respond(invitationId: number, status: 'ACCEPTED' | 'REJECTED'): void {
    this.apiService.respondToInvitation(invitationId, status).subscribe({
      next: (res: any) => {
        // Anında listeden silme işlemi
        this.invitations = this.invitations.filter((inv: any) => inv.id !== invitationId);

        // Kabul edildiyse tabloyu yenile
        if (status === 'ACCEPTED') {
          this.loadActivities(this.currentPage);
        }

        this.message = status === 'ACCEPTED' ? '✔ Daveti başarıyla kabul ettiniz.' : '✖ Daveti reddettiniz.';
        setTimeout(() => this.message = '', 3000);
      },
      error: (err: any) => {
        this.error = 'Davet yanıtlanırken bir hata oluştu.';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  clearNotifications(): void {
    if (!confirm('Tüm bildirim geçmişini temizlemek istediğinize emin misiniz?')) return;

    this.apiService.clearNotifications(this.currentUser.id).subscribe({
      next: () => {
        this.notifications = [];
        this.message = 'Bildirim geçmişi başarıyla temizlendi.';
      },
      error: () => this.error = 'Bildirimler temizlenirken bir hata oluştu.'
    });
  }

  changeUserRole(user: any): void {
    const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
    if (!confirm(`${user.fullName} isimli personelin yetkisini "${newRole === 'ROLE_ADMIN' ? 'Yönetici' : 'Personel'}" olarak değiştirmek istiyor musunuz?`)) return;

    this.apiService.updateUserRole(user.id, newRole).subscribe({
      next: () => {
        user.role = newRole;
        this.message = 'Kullanıcı yetkisi başarıyla güncellendi.';
      },
      error: () => this.error = 'Yetki değiştirilirken bir hata oluştu.'
    });
  }

  deleteUser(user: any): void {
    if (user.id === this.currentUser.id) {
      alert('Kendi admin hesabınızı silemezsiniz!');
      return;
    }
    if (!confirm(`${user.fullName} kullanıcısını sistemden tamamen silmek istediğinize emin misiniz?`)) return;

    this.apiService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
        this.message = 'Kullanıcı sistemden başarıyla silindi.';
      },
      error: () => this.error = 'Kullanıcı silinirken hata oluştu.'
    });
  }

  openFileManager(activity: any): void {
    this.selectedActivityForFiles = activity;
    this.selectedFileToUpload = null;
    this.loadActivityFiles(activity.id);
  }

  closeFileManager(): void {
    this.selectedActivityForFiles = null;
    this.activityFiles = [];
  }

  loadActivityFiles(activityId: number): void {
    this.apiService.getActivityFiles(activityId).subscribe({
      next: (res: any) => this.activityFiles = res,
      error: () => console.error('Dosyalar çekilemedi')
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFileToUpload = file;
    }
  }

  uploadSelectedFile(): void {
    if (!this.selectedFileToUpload || !this.selectedActivityForFiles) return;

    this.isUploading = true;
    this.apiService.uploadFile(this.selectedActivityForFiles.id, this.selectedFileToUpload).subscribe({
      next: (res: any) => {
        this.message = `'${res.originalName}' dosyası başarıyla yüklendi!`;
        this.selectedFileToUpload = null;
        this.isUploading = false;
        this.loadActivityFiles(this.selectedActivityForFiles.id);
      },
      error: () => {
        this.error = 'Dosya yüklenirken hata oluştu! (Boyut 10MB altı olmalıdır)';
        this.isUploading = false;
      }
    });
  }

  getDownloadLink(fileId: number): string {
    return this.apiService.getDownloadUrl(fileId);
  }

  connectWebSocket(): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('⚡ WebSocket Canlı Bağlantısı Kuruldu!');
        this.stompClient?.subscribe(`/topic/user/${this.currentUser.id}`, (message) => {
          const newNotif = JSON.parse(message.body);
          this.notifications.unshift(newNotif);
          alert(`🔔 CANLI BİLDİRİM: ${newNotif.message}`);
          this.loadData();
        });
      },
      onStompError: (frame) => console.error('WebSocket Hatası:', frame)
     });

    this.stompClient.activate();
  }

  public resetAlerts(): void {
    this.message = '';
    this.error = '';
  }

  logout(): void {
    this.apiService.logout();
    this.router.navigate(['/login']);
  }
}
