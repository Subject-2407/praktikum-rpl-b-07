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

<<<<<<< HEAD
=======
    resetPassword(token, payload) {
      return authApi.resetPassword(token, payload);
    },

>>>>>>> 4869852afc37065278ea6adbe6b6a445b0f35e05
    getCurrentSession(options) {
      return authApi.getCurrentSession(options);
    },

    logout() {
      return authApi.logout();
    },
  };
}

export const authRepository = createAuthRepository();
