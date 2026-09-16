// Profile JSON includes an avatar URL; the image itself is loaded from a separate endpoint.
export interface UserProfile {
  username: string;
  bio: string | null;
  avatarUrl: string | null;
}

// Send only the current and new passwords; confirmation is checked in the component.
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
