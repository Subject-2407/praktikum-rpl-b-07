import { AuthApi } from '../api/auth-api.js';

export function createAuthRepository({ authApi = AuthApi } = {}) {
  return {
    login(credentials) {
      return authApi.login(credentials);
    },

    register(payload) {
      return authApi.register(payload);
    },

    requestPasswordReset(email) {
      return authApi.requestPasswordReset(email);
    },

    getCurrentSession(options) {
      return authApi.getCurrentSession(options);
    },

    logout() {
      return authApi.logout();
    },
  };
}

export const authRepository = createAuthRepository();
