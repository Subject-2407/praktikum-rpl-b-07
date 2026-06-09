export async function resetPassword(authRepository, token, payload) {
  const normalizedToken = String(token || '').trim();
  const password = String(payload?.password || '');
  const confirmPassword = String(payload?.confirmPassword || '');

  if (!normalizedToken) {
    throw new Error('Password reset token is missing.');
  }

  if (password.length < 8) {
    throw new Error('Password must be at least 8 characters.');
  }

  if (password !== confirmPassword) {
    throw new Error('Password confirmation does not match.');
  }

  return authRepository.resetPassword(normalizedToken, {
    password,
    password_confirmation: confirmPassword,
  });
}
