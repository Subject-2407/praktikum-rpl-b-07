/**
 * Login page with form validation and mock authentication.
 */
import { saveToken } from "./core/auth-guard.js";

export function bootstrapLoginPage() {
	const form = document.querySelector("form");
	const emailInput = document.querySelector("input[name='email']");
	const passwordInput = document.querySelector("input[name='password']");
	const emailError = document.querySelector("#emailError");
	const passwordError = document.querySelector("#passwordError");
	
	if (!form) return;

	function showError(input, errorEl, message) {
		input.classList.add('error');
		errorEl.textContent = message;
		errorEl.classList.remove('hidden');
	}

	function clearError(input, errorEl) {
		input.classList.remove('error');
		errorEl.textContent = '';
		errorEl.classList.add('hidden');
	}

	// Clear errors when user starts typing
	emailInput.addEventListener('input', () => {
		clearError(emailInput, emailError);
	});

	passwordInput.addEventListener('input', () => {
		clearError(passwordInput, passwordError);
	});

	// Validate only on form submit
	form.addEventListener("submit", (e) => {
		e.preventDefault();
		
		const email = emailInput.value?.trim();
		const password = passwordInput.value?.trim();

		// Check if fields are empty
		let isValid = true;
		if (!email) {
			showError(emailInput, emailError, 'Email is required');
			isValid = false;
		} else {
			clearError(emailInput, emailError);
		}

		if (!password) {
			showError(passwordInput, passwordError, 'Password is required');
			isValid = false;
		} else {
			clearError(passwordInput, passwordError);
		}

		if (!isValid) {
			return;
		}

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