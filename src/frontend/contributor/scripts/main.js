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
  const button = document.getElementById('themeToggle');
  const sunIcon = document.getElementById('sunIcon');
  const moonIcon = document.getElementById('moonIcon');
  const isDark = theme === DARK_THEME;
  const nextLabel = isDark ? 'Switch to light mode' : 'Switch to dark mode';

  if (button) {
    button.setAttribute('aria-label', nextLabel);
    button.setAttribute('title', nextLabel);
  }

  sunIcon?.classList.toggle('hidden', !isDark);
  moonIcon?.classList.toggle('hidden', isDark);
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
  const button = document.getElementById('themeToggle');
  if (!button) return;

  button.classList.remove('theme-toggle-spin');
  void button.offsetWidth;
  button.classList.add('theme-toggle-spin');
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
    if (!event.target.closest('#themeToggle')) return;
    toggleTheme();
  });

  const observer = new MutationObserver(() => {
    if (document.getElementById('themeToggle')) {
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
