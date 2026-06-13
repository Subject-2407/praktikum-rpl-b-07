import { authRepository } from '../../data/repositories/auth-repository.js';
import { verifyEmail } from '../../domain/use-cases/verify-email.js';
import { renderAuthSupportShell } from './auth-support-shell.js';

function escapeHtml(value) {
  return String(value || '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function getStatusPresentation(status) {
  if (status === 'success') {
    return {
      badge: 'Verified',
      icon: 'fa-solid fa-circle-check',
      iconClass: 'bg-green-500 text-white dark:bg-green-400 dark:text-gray-950',
      panelClass: 'border-green-500/35 bg-green-50/90 text-green-800 dark:bg-green-900/12 dark:text-green-100',
      description: 'Your contributor account is active now. You can continue to the login screen and start managing your workspace.',
    };
  }

  if (status === 'pending') {
    return {
      badge: 'Checking token',
      icon: 'fa-solid fa-spinner fa-spin',
      iconClass: 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-gray-950',
      panelClass: 'border-scapes-light-primary/25 bg-white/90 text-gray-900 dark:border-scapes-dark-primary/20 dark:bg-[#101719] dark:text-white',
      description: 'We are validating your verification link with the backend. This should only take a moment.',
    };
  }

  return {
    badge: 'Verification issue',
    icon: 'fa-solid fa-triangle-exclamation',
    iconClass: 'bg-red-500 text-white dark:bg-red-400 dark:text-gray-950',
    panelClass: 'border-red-500/35 bg-red-50/90 text-red-800 dark:bg-red-900/12 dark:text-red-100',
    description: 'This link could not be verified. The token may be missing, expired, or already used.',
  };
}

function renderActionMarkup(status) {
  if (status === 'success') {
    return `
      <div class="mt-8 flex flex-col gap-3 sm:flex-row">
        <a href="/login" class="inline-flex min-h-12 items-center justify-center rounded-full bg-scapes-light-primary px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-scapes-light-secondary dark:bg-scapes-dark-primary dark:text-gray-950 dark:hover:bg-[#56c6d1]">
          Continue to login
        </a>
        <a href="/dashboard" class="inline-flex min-h-12 items-center justify-center rounded-full border border-black/10 px-6 text-sm font-semibold text-gray-700 transition-colors duration-300 hover:border-scapes-light-primary hover:text-scapes-light-primary dark:border-white/10 dark:text-gray-200 dark:hover:border-scapes-dark-primary dark:hover:text-scapes-dark-primary">
          Open portal
        </a>
      </div>
    `;
  }

  if (status === 'pending') {
    return `
      <div class="mt-8 flex items-center gap-3 text-sm text-gray-500 dark:text-gray-400">
        <span class="inline-flex h-2.5 w-2.5 rounded-full bg-scapes-light-primary dark:bg-scapes-dark-primary"></span>
        Verifying token with Scapes API...
      </div>
    `;
  }

  return `
    <div class="mt-8 flex flex-col gap-3 sm:flex-row">
      <button id="verification-retry" type="button" class="inline-flex min-h-12 items-center justify-center rounded-full bg-gray-950 px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-gray-800 dark:bg-white dark:text-gray-950 dark:hover:bg-gray-200">
        Try again
      </button>
      <a href="/login" class="inline-flex min-h-12 items-center justify-center rounded-full border border-black/10 px-6 text-sm font-semibold text-gray-700 transition-colors duration-300 hover:border-scapes-light-primary hover:text-scapes-light-primary dark:border-white/10 dark:text-gray-200 dark:hover:border-scapes-dark-primary dark:hover:text-scapes-dark-primary">
        Back to login
      </a>
    </div>
  `;
}

function renderStatusCard({ status, title, message, token }) {
  const presentation = getStatusPresentation(status);
  const tokenMarkup = token
    ? `
      <div class="mt-6 rounded-[1.4rem] border border-black/8 bg-black/[0.03] p-4 text-xs leading-6 text-gray-600 dark:border-white/10 dark:bg-white/[0.03] dark:text-gray-300">
        <p class="font-semibold uppercase tracking-[0.22em] text-scapes-light-secondary dark:text-scapes-dark-secondary">
          Verification token
        </p>
        <p class="mt-3 break-all font-mono text-[0.74rem] leading-6 text-gray-700 dark:text-gray-200">${escapeHtml(token)}</p>
      </div>
    `
    : '';

  return `
    <section class="rounded-[2rem] border ${presentation.panelClass} p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)] backdrop-blur sm:p-8">
      <div class="flex items-start gap-4">
        <div class="inline-flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl ${presentation.iconClass}">
          <i class="${presentation.icon} text-xl" aria-hidden="true"></i>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-scapes-light-secondary dark:text-scapes-dark-secondary">
            ${presentation.badge}
          </p>
          <h2 class="mt-3 text-[2rem] font-bold leading-tight tracking-[-0.04em]">
            ${title}
          </h2>
          <p class="mt-4 text-sm leading-7 opacity-90">
            ${message}
          </p>
          <p class="mt-4 text-sm leading-7 text-gray-600 dark:text-gray-300">
            ${presentation.description}
          </p>
          ${tokenMarkup}
          ${renderActionMarkup(status)}
        </div>
      </div>
    </section>
  `;
}

export function renderEmailVerificationPage({ token }) {
  return renderAuthSupportShell({
    eyebrow: 'Email verification',
    title: 'Activate your contributor access.',
    description: 'Verification links from Scapes land here first. Once the token is confirmed, your contributor account is ready to log in and submit wallpapers.',
    accentClass: 'from-scapes-light-primary/22 via-scapes-light-highlight/18 to-transparent dark:from-scapes-dark-primary/24 dark:via-scapes-dark-secondary/16 dark:to-transparent',
    cardMarkup: `
      <div class="space-y-6">
        <div class="rounded-[2rem] border border-black/8 bg-white/85 p-6 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur dark:border-white/10 dark:bg-[#0f1719] sm:p-8">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-scapes-light-primary dark:text-scapes-dark-secondary">
            Verification handoff
          </p>
          <h1 class="mt-4 text-[2.35rem] font-bold leading-[1.02] tracking-[-0.05em] text-gray-950 dark:text-white">
            Confirming your email with Scapes
          </h1>
          <p class="mt-5 text-sm leading-7 text-gray-600 dark:text-gray-300">
            This page will validate the token from your email against the contributor auth API.
          </p>
        </div>

        <div id="email-verification-status">
          ${renderStatusCard({
            status: 'pending',
            title: 'Checking your verification link...',
            message: token
              ? 'We found a token in the current URL and are sending it to the backend now.'
              : 'No verification token was found in the current URL.',
            token,
          })}
        </div>
      </div>
    `,
  });
}

export function initEmailVerificationPage({ navigate, token }) {
  const statusRoot = document.getElementById('email-verification-status');
  let currentToken = String(token || '').trim();
  let isSubmitting = false;

  const paintState = ({ status, title, message }) => {
    if (!statusRoot) {
      return;
    }

    statusRoot.innerHTML = renderStatusCard({
      status,
      title,
      message,
      token: currentToken,
    });

    statusRoot.querySelector('#verification-retry')?.addEventListener('click', () => {
      void runVerification();
    });
  };

  const runVerification = async () => {
    if (isSubmitting) {
      return;
    }

    if (!currentToken) {
      paintState({
        status: 'error',
        title: 'Verification token missing',
        message: 'Open the verification link directly from your email, or request a new verification email from the registration flow.',
      });
      return;
    }

    isSubmitting = true;
    paintState({
      status: 'pending',
      title: 'Checking your verification link...',
      message: 'We are confirming the token with the backend. Please keep this tab open for a moment.',
    });

    try {
      const result = await verifyEmail(authRepository, currentToken);
      paintState({
        status: 'success',
        title: 'Email verified successfully',
        message: result.message,
      });
      window.setTimeout(() => navigate('/login', { replace: true }), 1600);
    } catch (error) {
      paintState({
        status: 'error',
        title: 'Verification could not be completed',
        message: error.message || 'We could not verify this email token.',
      });
    } finally {
      isSubmitting = false;
    }
  };

  void runVerification();
}
