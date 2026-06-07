import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatDate } from '../../core/utils/formatters.js';

export function renderSettingsPage(user = {}) {
  return `
    <section class="space-y-6">
      <div>
        <h1 class="text-3xl font-bold text-scapes-light-primary dark:text-scapes-dark-primary">Profile</h1>
        <p class="mt-2 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">Informasi sesi aktif yang diambil langsung dari backend.</p>
      </div>
      <div class="panel-card max-w-2xl">
        <dl class="grid gap-4 sm:grid-cols-2">
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">User ID</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(String(user.id || '-'))}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Nama</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(user.name || user.email || '-')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Email</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(user.email || '-')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Role</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(user.role || 'contributor')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Session source</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">Backend session</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Session expires</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${formatDate(user.expiresAt || '-')}</dd>
          </div>
        </dl>
      </div>
    </section>
  `;
}

export function initSettingsPage() {}
