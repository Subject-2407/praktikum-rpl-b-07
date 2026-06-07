import { request } from '../../core/http/api-client.js';

function resolveUser(data, fallback = {}) {
  return data?.user || data?.data?.user || fallback;
}

export const AuthApi = {
  async login(credentials) {
    const data = await request('/sessions', {
      method: 'POST',
      body: credentials,
    });
    const user = resolveUser(data, {
      email: credentials.email,
      role: 'contributor',
    });

    return {
      success: true,
      user,
      message: data?.message || 'Login berhasil.',
    };
  },

  async register(payload) {
    const data = await request('/registrations', {
      method: 'POST',
      body: payload,
    });

    return {
      success: true,
      message: data?.message || 'Registrasi berhasil. Silakan login.',
    };
  },

  async requestPasswordReset(email) {
    const data = await request('/password-resets', {
      method: 'POST',
      body: { email },
    });

    return {
      success: true,
      message: data?.message || 'Link reset password telah dikirim.',
    };
  },

  async getCurrentSession(options = {}) {
    const data = await request('/sessions/current', options);
    const user = resolveUser(data) || {};
    const expiresAt = data?.data?.expires_at || data?.expires_at || null;

    return {
      success: true,
      user,
      expiresAt,
      message: data?.message || 'Sesi aktif berhasil diambil.',
    };
  },

  async logout() {
    try {
      await request('/sessions/current', {
        method: 'DELETE',
        suppressUnauthorizedEvent: true,
      });
    } catch (error) {
      if (error?.status !== 401 && error?.status !== 403) {
        throw error;
      }
    }
  },
};
