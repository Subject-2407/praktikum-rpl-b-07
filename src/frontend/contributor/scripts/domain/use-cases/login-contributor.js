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

  return authRepository.login({ email, password });
}
