/**
 * Login page with mock auth
 * Accepts any email + password for testing
 */
import { saveToken } from "./core/auth-guard.js";

export function bootstrapLoginPage() {
	const form = document.querySelector("form");
	
	if (!form) return;

	// Prevent default form submission
	form.addEventListener("submit", (e) => {
		e.preventDefault();
		
		const formData = new FormData(form);
		const email = formData.get("email")?.trim();
		const password = formData.get("password")?.trim();

		// Fake login: accept any non-empty email + password
		if (email && password) {
			// Generate a fake token
			const fakeToken = `mock-token-${Date.now()}-${Math.random().toString(36).substring(7)}`;
			
			// Save token to localStorage
			saveToken(fakeToken);
			console.log("[MOCK LOGIN] Token saved:", fakeToken);
			
			// Redirect to queue/index
			setTimeout(() => {
				window.location.href = "./index.html";
			}, 100);
		}
	});
}

bootstrapLoginPage();
