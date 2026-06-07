import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatDate } from '../../core/utils/formatters.js';
import { renderStatusBadge } from '../components/status-badge.js';

function renderTagList(tags = []) {
  if (!Array.isArray(tags) || !tags.length) {
    return '<span class="text-scapes-light-secondary dark:text-scapes-dark-secondary">-</span>';
  }

  return tags.map((tag) => `
    <span class="rounded-full border border-scapes-light-accent px-2.5 py-1 text-xs font-medium text-scapes-light-primary dark:border-scapes-dark-accent dark:text-scapes-dark-primary">
      ${escapeHtml(tag.name || String(tag))}
    </span>
  `).join('');
}

export function renderWallpaperDetailPage(wallpaper) {
  if (!wallpaper) {
    return `
      <section class="panel-card">
        <h1 class="text-2xl font-bold text-scapes-light-primary dark:text-scapes-dark-primary">Wallpaper tidak ditemukan</h1>
        <a href="/dashboard" class="secondary-button mt-4">Kembali</a>
      </section>
    `;
  }

  return `
    <section class="space-y-6">
      <a href="/dashboard" class="secondary-button">Kembali</a>
      <article class="panel-card">
        <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h1 class="text-3xl font-bold text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(wallpaper.title)}</h1>
            <p class="mt-2 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">${escapeHtml(wallpaper.description || 'Tidak ada deskripsi.')}</p>
          </div>
          ${renderStatusBadge(wallpaper.status)}
        </div>
        <dl class="mt-6 grid gap-4 sm:grid-cols-2">
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Kategori</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(wallpaper.category)}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Target device</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${escapeHtml(wallpaper.targetDevice || '-')}</dd>
          </div>
          <div class="sm:col-span-2">
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Tags</dt>
            <dd class="mt-2 flex flex-wrap gap-2">${renderTagList(wallpaper.tags)}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Update terakhir</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${formatDate(wallpaper.updatedAt)}</dd>
          </div>
          <div>
            <dt class="text-sm font-semibold text-scapes-light-secondary dark:text-scapes-dark-secondary">Dibuat</dt>
            <dd class="mt-1 text-scapes-light-primary dark:text-scapes-dark-primary">${formatDate(wallpaper.createdAt)}</dd>
          </div>
        </dl>
        ${wallpaper.rejectionReason ? `
          <div class="mt-6 rounded-md border-l-4 border-red-500 bg-red-50 p-4 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400">
            <strong>Alasan rejection:</strong> ${escapeHtml(wallpaper.rejectionReason)}
          </div>
        ` : ''}
      </article>
    </section>
  `;
}
