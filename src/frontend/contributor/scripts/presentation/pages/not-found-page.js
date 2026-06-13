export function renderNotFoundPage() {
  return `
    <main class="flex min-h-screen items-center justify-center bg-scapes-light-base px-4 dark:bg-scapes-dark-base">
      <section class="panel-card max-w-md text-center">
        <h1 class="text-3xl font-bold text-accent-heading">404</h1>
        <p class="mt-3 text-sm text-body-muted">Halaman yang kamu cari tidak tersedia.</p>
        <a href="/dashboard" class="primary-button mt-5">Ke Dashboard</a>
      </section>
    </main>
  `;
}
