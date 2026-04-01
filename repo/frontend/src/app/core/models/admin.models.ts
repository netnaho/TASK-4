export interface AdminUserModel {
  id: number;
  username: string;
  displayName: string;
  role: string;
  active: boolean;
}

export interface PermissionOverviewModel {
  role: string;
  permissions: string[];
}

export interface StateMachineTransitionModel {
  fromStatus: string;
  toStatus: string;
  active: boolean;
}

export interface StateMachineConfigModel {
  transitions: StateMachineTransitionModel[];
}

export interface ReasonCodeModel {
  id: number;
  codeType: string;
  code: string;
  label: string;
  active: boolean;
}
