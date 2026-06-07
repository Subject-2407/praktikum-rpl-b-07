const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function registerContributor(authRepository, payload) {
  const email = String(payload?.email || '').trim();
  const password = String(payload?.password || '');
  const confirmPassword = String(payload?.confirmPassword || '');

  if (!EMAIL_PATTERN.test(email)) {
    throw new Error('Masukkan email yang valid.');
  }

  if (password.length < 8) {
    throw new Error('Password minimal 8 karakter.');
  }

  if (password !== confirmPassword) {
    throw new Error('Konfirmasi password tidak sama.');
  }

  return authRepository.register({
    email,
    password,
    password_confirmation: confirmPassword,
  });
}
