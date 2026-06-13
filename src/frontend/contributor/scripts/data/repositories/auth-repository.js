import { AuthApi } from '../api/auth-api.js';

export function createAuthRepository({ authApi = AuthApi } = {}) {
  return {
    verifyEmail(token) {
      return authApi.verifyEmail(token);
    },

    login(credentials) {
      return authApi.login(credentials);
    },

    register(payload) {
      return authApi.register(payload);
    },

    requestPasswordReset(email) {
      return authApi.requestPasswordReset(email);
    },

    resetPassword(token, payload) {
      return authApi.resetPassword(token, payload);
    },

    resetPassword(token, payload) {
      return authApi.resetPassword(token, payload);
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
