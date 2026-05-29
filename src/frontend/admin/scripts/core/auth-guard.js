/**
 * Auth Guard utility for protecting pages
 * DEV_MODE = true bypasses auth checks for UI testing
 */

// Set to true to bypass auth guard (useful for UI testing)
export const DEV_MODE = true;

const TOKEN_KEY = "scapes.admin.token";

export function getStoredToken() {
	try {
		return localStorage.getItem(TOKEN_KEY);
	} catch (e) {
		console.warn("Could not access localStorage:", e);
		return null;
	}
}

export function saveToken(token) {
	try {
		localStorage.setItem(TOKEN_KEY, token);
	} catch (e) {
		console.warn("Could not save to localStorage:", e);
	}
}

export function clearToken() {
	try {
		localStorage.removeItem(TOKEN_KEY);
	} catch (e) {
		console.warn("Could not clear localStorage:", e);
	}
}

export function isAuthenticated() {
	if (DEV_MODE) {
		console.log("[DEV MODE] Auth check bypassed");
		return true;
	}
	return !!getStoredToken();
}

export function requireAuth(redirectTo = "./login.html") {
	if (!isAuthenticated()) {
		window.location.href = redirectTo;
		return false;
	}
	return true;
}