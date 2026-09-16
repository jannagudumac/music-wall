import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChangePasswordRequest, UserProfile } from '../models/profile.model';

// Sends profile edits, password changes and avatar uploads to the backend with HttpClient.
@Injectable({ providedIn: 'root' })
export class ProfileService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(username: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.api}/profiles/${username}`);
  }

  // Send only bio; /me uses the account identified by the JWT.
  updateProfile(request: Pick<UserProfile, 'bio'>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.api}/profiles/me`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/profiles/me/password`, request);
  }

  uploadAvatar(file: File): Observable<UserProfile> {
    // FormData sends the file as multipart data; let the browser set the upload Content-Type.
    const data = new FormData();
    data.append('file', file);
    return this.http.post<UserProfile>(`${this.api}/profiles/me/avatar`, data);
  }

}
