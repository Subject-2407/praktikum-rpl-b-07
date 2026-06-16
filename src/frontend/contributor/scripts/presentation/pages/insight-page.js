export function renderInsightPage() {
  return `
    <section class="min-h-full px-3 py-4 sm:px-6 sm:py-6 lg:px-8">
      <div class="mx-auto max-w-5xl space-y-5">
        <div class="rounded-2xl border border-scapes-light-accent bg-white p-5 shadow-sm dark:border-scapes-dark-accent dark:bg-gray-900 sm:p-6">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h1 class="text-3xl font-bold text-accent-heading">Insight</h1>
              <p class="mt-2 text-sm text-body-muted">
                This page is still in progress. A simple analytics overview will be available soon.
              </p>
            </div>
          </div>
        </div>

        <div class="rounded-2xl border border-scapes-light-accent bg-white p-4 shadow-sm dark:border-scapes-dark-accent dark:bg-gray-900 sm:p-6">
          <div class="pointer-events-none select-none space-y-4 blur-sm">
            <div class="grid gap-4 md:grid-cols-3">
              ${['Total Views', 'Approval Rate', 'Review Time'].map((label, index) => `
                <article class="rounded-xl border border-scapes-light-accent bg-scapes-light-base p-4 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
                  <p class="text-sm text-body-muted">${label}</p>
                  <p class="mt-3 text-2xl font-bold text-body-strong">${['18.4K', '91%', '14h'][index]}</p>
                </article>
              `).join('')}
            </div>

            <article class="rounded-xl border border-scapes-light-accent bg-scapes-light-base p-5 dark:border-scapes-dark-accent dark:bg-scapes-dark-base">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h2 class="text-lg font-bold text-body-strong">Weekly Trend</h2>
                  <p class="mt-1 text-sm text-body-muted">Preview of contributor performance analytics.</p>
                </div>
                <span class="text-xs font-semibold uppercase tracking-[0.18em] text-body-muted">Mock</span>
              </div>

              <div class="mt-5 grid h-32 grid-cols-6 items-end gap-3" aria-label="Mock insight chart">
                ${['35%', '56%', '48%', '72%', '64%', '82%'].map((height) => `
                  <div class="rounded-t-lg bg-scapes-light-accent dark:bg-scapes-dark-accent" style="height: ${height};"></div>
                `).join('')}
              </div>
            </article>
          </div>
        </div>
      </div>
    </section>
  `;
}
