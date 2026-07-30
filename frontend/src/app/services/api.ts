import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, data);
  }

  verify(email: string, code: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/verify`, { email, verificationCode: code });
  }

  login(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, data);
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/categories`);
  }

  createCategory(name: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/tracker/categories`, { name });
  }

  getActivities(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/activities`);
  }

  createActivity(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/tracker/activities`, data);
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/users`);
  }

  getNotifications(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/notifications/user/${userId}`);
  }

  respondToInvitation(invitationId: number, status: 'ACCEPTED' | 'REJECTED'): Observable<any> {
    return this.http.post(`${this.baseUrl}/tracker/invitations/${invitationId}/respond`, { status });
  }

  saveCurrentUser(user: any): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
  }

  getCurrentUser(): any {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  logout(): void {
    localStorage.removeItem('currentUser');
  }
getInvitations(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/invitations/user/${userId}`);
  }
clearNotifications(userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/tracker/notifications/user/${userId}`);
  }
updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/tracker/users/${userId}/role`, { role });
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/tracker/users/${userId}`);
  }
  getStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/tracker/stats`);
  }

  searchActivities(keyword: string = '', categoryId: any = '', status: string = 'ONGOING', userId: any = null, page: number = 0, size: number = 5): Observable<any> {
      let url = `${this.baseUrl}/tracker/activities/search?page=${page}&size=${size}`;
      if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
      if (categoryId) url += `&categoryId=${categoryId}`;
      if (status) url += `&status=${status}`;
      if (userId) url += `&userId=${userId}`;
      return this.http.get(url);
    }

  uploadFile(activityId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.baseUrl}/tracker/activities/${activityId}/files`, formData);
  }

  getActivityFiles(activityId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tracker/activities/${activityId}/files`);
  }

  getDownloadUrl(fileId: number): string {
    return `${this.baseUrl}/tracker/files/${fileId}/download`;
  }
}
