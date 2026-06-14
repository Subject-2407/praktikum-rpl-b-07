import { initRouter } from './presentation/router.js';

const THEME_KEY = 'scapes-theme-preference';
const LIGHT_THEME = 'light';
const DARK_THEME = 'dark';

function getStoredTheme() {
  const savedTheme = localStorage.getItem(THEME_KEY);
  return savedTheme === LIGHT_THEME || savedTheme === DARK_THEME ? savedTheme : null;
}

function getPreferredTheme() {
  return getStoredTheme()
    || (window.matchMedia('(prefers-color-scheme: dark)').matches ? DARK_THEME : LIGHT_THEME);
}

function updateThemeToggleUi(theme) {
  const isDark = theme === DARK_THEME;
  const nextLabel = isDark ? 'Switch to light mode' : 'Switch to dark mode';

  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.setAttribute('aria-label', nextLabel);
    button.setAttribute('title', nextLabel);
  });

  document.querySelectorAll('[data-sun-icon]').forEach((icon) => {
    icon.classList.toggle('hidden', !isDark);
  });
  document.querySelectorAll('[data-moon-icon]').forEach((icon) => {
    icon.classList.toggle('hidden', isDark);
  });
}

function applyTheme(theme, { persist = true } = {}) {
  document.documentElement.classList.toggle('dark', theme === DARK_THEME);

  if (persist) {
    localStorage.setItem(THEME_KEY, theme);
  }

  updateThemeToggleUi(theme);
  window.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
}

function animateThemeToggle() {
  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.classList.remove('theme-toggle-spin');
    void button.offsetWidth;
    button.classList.add('theme-toggle-spin');
  });
}

function toggleTheme() {
  const nextTheme = document.documentElement.classList.contains('dark')
    ? LIGHT_THEME
    : DARK_THEME;

  animateThemeToggle();
  applyTheme(nextTheme);
}

function initTheme() {
  applyTheme(getPreferredTheme(), { persist: Boolean(getStoredTheme()) });

  document.addEventListener('click', (event) => {
    if (!event.target.closest('[data-theme-toggle]')) return;
    toggleTheme();
  });

  const observer = new MutationObserver(() => {
    if (document.querySelector('[data-theme-toggle]')) {
      updateThemeToggleUi(document.documentElement.classList.contains('dark') ? DARK_THEME : LIGHT_THEME);
    }
  });
  
  observer.observe(document.body, { childList: true, subtree: true });

  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  mediaQuery.addEventListener('change', (event) => {
    if (getStoredTheme()) return;
    applyTheme(event.matches ? DARK_THEME : LIGHT_THEME, { persist: false });
  });
}

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initRouter();
});
