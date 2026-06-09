/**
 * Auth Guard utility for protecting pages
 */
import { ENV } from '../config/environment.js';

let sessionTimer = null;
let isTimerRunning = false;

function startSessionTimer(expiresAtData) {
    if (isTimerRunning) return;
    isTimerRunning = true;
    
    const expiryTime = new Date(expiresAtData).getTime();

    sessionTimer = setInterval(() => {
        const currentTime = Date.now();
        
        if (currentTime >= expiryTime) {
            clearInterval(sessionTimer);
            window.location.href = "./login.html";
            isTimerRunning = false;
        }
    }, 30 * 1000); // check every 30 seconds
}

export async function isAuthenticated() {
    if (ENV.DEV_MODE) {
        return true;
    }

    try {
        // check if admin have a valid session cookie
        const res = await fetch(`${ENV.API_BASE_URL}/sessions/current`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include' ,
            cache: 'no-store'
        });

        if (!res.ok) return false;

        // verify role admin
        const result = await res.json();
        const isAdmin = result.data?.user?.role === 'admin';

        if (isAdmin && result.data?.expires_at) {
            startSessionTimer(result.data.expires_at);
        }
        
        return isAdmin;
        
    } catch (e) {
        console.warn("Auth check failed:", e);
        return false;
    }
}

export async function requireAuth(redirectTo = "./login.html") {
    if (!(await isAuthenticated())) {
        window.location.href = redirectTo;
        return false;
    }
    return true;
}