import { AuthRepository } from '../../data/repositories/auth-repository.js';
import { setCookie } from '../../core/http/api-client.js';

// max failed attempts before brute-force block. 
const MAX_ATTEMPTS = 5;

// block duration in milliseconds (30 seconds). 
const BLOCK_DURATION_MS = 30 * 1000;

const STORAGE_KEY_ATTEMPTS = 'scapes_login_attempts';
const STORAGE_KEY_BLOCKED_UNTIL = 'scapes_login_blocked_until';

/**
 * Attempts to log in a contributor.
 * Enforces brute-force protection client-side.
 */

export async function loginContributor(credentials) {
  const { email, password } = credentials;

  if (!email || !password) {
    return { success: false, message: 'Email and password are required.' };
  }

  // brute-force check 
  const blockedUntil = parseInt(localStorage.getItem(STORAGE_KEY_BLOCKED_UNTIL) || '0', 10);
  const now = Date.now();

  if (blockedUntil && now < blockedUntil) {
    const secsLeft = Math.ceil((blockedUntil - now) / 1000);
    return {
      success: false,
      message: `Too many failed attempts. Try again in ${secsLeft} seconds.`,
      blockedUntil,
    };
  }

  // attempt login
  const result = await AuthRepository.login({ email, password });

  if (result.success) {
    // clear brute-force state 
    localStorage.removeItem(STORAGE_KEY_ATTEMPTS);
    localStorage.removeItem(STORAGE_KEY_BLOCKED_UNTIL);

    setCookie('scapes_token', result.token, 30);
    return { success: true, message: 'Login successful.' };
  }

  // track failed attempt
  const attempts = parseInt(localStorage.getItem(STORAGE_KEY_ATTEMPTS) || '0', 10) + 1;

  if (attempts >= MAX_ATTEMPTS) {
    const newBlockedUntil = Date.now() + BLOCK_DURATION_MS;
    localStorage.setItem(STORAGE_KEY_BLOCKED_UNTIL, String(newBlockedUntil));
    localStorage.removeItem(STORAGE_KEY_ATTEMPTS);
    return {
      success: false,
      message: 'Too many failed attempts. Try again in 30 seconds.',
      blockedUntil: newBlockedUntil,
    };
  }

  localStorage.setItem(STORAGE_KEY_ATTEMPTS, String(attempts));
  return {
    success: false,
    message: result.message || 'Invalid email or password.',
  };
}