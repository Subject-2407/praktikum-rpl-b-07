import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatDate } from '../../core/utils/formatters.js';
import { wallpaperRepository } from '../../data/repositories/wallpaper-repository.js';
import { deleteWallpaper } from '../../domain/use-cases/delete-wallpaper.js';
import { listContributorWallpapers } from '../../domain/use-cases/list-contributor-wallpapers.js';
import { renderStatusBadge } from '../components/status-badge.js';
import { renderToast } from '../components/toast.js';

function renderSummary(wallpapers) {
  const summary = document.getElementById('dashboard-summary');
  const counts = {
    Pending: wallpapers.filter((item) => String(item.status).toLowerCase() === 'pending').length,
    Approved: wallpapers.filter((item) => String(item.status).toLowerCase() === 'approved').length,
    Rejected: wallpapers.filter((item) => String(item.status).toLowerCase() === 'rejected').length,
  };

  summary.innerHTML = Object.entries(counts).map(([label, count]) => `
    <article class="panel-card">
      <p class="text-sm font-semibold text-body-muted">${label}</p>
      <p class="mt-2 text-3xl font-bold text-body-strong">${count}</p>
    </article>
  `).join('');
}

function renderList(wallpapers) {
  const list = document.getElementById('wallpaper-list');

  if (!wallpapers.length) {
    list.innerHTML = `
      <div class="rounded-md border border-scapes-light-accent p-5 text-sm text-body-muted dark:border-scapes-dark-accent">
        Belum ada wallpaper. Mulai upload karya pertama kamu.
      </div>
    `;
    return;
  }

  list.innerHTML = wallpapers.map((wallpaper) => `
    <article class="rounded-lg border border-scapes-light-accent bg-scapes-light-base p-4 transition-colors duration-300 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div class="min-w-0">
          <div class="flex flex-wrap items-center gap-2">
            <h3 class="font-heading text-lg font-bold text-accent-heading">${escapeHtml(wallpaper.title)}</h3>
            ${renderStatusBadge(wallpaper.status)}
          </div>
          <p class="mt-1 text-sm text-body-muted">${escapeHtml(wallpaper.description || 'Tidak ada deskripsi.')}</p>
          <p class="mt-2 text-xs text-body-muted">${escapeHtml(wallpaper.category)} &bull; Update ${formatDate(wallpaper.updatedAt)}</p>
          ${wallpaper.rejectionReason ? `<p class="mt-2 rounded-md border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400">${escapeHtml(wallpaper.rejectionReason)}</p>` : ''}
        </div>
        <div class="flex shrink-0 flex-wrap gap-2">
          <a href="/wallpaper/${encodeURIComponent(wallpaper.id)}" class="secondary-button">Detail</a>
          <button type="button" class="secondary-button" data-delete-wallpaper="${escapeHtml(wallpaper.id)}">Delete</button>
        </div>
      </div>
    </article>
  `).join('');
}

export function renderDashboardPage() {
  return `
    <section class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 class="text-3xl font-bold text-accent-heading">My Uploads</h1>
          <p class="mt-2 text-sm text-body-muted">Pantau status moderasi wallpaper yang sudah kamu kirim.</p>
        </div>
        <a href="/upload" class="primary-button">Upload Wallpaper</a>
      </div>

      <div id="dashboard-summary" class="grid gap-4 sm:grid-cols-3">
        <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
        <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
        <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
      </div>

      <div class="panel-card">
        <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 class="text-xl font-bold text-accent-heading">Daftar wallpaper</h2>
          <button id="refresh-wallpapers" type="button" class="secondary-button">Refresh</button>
        </div>
        <div id="wallpaper-list" class="dashboard-list-scroll app-scrollbar space-y-3">
          <div class="rounded-md border border-scapes-light-accent p-4 text-sm text-body-muted dark:border-scapes-dark-accent">Memuat data...</div>
        </div>
      </div>

      <div id="delete-modal" class="fixed inset-0 z-40 hidden items-center justify-center bg-black/50 p-4 dark:bg-black/70">
        <div class="w-full max-w-sm animate-scale-in rounded-lg border border-scapes-light-accent bg-white p-5 shadow-lg dark:border-scapes-dark-accent dark:bg-gray-900">
          <h2 class="text-lg font-bold text-accent-heading">Hapus wallpaper?</h2>
          <p class="mt-2 text-sm text-body-muted">Wallpaper akan dihapus dari dashboard contributor.</p>
          <div class="mt-5 flex justify-end gap-3">
            <button id="cancel-delete" type="button" class="secondary-button">Batal</button>
            <button id="confirm-delete" type="button" class="primary-button bg-red-600 hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600">Hapus</button>
          </div>
        </div>
      </div>
    </section>
  `;
}

export async function initDashboardPage() {
  const modal = document.getElementById('delete-modal');
  const list = document.getElementById('wallpaper-list');
  let deleteTarget = null;

  const refreshDashboard = async () => {
    try {
      const wallpapers = await listContributorWallpapers(wallpaperRepository);
      renderSummary(wallpapers);
      renderList(wallpapers);
    } catch (error) {
      renderSummary([]);
      list.innerHTML = `
        <div class="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-400">
          ${escapeHtml(error.message || 'Gagal memuat dashboard contributor.')}
        </div>
      `;
    }
  };

  await refreshDashboard();

  document.getElementById('refresh-wallpapers')?.addEventListener('click', refreshDashboard);

  document.getElementById('wallpaper-list')?.addEventListener('click', (event) => {
    const button = event.target.closest('[data-delete-wallpaper]');
    if (!button) return;

    deleteTarget = button.dataset.deleteWallpaper;
    modal.classList.remove('hidden');
    modal.classList.add('flex');
  });

  document.getElementById('cancel-delete')?.addEventListener('click', () => {
    deleteTarget = null;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  });

  document.getElementById('confirm-delete')?.addEventListener('click', async () => {
    if (!deleteTarget) return;

    try {
      await deleteWallpaper(wallpaperRepository, deleteTarget);
      renderToast('Wallpaper dihapus dari dashboard.', 'success');
      deleteTarget = null;
      modal.classList.add('hidden');
      modal.classList.remove('flex');
      await refreshDashboard();
    } catch (error) {
      renderToast(error.message || 'Gagal menghapus wallpaper.', 'error');
    }
  });
}
