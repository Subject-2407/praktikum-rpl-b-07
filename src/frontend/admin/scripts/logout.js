/**
 * Secure logout handler
 */
import { ENV } from "./config/environment.js";

document.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.querySelector('#logoutButton');

    if (!logoutBtn) return;

    logoutBtn.addEventListener('click', async (e) => {
        e.preventDefault(); 

        try {
            // revoke session and destroy HttpOnly cookie
            await fetch(`${ENV.API_BASE_URL}/sessions/current`, {
                method: 'DELETE',
                headers: { 'Accept': 'application/json' },
                credentials: 'include'
            });
        } catch (error) {
            console.warn("Backend logout failed, but redirecting anyway.");
        }

        // redirect to login
        window.location.href = './login.html';
    });
});