import { renderBrandLogo } from '../components/brand-logo.js';

export function renderAuthSupportShell({
  eyebrow,
  title,
  description,
  accentClass = 'from-scapes-light-primary/20 via-scapes-light-highlight/12 to-transparent dark:from-scapes-dark-primary/20 dark:via-scapes-dark-secondary/14 dark:to-transparent',
  cardMarkup,
  sideNote = 'Contributor access',
}) {
  return `
    <main class="min-h-screen bg-[#edf2ef] text-gray-950 dark:bg-[#050a0d] dark:text-white lg:grid lg:min-h-screen lg:grid-cols-[1.05fr_0.95fr]">
      <section class="relative hidden overflow-hidden border-r border-black/5 bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.94),_rgba(255,255,255,0)_36%),linear-gradient(160deg,_#d5e8df_0%,_#bad6cb_43%,_#8db4aa_100%)] px-16 py-14 dark:border-white/6 dark:bg-[radial-gradient(circle_at_top_left,_rgba(43,180,193,0.16),_rgba(43,180,193,0)_30%),linear-gradient(160deg,_#0d181d_0%,_#081114_46%,_#04090b_100%)] lg:flex lg:flex-col lg:justify-between">
        <div class="absolute -left-20 top-20 h-64 w-64 rounded-full bg-scapes-light-primary/20 blur-3xl dark:bg-scapes-dark-primary/14"></div>
        <div class="absolute right-10 top-16 h-40 w-40 rounded-full bg-scapes-light-highlight/18 blur-3xl dark:bg-scapes-dark-secondary/16"></div>
        <div class="relative">
          ${renderBrandLogo({
            href: '/login',
            containerClass: 'inline-flex items-center gap-4',
            imageClass: 'h-14 w-auto',
            subtitle: sideNote,
            titleClass: 'text-2xl font-bold tracking-[-0.04em] text-gray-950 dark:text-white',
            subtitleClass: 'mt-1 text-xs font-semibold uppercase tracking-[0.28em] text-scapes-light-secondary dark:text-scapes-dark-secondary',
          })}
        </div>

        <div class="relative max-w-[33rem]">
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-scapes-light-primary dark:text-scapes-dark-secondary">
            ${eyebrow}
          </p>
          <h1 class="mt-6 text-[3.25rem] font-bold leading-[0.98] tracking-[-0.05em] text-gray-950 dark:text-white">
            ${title}
          </h1>
          <p class="mt-8 max-w-[29rem] text-lg leading-8 text-gray-700 dark:text-gray-300">
            ${description}
          </p>

          <div class="mt-12 grid gap-4 sm:grid-cols-2">
            <article class="rounded-[1.8rem] border border-white/45 bg-white/55 p-5 shadow-[0_22px_55px_rgba(15,23,42,0.08)] backdrop-blur dark:border-white/10 dark:bg-white/[0.04]">
              <p class="text-xs font-semibold uppercase tracking-[0.24em] text-scapes-light-secondary dark:text-scapes-dark-secondary">
                Token flow
              </p>
              <p class="mt-3 text-sm leading-6 text-gray-700 dark:text-gray-300">
                Secure action links route you back into the contributor portal, then the page completes the backend request using the provided token.
              </p>
            </article>
            <article class="rounded-[1.8rem] border border-white/45 bg-white/55 p-5 shadow-[0_22px_55px_rgba(15,23,42,0.08)] backdrop-blur dark:border-white/10 dark:bg-white/[0.04]">
              <p class="text-xs font-semibold uppercase tracking-[0.24em] text-scapes-light-secondary dark:text-scapes-dark-secondary">
                Account safety
              </p>
              <p class="mt-3 text-sm leading-6 text-gray-700 dark:text-gray-300">
                Password reset links expire after 24 hours, and verification tokens can only be used once.
              </p>
            </article>
          </div>
        </div>

        <div class="relative h-40 overflow-hidden rounded-[2rem] border border-white/45 bg-white/30 backdrop-blur dark:border-white/10 dark:bg-white/[0.03]">
          <div class="absolute inset-0 bg-gradient-to-br ${accentClass}"></div>
          <div class="absolute left-8 top-8 h-14 w-14 rounded-2xl border border-white/55 bg-white/60 dark:border-white/10 dark:bg-white/[0.05]"></div>
          <div class="absolute left-28 top-12 h-10 w-28 rounded-full border border-white/45 bg-white/50 dark:border-white/10 dark:bg-white/[0.04]"></div>
          <div class="absolute bottom-8 left-10 h-16 w-40 rounded-[1.5rem] border border-white/45 bg-black/[0.04] dark:border-white/10 dark:bg-white/[0.03]"></div>
          <div class="absolute bottom-10 right-10 h-20 w-24 rounded-[1.75rem] border border-white/45 bg-white/45 dark:border-white/10 dark:bg-white/[0.04]"></div>
        </div>
      </section>

      <section class="flex min-h-screen items-center justify-center bg-[linear-gradient(180deg,_#f9fbfa_0%,_#eef3f1_100%)] px-6 py-10 dark:bg-[linear-gradient(180deg,_#04080a_0%,_#091114_100%)] sm:px-10 lg:px-14">
        <div class="w-full max-w-[34rem]">
          <div class="mb-8 lg:hidden">
            ${renderBrandLogo({
              href: '/login',
              containerClass: 'inline-flex items-center gap-3',
              imageClass: 'h-12 w-auto',
              subtitle: sideNote,
              titleClass: 'text-xl font-bold tracking-[-0.04em] text-gray-950 dark:text-white',
              subtitleClass: 'text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-scapes-light-secondary dark:text-scapes-dark-secondary',
            })}
          </div>

          ${cardMarkup}
        </div>
      </section>
    </main>
  `;
}
