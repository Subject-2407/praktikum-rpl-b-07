/**
 * Login page with form validation
 */
import { isAuthenticated } from "./core/auth-guard.js";
import { ENV } from "./config/environment.js";

export async function bootstrapLoginPage() {
    // Auto-redirect if already logged in
    if (await isAuthenticated()) {
        window.location.href = './index.html';
        return;
    }

	const form = document.querySelector("form");
	const emailInput = document.querySelector("input[name='email']");
	const passwordInput = document.querySelector("input[name='password']");
	const passwordToggleBtn = document.querySelector("#passwordToggle");
	const submitBtn = document.querySelector("#submitBtn");
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

	// Password visibility toggle
	if (passwordToggleBtn && passwordInput) {
		passwordToggleBtn.addEventListener('click', function(e) {
			e.preventDefault();
			e.stopPropagation();
			
			console.log('Toggle clicked, current type:', passwordInput.type);
			
			if (passwordInput.type === 'password') {
				// Show password
				passwordInput.type = 'text';
				passwordToggleBtn.innerHTML = '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M3 3l18 18M10.477 10.477A3 3 0 0013.5 13.5M6.357 6.357A9.953 9.953 0 002.458 12c1.274 4.057 5.065 7 9.542 7 1.822 0 3.532-.487 5-.337m2.186-2.186A9.953 9.953 0 0021.542 12c-1.274-4.057-5.064-7-9.542-7-1.116 0-2.19.19-3.186.527"/></svg>';
			} else {
				// Hide password
				passwordInput.type = 'password';
				passwordToggleBtn.innerHTML = '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>';
			}
		}, false);
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
		} else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
			showError(emailInput, emailError, 'Please enter a valid email');
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

		// Show loading state
		submitBtn.disabled = true;
		submitBtn.textContent = 'Signing in...';
		submitBtn.classList.add('opacity-70');

		// Login API call
		fetch(`${ENV.API_BASE_URL}/sessions`, {
			method: 'POST',
			credentials: 'include',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email, password })
		})
		.then(res => res.json())
		.then(data => {
			if (data.success) {
				if (data.data.user.role !== 'admin') {
					submitBtn.disabled = false;
					submitBtn.textContent = 'Log in';
					submitBtn.classList.remove('opacity-70');
					showError(emailInput, emailError, 'Access denied. Admin only.');
					return;
				}
				window.location.href = './index.html';
			} else {
				submitBtn.disabled = false;
				submitBtn.textContent = 'Log in';
				submitBtn.classList.remove('opacity-70');
				showError(emailInput, emailError, data.message || 'Invalid credentials');
			}
		})
		.catch(() => {
			submitBtn.disabled = false;
			submitBtn.textContent = 'Log in';
			submitBtn.classList.remove('opacity-70');
			showError(emailInput, emailError, 'Connection error. Try again.');
		});
	});
}

bootstrapLoginPage();