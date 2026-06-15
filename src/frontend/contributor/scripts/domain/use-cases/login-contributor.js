import { isContributorUser } from '../../core/auth/contributor-role.js';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function loginContributor(authRepository, credentials) {
  const email = String(credentials?.email || '').trim();
  const password = String(credentials?.password || '');

  if (!EMAIL_PATTERN.test(email)) {
    throw new Error('Masukkan email yang valid.');
  }

  if (!password) {
    throw new Error('Password wajib diisi.');
  }

  const loginResult = await authRepository.login({ email, password });

  if (isContributorUser(loginResult.user)) {
    return loginResult;
  }

  const session = await authRepository.getCurrentSession({
    suppressUnauthorizedEvent: true,
  });

  if (isContributorUser(session.user)) {
    return {
      ...loginResult,
      user: session.user,
    };
  }

  throw new Error('Akun ini tidak memiliki akses ke portal contributor.');
}
