import { escapeHtml } from '../../core/utils/escape-html.js';
import { renderBrandLogo } from '../components/brand-logo.js';
import { renderThemeToggle } from '../components/theme-toggle.js';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: 'fa-solid fa-table-columns' },
  { href: '/upload', label: 'Upload', icon: 'fa-solid fa-cloud-arrow-up' },
  { href: '/insight', label: 'Insight', icon: 'fa-solid fa-chart-line' },
  { href: '/profile', label: 'Profile', icon: 'fa-solid fa-user-gear' },
];

function getUserDisplayLabel(user = {}) {
  return String(user.display_name || user.displayName || user.name || user.email || 'Contributor').trim() || 'Contributor';
}

function getUserInitials(user = {}) {
  return getUserDisplayLabel(user)
    .split(/[ @._-]/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase() || 'CO';
}

export function renderAppShell(content, activePath = '/dashboard', user = {}) {
  const initials = getUserInitials(user);
  const displayLabel = getUserDisplayLabel(user);

  return `
    <div class="flex h-[100dvh] flex-col overflow-hidden bg-scapes-light-base dark:bg-scapes-dark-base lg:flex-row">
      <aside class="fixed inset-x-3 bottom-3 z-[60] flex shrink-0 flex-col rounded-lg border border-scapes-light-accent bg-scapes-light-base/95 px-2 py-2 shadow-[0_14px_40px_rgba(15,23,42,0.18)] backdrop-blur dark:border-scapes-dark-accent dark:bg-scapes-dark-base/95 dark:shadow-[0_14px_40px_rgba(0,0,0,0.38)] lg:static lg:inset-auto lg:h-full lg:w-60 lg:rounded-none lg:border-y-0 lg:border-l-0 lg:border-r lg:px-4 lg:py-4 lg:shadow-none">
        <div class="hidden items-center justify-between gap-4 lg:flex lg:flex-col lg:items-stretch lg:justify-start lg:gap-5">
          ${renderBrandLogo({
            href: '/dashboard',
            containerClass: 'flex min-w-0 items-center gap-2 text-accent-heading',
            imageClass: 'h-10 w-auto',
            title: '',
            subtitle: 'Contributor',
            subtitleClass: 'text-s text-scapes-light-accent dark:text-scapes-dark-accent',
          })}

        </div>

        <div class="grid grid-cols-6 gap-1 lg:hidden">
          ${navItems.map((item) => `
            <a
              href="${item.href}"
              class="flex h-11 min-w-0 items-center justify-center rounded-md text-sm font-semibold transition-colors duration-300 ${activePath === item.href ? 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-gray-950' : 'text-body-muted hover:bg-white dark:hover:bg-gray-900'}"
              aria-label="${escapeHtml(item.label)}"
              title="${escapeHtml(item.label)}"
            >
              <i class="${item.icon} w-5 shrink-0 text-center text-lg leading-none" aria-hidden="true"></i>
            </a>
          `).join('')}
          ${renderThemeToggle('flex h-11 min-w-0', 'plain')}
          <button
            data-logout-button
            class="flex h-11 min-w-0 items-center justify-center rounded-md text-lg font-semibold text-body-muted transition-colors duration-300 hover:bg-white dark:hover:bg-gray-900"
            type="button"
            aria-label="Logout"
            title="Logout"
          >
            <i class="fa-solid fa-right-from-bracket w-5 shrink-0 text-center leading-none" aria-hidden="true"></i>
          </button>
        </div>

        <nav class="shell-nav-scroll app-scrollbar hidden lg:mt-5 lg:flex lg:flex-1 lg:flex-col lg:justify-start lg:gap-2" aria-label="Contributor navigation">
          ${navItems.map((item) => `
            <a
              href="${item.href}"
              class="flex min-h-10 w-full flex-none items-center justify-start gap-3 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors duration-300 ${activePath === item.href ? 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-gray-950' : 'text-body-muted hover:bg-white dark:hover:bg-gray-900'}"
              aria-label="${escapeHtml(item.label)}"
              title="${escapeHtml(item.label)}"
            >
              <i class="${item.icon} w-5 shrink-0 text-center leading-none" aria-hidden="true"></i>
              <span>${item.label}</span>
            </a>
          `).join('')}
        </nav>

        <div class="mt-5 hidden border-t border-scapes-light-accent pt-4 dark:border-scapes-dark-accent lg:block">
          <div class="flex items-center gap-3">
            <span class="flex h-10 w-10 items-center justify-center rounded-full bg-scapes-light-accent text-sm font-bold text-accent-heading dark:bg-scapes-dark-accent">${escapeHtml(initials || 'CO')}</span>
            <span class="min-w-0 truncate text-sm text-body-muted">${escapeHtml(displayLabel)}</span>
          </div>
          <div class="mt-4 flex items-center gap-2">
            <button data-logout-button class="secondary-button flex-1 gap-2 hover:border-red-600 hover:bg-red-600 hover:text-white dark:hover:border-red-600 dark:hover:bg-red-600 dark:hover:text-white" type="button">
              <i class="fa-solid fa-right-from-bracket text-sm leading-none" aria-hidden="true"></i>
              <span class="pb-0.5">Log out</span>
            </button>
            ${renderThemeToggle('relative flex h-10 w-10 shrink-0')}
          </div>
        </div>
      </aside>

      <div class="flex min-w-0 flex-1 overflow-hidden">
        <main class="shell-main app-scrollbar animate-fade-in pb-20 lg:pb-0">
          ${content}
        </main>
      </div>
    </div>
  `;
}
