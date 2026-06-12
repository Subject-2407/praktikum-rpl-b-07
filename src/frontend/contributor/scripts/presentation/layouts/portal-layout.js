import { escapeHtml } from '../../core/utils/escape-html.js';
import { renderBrandLogo } from '../components/brand-logo.js';
import { renderThemeToggle } from '../components/theme-toggle.js';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: 'fa-solid fa-table-columns' },
  { href: '/upload', label: 'Upload', icon: 'fa-solid fa-cloud-arrow-up' },
  { href: '/insight', label: 'Insight', icon: 'fa-solid fa-chart-line' },
  { href: '/profile', label: 'Profile', icon: 'fa-solid fa-user-gear' },
];

export function renderAppShell(content, activePath = '/dashboard', user = {}) {
  const initials = (user.name || user.email || 'CO')
    .split(/[ @._-]/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();

  return `
    <div class="flex h-[100dvh] flex-col overflow-hidden bg-scapes-light-base dark:bg-scapes-dark-base lg:flex-row">
      <aside class="flex shrink-0 flex-col border-b border-scapes-light-accent bg-scapes-light-base/95 px-4 py-3 backdrop-blur dark:border-scapes-dark-accent dark:bg-scapes-dark-base/95 sm:px-6 lg:h-full lg:w-64 lg:border-b-0 lg:border-r lg:px-5 lg:py-5" style="z-index: 60;">
        <div class="flex items-center justify-between gap-4 lg:flex-col lg:items-stretch lg:justify-start lg:gap-6">
          ${renderBrandLogo({
            href: '/dashboard',
            containerClass: 'flex min-w-0 items-center gap-2 text-accent-heading',
            imageClass: 'h-10 w-auto',
            title: '',
            subtitle: 'Contributor',
            subtitleClass: 'text-s text-accent-heading',
          })}

          <div class="flex items-center gap-2 lg:hidden">
            <button data-logout-button class="secondary-button" type="button">Logout</button>
            ${renderThemeToggle('relative h-10 w-10 shrink-0')}
          </div>
        </div>

        <nav class="shell-nav-scroll app-scrollbar mt-3 flex gap-2 border-t border-scapes-light-accent pt-3 dark:border-scapes-dark-accent lg:mt-6 lg:flex-1 lg:flex-col lg:border-t-0 lg:pt-0" aria-label="Contributor navigation">
          ${navItems.map((item) => `
            <a
              href="${item.href}"
              class="flex min-h-10 items-center justify-center gap-3 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors duration-300 lg:justify-start ${activePath === item.href ? 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-gray-950' : 'text-body-muted hover:bg-white dark:hover:bg-gray-900'}"
            >
              <i class="${item.icon} w-5 shrink-0 text-center leading-none" aria-hidden="true"></i>
              <span>${item.label}</span>
            </a>
          `).join('')}
        </nav>

        <div class="mt-6 hidden border-t border-scapes-light-accent pt-4 dark:border-scapes-dark-accent lg:block">
          <div class="flex items-center gap-3">
            <span class="flex h-10 w-10 items-center justify-center rounded-full bg-scapes-light-accent text-sm font-bold text-accent-heading dark:bg-scapes-dark-accent">${escapeHtml(initials || 'CO')}</span>
            <span class="min-w-0 truncate text-sm text-body-muted">${escapeHtml(user.name || user.email || 'Contributor')}</span>
          </div>
          <div class="mt-4 flex items-center gap-2">
            <button data-logout-button class="secondary-button flex-1" type="button">Logout</button>
            ${renderThemeToggle('relative h-10 w-10 shrink-0')}
          </div>
        </div>
      </aside>

      <div class="flex min-w-0 flex-1 overflow-hidden">
        <main class="shell-main app-scrollbar animate-fade-in pb-10">
          ${content}
        </main>
      </div>
    </div>
  `;
}
