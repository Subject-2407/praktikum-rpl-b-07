import { escapeHtml } from '../../core/utils/escape-html.js';

function getDisplayName(user = {}) {
  return String(user.display_name || user.displayName || user.name || '').trim() || 'Not set';
}

function getEmail(user = {}) {
  return String(user.email || '').trim() || 'Not available';
}

function getProfileInitials(user = {}) {
  const seed = getDisplayName(user) !== 'Not set'
    ? getDisplayName(user)
    : getEmail(user);

  return seed
    .split(/[ @._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase() || 'CO';
}

function formatSessionExpiry(value) {
  if (!value) return 'Not available';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'long',
    timeStyle: 'short',
  }).format(date);
}

export function renderSettingsPage(user = {}) {
  const displayName = getDisplayName(user);
  const email = getEmail(user);
  const initials = getProfileInitials(user);
  const sessionExpiry = formatSessionExpiry(user.expiresAt);

  return `
    <section class="min-h-full px-3 py-4 sm:px-6 sm:py-6 lg:px-8">
      <div class="mx-auto max-w-5xl space-y-5">
        <div class="rounded-2xl border border-scapes-light-accent bg-white p-5 shadow-sm dark:border-scapes-dark-accent dark:bg-gray-900 sm:p-6">
          <div class="flex flex-col gap-5 lg:flex-row lg:items-center">
            <div class="flex items-center gap-4">
              <div class="flex h-16 w-16 items-center justify-center rounded-full bg-scapes-light-accent text-lg font-bold text-accent-heading dark:bg-scapes-dark-accent">
                ${escapeHtml(initials)}
              </div>
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.28em] text-body-muted">Profile</p>
                <h1 class="mt-2 text-3xl font-bold text-accent-heading">${escapeHtml(displayName)}</h1>
                <p class="mt-2 text-sm text-body-muted">${escapeHtml(email)}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div>
            <h2 class="text-xl font-bold text-accent-heading">Account details</h2>
          </div>

          <dl class="mt-5 grid gap-4">
            <div class="rounded-xl border border-scapes-light-accent bg-scapes-light-base p-4 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
              <dt class="text-sm font-semibold text-body-label">Display name</dt>
              <dd class="mt-2 text-base font-semibold text-body-strong">${escapeHtml(displayName)}</dd>
            </div>
            <div class="rounded-xl border border-scapes-light-accent bg-scapes-light-base p-4 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
              <dt class="text-sm font-semibold text-body-label">Email</dt>
              <dd class="mt-2 text-base font-semibold text-body-strong">${escapeHtml(email)}</dd>
            </div>
            <div class="rounded-xl border border-scapes-light-accent bg-scapes-light-base p-4 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
              <dt class="text-sm font-semibold text-body-label">Session expiration</dt>
              <dd class="mt-2 text-base font-semibold text-body-strong">${escapeHtml(sessionExpiry)}</dd>
            </div>
          </dl>
        </div>
      </div>
    </section>
  `;
}

export function initSettingsPage() {}
