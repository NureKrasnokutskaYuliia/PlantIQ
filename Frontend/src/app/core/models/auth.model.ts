export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: number; // 0 = Owner
}

export interface UserResponse {
  userId: number;
  name: string;
  email: string;
  role: number;
  createdAt: string;
  isActive: boolean;
  lastLogin: string | null;
}

export interface LoginResponse {
  token: string;
  user: UserResponse;
}
