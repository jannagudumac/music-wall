export interface UserProfile {
  username: string;
  bio: string | null;
  avatarUrl: string | null;
}

export interface UpdateProfile {
  bio: string;
}
