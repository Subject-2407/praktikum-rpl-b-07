import { escapeHtml } from '../../core/utils/escape-html.js';
import { renderBrandLogo } from '../components/brand-logo.js';
import { renderThemeToggle } from '../components/theme-toggle.js';

const navItems = [
  { href: '/dashboard', label: 'Dashboard' },
  { href: '/upload', label: 'Upload' },
  { href: '/insight', label: 'Insight' },
  { href: '/profile', label: 'Profile' },
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
    <div class="flex h-[100dvh] flex-col overflow-hidden bg-scapes-light-base dark:bg-scapes-dark-base">
      <header class="sticky top-0 border-b border-scapes-light-accent bg-scapes-light-base/95 px-4 py-3 backdrop-blur dark:border-scapes-dark-accent dark:bg-scapes-dark-base/95 sm:px-6" style="z-index: 60;">
        <div class="mx-auto flex max-w-7xl items-center justify-between gap-4">
          ${renderBrandLogo({
            href: '/dashboard',
            containerClass: 'flex min-w-0 items-center gap-3 text-scapes-light-primary dark:text-scapes-dark-primary',
            imageClass: 'h-10 w-auto',
            title: '',
            subtitle: 'Contributor Portal',
          })}

          <div class="flex items-center gap-2">
            <div class="hidden items-center gap-3 sm:flex">
              <span class="flex h-10 w-10 items-center justify-center rounded-full bg-scapes-light-accent text-sm font-bold text-scapes-light-primary dark:bg-scapes-dark-accent dark:text-scapes-dark-primary">${escapeHtml(initials || 'CO')}</span>
              <span class="text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">${escapeHtml(user.name || user.email || 'Contributor')}</span>
            </div>
            <button id="logout-button" class="secondary-button" type="button">Logout</button>
            <div class="flex items-center justify-center">
              ${renderThemeToggle('relative h-10 w-10 shrink-0')}
            </div>
          </div>
        </div>
      </header>

      <div class="mx-auto grid min-h-0 w-full max-w-7xl flex-1 gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[14rem_1fr]">
        <nav class="shell-nav-scroll app-scrollbar flex gap-2 border-b border-scapes-light-accent pb-3 dark:border-scapes-dark-accent lg:flex-col lg:border-b-0 lg:pb-0" aria-label="Contributor navigation">
          ${navItems.map((item) => `
            <a
              href="${item.href}"
              class="min-h-10 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors duration-300 ${activePath === item.href ? 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-gray-950' : 'text-scapes-light-secondary hover:bg-white dark:text-scapes-dark-secondary dark:hover:bg-gray-900'}"
            >${item.label}</a>
          `).join('')}
        </nav>

        <main class="shell-main app-scrollbar animate-fade-in pb-10">
          ${content}
        </main>
      </div>
    </div>
  `;
}
