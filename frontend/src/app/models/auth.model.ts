// TypeScript interfaces describe the JSON exchanged with the backend; they do not validate it at
// runtime.
export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}
