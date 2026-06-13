import { authRepository } from '../../data/repositories/auth-repository.js';
import { requestPasswordReset } from '../../domain/use-cases/request-password-reset.js';
import { resetPassword } from '../../domain/use-cases/reset-password.js';
import { renderAuthSupportShell } from './auth-support-shell.js';

const INVALID_FIELD_CLASSES = [
  'border-red-400',
  'ring-2',
  'ring-red-200',
  'dark:border-red-500',
  'dark:ring-red-900/40',
];

function escapeHtml(value) {
  return String(value || '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function setFieldInvalidState(field, isInvalid) {
  if (!field) {
    return;
  }

  field.setAttribute('aria-invalid', String(isInvalid));
  INVALID_FIELD_CLASSES.forEach((className) => {
    field.classList.toggle(className, isInvalid);
  });
}

function setMessage(element, message, type = 'error') {
  if (!element) {
    return;
  }

  const palette = type === 'success'
    ? 'border-green-500 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-300'
    : 'border-red-500 bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-300';

  element.className = `rounded-sm border-l-4 p-3 text-sm ${palette}`;
  element.textContent = message;
  element.classList.toggle('hidden', !message);
}

function renderRequestPanel() {
  return `
    <section class="rounded-[2rem] border border-black/8 bg-white/88 p-6 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur dark:border-white/10 dark:bg-[#0f1719] sm:p-8">
      <p class="text-xs font-semibold uppercase tracking-[0.3em] text-scapes-light-primary dark:text-scapes-dark-secondary">
        Password recovery
      </p>
      <h1 class="mt-4 text-[2.35rem] font-bold leading-[1.02] tracking-[-0.05em] text-gray-950 dark:text-white">
        Request a fresh reset link.
      </h1>
      <p class="mt-5 text-sm leading-7 text-gray-600 dark:text-gray-300">
        Enter the email tied to your contributor account. If it exists, Scapes will send a reset link without revealing whether the account is registered.
      </p>

      <form id="password-reset-request-form" class="mt-8 space-y-5" novalidate>
        <div id="password-reset-request-message" class="hidden" role="alert"></div>
        <div>
          <label for="password-reset-email" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">
            Account email
          </label>
          <input id="password-reset-email" name="email" type="email" autocomplete="email" class="field-control" placeholder="email@example.com">
        </div>
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <a href="/login" class="text-sm font-medium text-gray-600 transition-colors duration-300 hover:text-scapes-light-primary dark:text-gray-300 dark:hover:text-scapes-dark-primary">
            Back to login
          </a>
          <button id="password-reset-request-submit" type="submit" class="inline-flex min-h-12 items-center justify-center rounded-full bg-scapes-light-primary px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-scapes-light-secondary disabled:cursor-not-allowed disabled:opacity-60 dark:bg-scapes-dark-primary dark:text-gray-950 dark:hover:bg-[#56c6d1]">
            Send reset link
          </button>
        </div>
      </form>
    </section>
  `;
}

function renderRequestSuccessPanel() {
  return `
    <section class="rounded-[2rem] border border-green-500/35 bg-green-50/90 p-6 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur dark:bg-green-900/10 sm:p-8">
      <div class="flex items-start gap-4">
        <div class="inline-flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-green-500 text-white dark:bg-green-400 dark:text-gray-950">
          <i class="fa-solid fa-envelope-circle-check text-xl" aria-hidden="true"></i>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-green-700 dark:text-green-300">
            Email sent
          </p>
          <h2 class="mt-3 text-[2rem] font-bold leading-tight tracking-[-0.04em] text-green-900 dark:text-white">
            Check your inbox.
          </h2>
          <p id="password-reset-request-success-message" class="mt-4 text-sm leading-7 text-green-800 dark:text-green-100">
            If that email is registered, a password reset link has been sent.
          </p>
          <div class="mt-7 flex flex-col gap-3 sm:flex-row">
            <a href="/login" class="inline-flex min-h-12 items-center justify-center rounded-full bg-green-600 px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-green-700 dark:bg-green-400 dark:text-gray-950 dark:hover:bg-green-300">
              Return to login
            </a>
            <button id="password-reset-request-another" type="button" class="inline-flex min-h-12 items-center justify-center rounded-full border border-green-600/30 px-6 text-sm font-semibold text-green-700 transition-colors duration-300 hover:border-green-700 hover:text-green-800 dark:border-green-400/40 dark:text-green-200 dark:hover:border-green-300 dark:hover:text-green-100">
              Use another email
            </button>
          </div>
        </div>
      </div>
    </section>
  `;
}

function renderResetPanel(token) {
  return `
    <section class="rounded-[2rem] border border-black/8 bg-white/88 p-6 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur dark:border-white/10 dark:bg-[#0f1719] sm:p-8">
      <p class="text-xs font-semibold uppercase tracking-[0.3em] text-scapes-light-primary dark:text-scapes-dark-secondary">
        Create a new password
      </p>
      <h1 class="mt-4 text-[2.35rem] font-bold leading-[1.02] tracking-[-0.05em] text-gray-950 dark:text-white">
        Finish your password reset.
      </h1>
      <p class="mt-5 text-sm leading-7 text-gray-600 dark:text-gray-300">
        This link came from your contributor password reset email. Choose a new password with at least 8 characters to reactivate sign-in.
      </p>

      <div class="mt-6 rounded-[1.45rem] border border-black/8 bg-black/[0.03] p-4 text-xs leading-6 text-gray-600 dark:border-white/10 dark:bg-white/[0.03] dark:text-gray-300">
        <p class="font-semibold uppercase tracking-[0.24em] text-scapes-light-secondary dark:text-scapes-dark-secondary">
          Reset token
        </p>
        <p class="mt-3 break-all font-mono text-[0.74rem] text-gray-700 dark:text-gray-200">${escapeHtml(token)}</p>
      </div>

      <form id="password-reset-form" class="mt-8 space-y-5" novalidate>
        <div id="password-reset-message" class="hidden" role="alert"></div>
        <div>
          <label for="password-reset-new" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">
            New password
          </label>
          <input id="password-reset-new" name="password" type="password" autocomplete="new-password" class="field-control" placeholder="Minimum 8 characters">
        </div>
        <div>
          <label for="password-reset-confirm" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">
            Confirm new password
          </label>
          <input id="password-reset-confirm" name="confirmPassword" type="password" autocomplete="new-password" class="field-control" placeholder="Retype your password">
        </div>
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <a href="/login" class="text-sm font-medium text-gray-600 transition-colors duration-300 hover:text-scapes-light-primary dark:text-gray-300 dark:hover:text-scapes-dark-primary">
            Back to login
          </a>
          <button id="password-reset-submit" type="submit" class="inline-flex min-h-12 items-center justify-center rounded-full bg-gray-950 px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-gray-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-white dark:text-gray-950 dark:hover:bg-gray-200">
            Save new password
          </button>
        </div>
      </form>
    </section>
  `;
}

function renderResetSuccessPanel() {
  return `
    <section class="rounded-[2rem] border border-green-500/35 bg-green-50/90 p-6 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur dark:bg-green-900/10 sm:p-8">
      <div class="flex items-start gap-4">
        <div class="inline-flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-green-500 text-white dark:bg-green-400 dark:text-gray-950">
          <i class="fa-solid fa-shield-heart text-xl" aria-hidden="true"></i>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-green-700 dark:text-green-300">
            Password updated
          </p>
          <h2 class="mt-3 text-[2rem] font-bold leading-tight tracking-[-0.04em] text-green-900 dark:text-white">
            You can sign in again.
          </h2>
          <p id="password-reset-success-message" class="mt-4 text-sm leading-7 text-green-800 dark:text-green-100">
            Password reset successfully. You can now log in with your new password.
          </p>
          <div class="mt-7 flex flex-col gap-3 sm:flex-row">
            <a href="/login" class="inline-flex min-h-12 items-center justify-center rounded-full bg-green-600 px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-green-700 dark:bg-green-400 dark:text-gray-950 dark:hover:bg-green-300">
              Continue to login
            </a>
            <a href="/password-resets" class="inline-flex min-h-12 items-center justify-center rounded-full border border-green-600/30 px-6 text-sm font-semibold text-green-700 transition-colors duration-300 hover:border-green-700 hover:text-green-800 dark:border-green-400/40 dark:text-green-200 dark:hover:border-green-300 dark:hover:text-green-100">
              Request another link
            </a>
          </div>
        </div>
      </div>
    </section>
  `;
}

export function renderPasswordResetPage({ token }) {
  const isResetWithToken = Boolean(token);

  return renderAuthSupportShell({
    eyebrow: isResetWithToken ? 'Reset password' : 'Password recovery',
    title: isResetWithToken
      ? 'Set a new contributor password.'
      : 'Recover your contributor access.',
    description: isResetWithToken
      ? 'Your reset link already includes the secure token from Scapes. All that remains is choosing a strong new password.'
      : 'Forgot your password? Request a recovery link and we will route you back here with a time-limited token.',
    accentClass: isResetWithToken
      ? 'from-[#1d4ed8]/18 via-scapes-light-primary/16 to-transparent dark:from-[#2563eb]/22 dark:via-scapes-dark-primary/18 dark:to-transparent'
      : 'from-[#c87b0d]/20 via-scapes-light-highlight/18 to-transparent dark:from-[#f59e0b]/20 dark:via-[#facc15]/10 dark:to-transparent',
    cardMarkup: `
      <div id="password-reset-page-root" data-has-token="${isResetWithToken ? 'true' : 'false'}" class="space-y-6">
        ${isResetWithToken ? renderResetPanel(token) : renderRequestPanel()}
      </div>
    `,
  });
}

export function initPasswordResetPage({ navigate, token }) {
  const pageRoot = document.getElementById('password-reset-page-root');
  const hasToken = pageRoot?.dataset.hasToken === 'true';

  if (!pageRoot) {
    return;
  }

  if (!hasToken) {
    const form = document.getElementById('password-reset-request-form');
    const messageRoot = document.getElementById('password-reset-request-message');
    const emailInput = document.getElementById('password-reset-email');
    const submitButton = document.getElementById('password-reset-request-submit');

    emailInput?.addEventListener('input', () => {
      setFieldInvalidState(emailInput, false);
      setMessage(messageRoot, '');
    });

    form?.addEventListener('submit', async (event) => {
      event.preventDefault();

      submitButton.disabled = true;
      submitButton.textContent = 'Sending link...';
      setMessage(messageRoot, '');

      try {
        const result = await requestPasswordReset(
          authRepository,
          String(new FormData(form).get('email') || ''),
        );
        pageRoot.innerHTML = renderRequestSuccessPanel();
        const successMessage = document.getElementById('password-reset-request-success-message');
        if (successMessage) {
          successMessage.textContent = result.message;
        }
        document.getElementById('password-reset-request-another')?.addEventListener('click', () => {
          navigate('/password-resets', { replace: true });
        });
      } catch (error) {
        setFieldInvalidState(emailInput, true);
        setMessage(messageRoot, error.message || 'We could not send the reset email.');
      } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Send reset link';
      }
    });

    return;
  }

  const form = document.getElementById('password-reset-form');
  const messageRoot = document.getElementById('password-reset-message');
  const passwordInput = document.getElementById('password-reset-new');
  const confirmInput = document.getElementById('password-reset-confirm');
  const submitButton = document.getElementById('password-reset-submit');

  const clearErrors = () => {
    setFieldInvalidState(passwordInput, false);
    setFieldInvalidState(confirmInput, false);
    setMessage(messageRoot, '');
  };

  passwordInput?.addEventListener('input', clearErrors);
  confirmInput?.addEventListener('input', () => {
    clearErrors();
    const password = String(passwordInput?.value || '');
    const confirmation = String(confirmInput?.value || '');
    setFieldInvalidState(confirmInput, confirmation.length > 0 && password !== confirmation);
  });

  form?.addEventListener('submit', async (event) => {
    event.preventDefault();

    submitButton.disabled = true;
    submitButton.textContent = 'Saving password...';
    clearErrors();

    try {
      const formData = new FormData(form);
      const result = await resetPassword(authRepository, token, {
        password: String(formData.get('password') || ''),
        confirmPassword: String(formData.get('confirmPassword') || ''),
      });

      pageRoot.innerHTML = renderResetSuccessPanel();
      const successMessage = document.getElementById('password-reset-success-message');
      if (successMessage) {
        successMessage.textContent = result.message;
      }
      window.setTimeout(() => navigate('/login', { replace: true }), 1800);
    } catch (error) {
      const message = error.message || 'We could not reset your password.';
      const normalizedMessage = message.toLowerCase();

      if (normalizedMessage.includes('confirmation') || normalizedMessage.includes('confirm')) {
        setFieldInvalidState(confirmInput, true);
        confirmInput?.focus();
      } else if (normalizedMessage.includes('password')) {
        setFieldInvalidState(passwordInput, true);
        passwordInput?.focus();
      }

      setMessage(messageRoot, message);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Save new password';
    }
  });
}
