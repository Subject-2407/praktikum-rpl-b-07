export async function verifyEmail(authRepository, token) {
  const normalizedToken = String(token || '').trim();

  if (!normalizedToken) {
    throw new Error('Verification token is missing.');
  }

  return authRepository.verifyEmail(normalizedToken);
}
