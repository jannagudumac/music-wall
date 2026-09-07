export interface UserProfile {
  username: string;
  bio: string | null;
  avatarUrl: string | null;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
