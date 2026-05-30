/**
 * Secure logout handler
 */
import { DEV_MODE } from './core/auth-guard.js';

document.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.querySelector('a[href="./login.html"]');

    if (!logoutBtn) return;

    logoutBtn.addEventListener('click', async (e) => {
        e.preventDefault(); 

        if (!DEV_MODE) {
            try {
                // revoke session and destroy HttpOnly cookie
                await fetch('http://localhost:8000/sessions/current', {
                    method: 'DELETE',
                    headers: { 'Accept': 'application/json' },
                    credentials: 'include'
                });
            } catch (error) {
                console.warn("Backend logout failed, but redirecting anyway.");
            }
        }

        // redirect to login
        window.location.href = './login.html';
    });
});