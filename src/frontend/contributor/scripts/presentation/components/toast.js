const MAX_VISIBLE_TOASTS = 3;
const TOAST_LIFETIME_MS = 3800;

export function renderToast(message, type = 'success') {
  const palette = {
    success: 'border-green-500 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400',
    warning: 'border-yellow-500 bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
    error: 'border-red-500 bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400',
  };

  const toastRoot = document.getElementById('toast-root');
  if (!toastRoot) return;

  const toast = document.createElement('div');
  toast.className = `animate-scale-in rounded-md border-l-4 p-4 text-sm font-medium shadow-lg ${palette[type] || palette.success}`;
  toast.textContent = message;
  toastRoot.appendChild(toast);

  while (toastRoot.childElementCount > MAX_VISIBLE_TOASTS) {
    const oldestToast = toastRoot.firstElementChild;
    if (!oldestToast) break;

    const timeoutId = Number(oldestToast.dataset.timeoutId);
    if (!Number.isNaN(timeoutId)) {
      window.clearTimeout(timeoutId);
    }

    oldestToast.remove();
  }

  const timeoutId = window.setTimeout(() => {
    toast.remove();
  }, TOAST_LIFETIME_MS);

  toast.dataset.timeoutId = String(timeoutId);
}
