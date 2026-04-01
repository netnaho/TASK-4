export interface UserSessionModel {
  id: number;
  username: string;
  displayName: string;
  organizationCode?: string;
  role: string;
  permissions: string[];
}

export interface LoginRequestModel {
  username: string;
  password: string;
  captchaChallengeId?: string | null;
  captchaAnswer?: string | null;
}

export interface LoginResponseModel {
  user: UserSessionModel;
  message: string;
}

export interface CaptchaChallengeModel {
  challengeId: string | null;
  question: string;
  required: boolean;
}

export interface ApiErrorModel {
  code: number;
  message: string;
  details: string[];
}
