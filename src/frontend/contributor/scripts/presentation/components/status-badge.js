import { escapeHtml } from '../../core/utils/escape-html.js';

export function renderStatusBadge(status) {
  const normalized = String(status || 'Pending').toLowerCase();
  const palettes = {
    pending: 'border-yellow-500 bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
    approved: 'border-green-500 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400',
    rejected: 'border-red-500 bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400',
  };

  return `
    <span class="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${palettes[normalized] || palettes.pending}">
      ${escapeHtml(status || 'Pending')}
    </span>
  `;
}
