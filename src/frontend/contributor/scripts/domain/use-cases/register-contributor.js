import { AuthRepository } from '../../data/repositories/auth-repository.js';

/**
 * Validates and submits a new contributor registration.
 */

export async function registerContributor(payload) {
  const { name, email, password, confirmPassword } = payload;

  if (!name || name.trim().length < 2) {
    return { success: false, message: 'Display name must be at least 2 characters.' };
  }

  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return { success: false, message: 'Please enter a valid email address.' };
  }

  if (!password || password.length < 8) {
    return { success: false, message: 'Password must be at least 8 characters.' };
  }

  if (password !== confirmPassword) {
    return { success: false, message: 'Passwords do not match.' };
  }

  // send registration request to repository
  return await AuthRepository.register({
    email,
    password,
    password_confirmation: confirmPassword,
  });
}