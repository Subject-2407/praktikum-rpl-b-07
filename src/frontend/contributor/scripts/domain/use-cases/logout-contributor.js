import { deleteCookie } from '../../core/http/api-client.js';
import { AuthRepository } from '../../data/repositories/auth-repository.js';

/**
 * Logs out the contributor
 */

export async function logoutContributor() {
  try {
    await AuthRepository.logout();
  } catch {
  }

  deleteCookie('scapes_token');

  // replace current history entry 
  history.replaceState(null, '', 'login.html');
  window.location.href = 'login.html';
}