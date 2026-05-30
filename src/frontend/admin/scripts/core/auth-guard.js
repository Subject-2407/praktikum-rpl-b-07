/**
 * Auth Guard utility for protecting pages
 * DEV_MODE = true bypasses auth checks for UI testing
 */
export const DEV_MODE = false;

export function getStoredToken() {
    try {
        const match = document.cookie.match(/scapes_access_token=([^;]+)/);
        return match ? match[1] : null;
    } catch (e) {
        console.warn("Could not access cookie:", e);
        return null;
    }
}

export function saveToken(token) {
    try {
        document.cookie = `scapes_access_token=${token}; path=/; SameSite=Lax`;
    } catch (e) {
        console.warn("Could not save cookie:", e);
    }
}

export function clearToken() {
    try {
        document.cookie = 'scapes_access_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    } catch (e) {
        console.warn("Could not clear cookie:", e);
    }
}

export async function isAuthenticated() {
    if (DEV_MODE) {
        console.log("[DEV MODE] Auth check bypassed");
        return true;
    }
    try {
        const res = await fetch('http://localhost:8000/auth/me', {
            credentials: 'include'
        });
        return res.ok;
    } catch (e) {
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