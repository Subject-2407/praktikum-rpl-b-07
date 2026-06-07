const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function requestPasswordReset(authRepository, email) {
  const normalizedEmail = String(email || '').trim();

  if (!EMAIL_PATTERN.test(normalizedEmail)) {
    throw new Error('Masukkan email yang valid.');
  }

  return authRepository.requestPasswordReset(normalizedEmail);
}
