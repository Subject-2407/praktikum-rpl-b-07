/**
 * Auth Guard utility for protecting pages
 * DEV_MODE = true bypasses auth checks for UI testing
 */
export const DEV_MODE = false;

export async function isAuthenticated() {
    if (DEV_MODE) {
        console.log("[DEV MODE] Auth check bypassed");
        return true;
    }

    try {
        // check if admin have a valid session cookie
        const res = await fetch('http://localhost:8000/v1/sessions/current', {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include' 
        });

        if (!res.ok) return false;

        // verify role admin
        const result = await res.json();
        return result.data?.user?.role === 'admin';
        
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