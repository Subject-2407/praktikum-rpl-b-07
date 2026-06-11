import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatDate } from '../../core/utils/formatters.js';

export function renderSettingsPage(user = {}) {
  return `
    <section class="space-y-6">
      <div>
        <h1 class="text-3xl font-bold text-accent-heading">Profile</h1>
        <p class="mt-2 text-sm text-body-muted">Informasi sesi aktif yang diambil langsung dari backend.</p>
      </div>
      <div class="panel-card max-w-2xl">
        <dl class="grid gap-4 sm:grid-cols-2">
          <div>
            <dt class="text-sm font-semibold text-body-label">User ID</dt>
            <dd class="mt-1 text-body-strong">${escapeHtml(String(user.id || '-'))}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-body-label">Nama</dt>
            <dd class="mt-1 text-body-strong">${escapeHtml(user.name || user.email || '-')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-body-label">Email</dt>
            <dd class="mt-1 text-body-strong">${escapeHtml(user.email || '-')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-body-label">Role</dt>
            <dd class="mt-1 text-body-strong">${escapeHtml(user.role || 'contributor')}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-body-label">Session source</dt>
            <dd class="mt-1 text-body-strong">Backend session</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-body-label">Session expires</dt>
            <dd class="mt-1 text-body-strong">${formatDate(user.expiresAt || '-')}</dd>
          </div>
        </dl>
      </div>
    </section>
  `;
}

export function initSettingsPage() {}
