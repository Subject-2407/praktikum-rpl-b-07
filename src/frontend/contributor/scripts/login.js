import { loginContributor } from '../../domain/use-cases/login-contributor.js';
import { registerContributor } from '../../domain/use-cases/register-contributor.js';
import { requestPasswordReset } from '../../domain/use-cases/request-password-reset.js';
import { hasValidSession } from '../../core/http/api-client.js';

/**
 * Initializes the login/register page behavior.
 * Handles card toggling, form submission, and modal display.
 */

export function initLoginPage() {
  if (hasValidSession()) {
    window.location.replace('portal.html');
    return;
  }

  // element references
  const loginCard = document.getElementById('login-card');
  const registerCard = document.getElementById('register-card');
  const forgotCard = document.getElementById('forgot-card');
  const inboxCard = document.getElementById('inbox-card');

  const loginError = document.getElementById('login-error');
  const loginBlocked = document.getElementById('login-blocked');
  const registerError = document.getElementById('register-error');
  const registerSuccess = document.getElementById('register-success');
  const forgotError = document.getElementById('forgot-error');
  const forgotSuccess = document.getElementById('forgot-success');
  const resendError = document.getElementById('resend-error');
  const resendSuccess = document.getElementById('resend-success');

  const loginBtn = document.getElementById('login-btn');
  const registerBtn = document.getElementById('register-btn');
  const forgotBtn = document.getElementById('forgot-btn');

  const tosModal = document.getElementById('tos-modal');
  const privacyModal = document.getElementById('privacy-modal');

  // helpers
  function setError(el, msg) {
    el.textContent = msg;
    el.classList.toggle('hidden', !msg);
  }

  function setSuccess(el, msg) {
    el.textContent = msg;
    el.classList.toggle('hidden', !msg);
  }

  function setButtonLoading(btn, text) {
    btn.disabled = true;
    btn.textContent = text;
  }

  function resetButton(btn, text) {
    btn.disabled = false;
    btn.textContent = text;
  }

  function clearLoginErrors() {
    setError(loginError, '');
    setError(loginBlocked, '');
  }

  function clearRegisterMessages() {
    setError(registerError, '');
    setSuccess(registerSuccess, '');
  }

  function clearRegisterForm() {
    ['reg-name', 'reg-email', 'reg-password', 'reg-confirm'].forEach((id) => {
      document.getElementById(id).value = '';
    });
    document.getElementById('reg-tos').checked = false;
    clearRegisterMessages();
  }

  // card toggles
  document.getElementById('go-register').addEventListener('click', () => {
    loginCard.classList.add('hidden');
    registerCard.classList.remove('hidden');
    clearRegisterForm();
  });

  document.getElementById('go-login').addEventListener('click', () => {
    registerCard.classList.add('hidden');
    loginCard.classList.remove('hidden');
    clearLoginErrors();
  });

  document.getElementById('go-forgot').addEventListener('click', () => {
    loginCard.classList.add('hidden');
    forgotCard.classList.remove('hidden');
    setError(forgotError, '');
    document.getElementById('forgot-email').value = '';
  });

  document.getElementById('forgot-back').addEventListener('click', () => {
    forgotCard.classList.add('hidden');
    loginCard.classList.remove('hidden');
  });

  document.getElementById('inbox-back').addEventListener('click', () => {
    inboxCard.classList.add('hidden');
    loginCard.classList.remove('hidden');
  });

  // login submit
  loginBtn.addEventListener('click', async () => {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    clearLoginErrors();
    setButtonLoading(loginBtn, 'Signing in...');

    const result = await loginContributor({ email, password });

    if (result.success) {
      window.location.replace('portal.html');
      return;
    }

    if (result.blockedUntil) {
      startBlockedCountdown(result.blockedUntil);
    } else {
      setError(loginError, result.message);
    }

    resetButton(loginBtn, 'Log in');
  });

  // register submit
  registerBtn.addEventListener('click', async () => {
    if (!document.getElementById('reg-tos').checked) {
      setError(registerError, 'You must agree to the Terms of Service to continue.');
      return;
    }

    const payload = {
      name: document.getElementById('reg-name').value,
      email: document.getElementById('reg-email').value.trim(),
      password: document.getElementById('reg-password').value,
      confirmPassword: document.getElementById('reg-confirm').value,
    };

    clearRegisterMessages();
    setButtonLoading(registerBtn, 'Creating account...');

    const result = await registerContributor(payload);

    if (result.success) {
      setSuccess(registerSuccess, result.message);
      clearRegisterForm();
    } else {
      setError(registerError, result.message);
    }

    resetButton(registerBtn, 'Get Started');
  });

  // forgot submit
  forgotBtn.addEventListener('click', async () => {
    const email = document.getElementById('forgot-email').value.trim();

    setError(forgotError, '');
    setSuccess(forgotSuccess, '');
    setButtonLoading(forgotBtn, 'Sending...');

    const result = await requestPasswordReset(email);

    if (result.success) {
      forgotCard.classList.add('hidden');
      inboxCard.classList.remove('hidden');
      inboxCard.dataset.email = email;
    } else {
      setError(forgotError, result.message);
    }

    resetButton(forgotBtn, 'Send Reset Link');
  });

  // resend reset link
  document.getElementById('resend-btn').addEventListener('click', async () => {
    const email = inboxCard.dataset.email || '';

    setError(resendError, '');
    setSuccess(resendSuccess, '');

    const result = await requestPasswordReset(email);

    if (result.success) {
      setSuccess(resendSuccess, 'Reset link resent! Check your inbox.');
    } else {
      setError(resendError, result.message);
    }
  });

  // tos and privacy modals
  document.getElementById('open-tos').addEventListener('click', () => tosModal.classList.remove('hidden'));
  document.getElementById('close-tos').addEventListener('click', () => tosModal.classList.add('hidden'));
  document.getElementById('open-privacy').addEventListener('click', () => privacyModal.classList.remove('hidden'));
  document.getElementById('close-privacy').addEventListener('click', () => privacyModal.classList.add('hidden'));

  [tosModal, privacyModal].forEach((modal) => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) modal.classList.add('hidden');
    });
  });

  // brute-force countdown
  let countdownInterval = null;

  function startBlockedCountdown(blockedUntil) {
    clearInterval(countdownInterval);
    loginBtn.disabled = true;

    function tick() {
      const secsLeft = Math.ceil((blockedUntil - Date.now()) / 1000);
      if (secsLeft <= 0) {
        clearInterval(countdownInterval);
        loginBlocked.classList.add('hidden');
        loginBtn.disabled = false;
        return;
      }
      setError(loginBlocked, `Too many failed attempts. Try again in ${secsLeft} seconds.`);
    }

    tick();
    countdownInterval = setInterval(tick, 1000);
  }
}