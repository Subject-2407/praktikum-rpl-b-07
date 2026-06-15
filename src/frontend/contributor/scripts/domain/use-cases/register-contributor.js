const DISPLAY_NAME_MAX_LENGTH = 100;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function registerContributor(authRepository, payload) {
  const displayName = String(payload?.displayName || '')
    .replace(/\s+/g, ' ')
    .trim();
  const email = String(payload?.email || '').trim();
  const password = String(payload?.password || '');
  const confirmPassword = String(payload?.confirmPassword || '');

  if (!displayName) {
    throw new Error('Display name is required.');
  }

  if (displayName.length > DISPLAY_NAME_MAX_LENGTH) {
    throw new Error('Display name must be 100 characters or less.');
  }

  if (!EMAIL_PATTERN.test(email)) {
    throw new Error('Enter a valid email address.');
  }

  if (password.length < 8) {
    throw new Error('Password must be at least 8 characters.');
  }

  if (password !== confirmPassword) {
    throw new Error('Password confirmation does not match.');
  }

  return authRepository.register({
    display_name: displayName,
    email,
    password,
    password_confirmation: confirmPassword,
  });
}
