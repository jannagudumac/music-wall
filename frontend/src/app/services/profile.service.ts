import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChangePasswordRequest, UpdateProfile, UserProfile } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(username: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.api}/profiles/${username}`);
  }

  updateProfile(request: UpdateProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.api}/profiles/me`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/profiles/me/password`, request);
  }

  uploadAvatar(file: File): Observable<UserProfile> {
    const data = new FormData();
    data.append('file', file);
    return this.http.post<UserProfile>(`${this.api}/profiles/me/avatar`, data);
  }

}
