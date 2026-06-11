export function renderInsightPage() {
  return `
    <section class="space-y-6">
      <div>
        <h1 class="text-3xl font-bold text-accent-heading">Insight</h1>
        <p class="mt-2 text-sm text-body-muted">Ringkasan performa upload contributor.</p>
      </div>
      <div class="grid gap-4 md:grid-cols-3">
        ${['Views bulan ini', 'Approval rate', 'Average review'].map((label, index) => `
          <article class="panel-card">
            <p class="text-sm text-body-muted">${label}</p>
            <p class="mt-3 text-3xl font-bold text-body-strong">${['2.4k', '86%', '18h'][index]}</p>
          </article>
        `).join('')}
      </div>
      <div class="panel-card">
        <h2 class="text-xl font-bold text-accent-heading">Moderation trend</h2>
        <div class="mt-5 grid h-48 grid-cols-6 items-end gap-3" aria-label="Moderation trend chart">
          ${['h-[42%]', 'h-[68%]', 'h-[55%]', 'h-[74%]', 'h-[63%]', 'h-[86%]'].map((heightClass) => `
            <div class="${heightClass} rounded-t-md bg-scapes-light-accent dark:bg-scapes-dark-accent"></div>
          `).join('')}
        </div>
      </div>
    </section>
  `;
}
