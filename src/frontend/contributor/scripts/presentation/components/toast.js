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

  window.setTimeout(() => toast.remove(), 3800);
}
