import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { UpdateProfile, UserProfile } from '../../models/profile.model';
import { AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  profile: UserProfile | null = null;
  editModel: UpdateProfile | null = null;
  errorMessage = '';
  editError = '';
  loading = true;
  editing = false;
  saving = false;
  uploadingAvatar = false;
  avatarVersion = 0;

  constructor(
    private route: ActivatedRoute,
    private profileService: ProfileService,
    public authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.profile = null;
      this.load(params.get('username') || this.authService.getUsername() || '');
    });
  }

  get isOwnProfile(): boolean {
    return !!this.profile && this.profile.username === this.authService.getUsername();
  }

  get avatarSrc(): string | null {
    if (!this.profile?.avatarUrl) return null;
    const server = environment.apiUrl.replace(/\/api$/, '');
    return `${server}${this.profile.avatarUrl}?v=${this.avatarVersion}`;
  }

  openEditor(): void {
    if (!this.profile || !this.isOwnProfile) return;
    this.editModel = { bio: this.profile.bio || '' };
    this.editError = '';
    this.editing = true;
  }

  closeEditor(): void {
    if (this.saving) return;
    this.editing = false;
    this.editModel = null;
  }

  saveProfile(): void {
    if (!this.editModel) return;
    this.saving = true;
    this.editError = '';
    this.profileService.updateProfile(this.editModel).subscribe({
      next: profile => {
        this.profile = profile;
        this.saving = false;
        this.closeEditor();
      },
      error: error => {
        this.editError = error.error?.message || 'Could not update profile';
        this.saving = false;
      }
    });
  }

  chooseAvatar(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || !this.isOwnProfile) return;

    this.uploadingAvatar = true;
    this.profileService.uploadAvatar(file).subscribe({
      next: profile => {
        this.profile = profile;
        this.avatarVersion = Date.now();
        this.uploadingAvatar = false;
      },
      error: error => {
        this.errorMessage = error.error?.message || 'Could not upload avatar';
        this.uploadingAvatar = false;
      }
    });
  }

  private load(username: string): void {
    this.loading = true;
    this.errorMessage = '';
    this.profileService.getProfile(username).subscribe({
      next: profile => {
        this.profile = profile;
        this.loading = false;
      },
      error: error => {
        this.errorMessage = error.error?.message || 'Could not load profile';
        this.loading = false;
      }
    });
  }
}
