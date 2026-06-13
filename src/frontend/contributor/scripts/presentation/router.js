import { authRepository } from '../data/repositories/auth-repository.js';
import { wallpaperRepository } from '../data/repositories/wallpaper-repository.js';
import { getContributorWallpaper } from '../domain/use-cases/get-contributor-wallpaper.js';
import { logoutContributor } from '../domain/use-cases/logout-contributor.js';
import { renderAppShell } from './layouts/portal-layout.js';
import { renderToast } from './components/toast.js';
import { initDashboardPage, renderDashboardPage } from './pages/dashboard-page.js';
import {
  initEmailVerificationPage,
  renderEmailVerificationPage,
} from './pages/email-verification-page.js';
import { renderInsightPage } from './pages/insight-page.js';
import { initLoginPage, renderLoginPage } from './pages/login-page.js';
import { renderNotFoundPage } from './pages/not-found-page.js';
import {
  initPasswordResetPage,
  renderPasswordResetPage,
} from './pages/password-reset-page.js';
import { initSettingsPage, renderSettingsPage } from './pages/settings-page.js';
import { initUploadPage, renderUploadPage } from './pages/upload-page.js';
import { renderWallpaperDetailPage } from './pages/wallpaper-detail-page.js';

const pageTitles = {
  '/email-verifications': 'Scapes - Verify Email',
  '/login': 'Scapes - Login Contributor',
  '/password-resets': 'Scapes - Reset Password',
  '/dashboard': 'Scapes - Dashboard Contributor',
  '/upload': 'Scapes - Upload Wallpaper',
  '/insight': 'Scapes - Contributor Insight',
  '/profile': 'Scapes - Profile Contributor',
};

let appContainer = null;

function cleanPath(path) {
  return !path || path === '/' ? '/' : path.replace(/\/$/, '') || '/';
}

function setTitle(path) {
  document.title = pageTitles[path] || 'Scapes - Contributor Portal';
}

async function resolveSessionUser() {
  try {
    const session = await authRepository.getCurrentSession({
      suppressUnauthorizedEvent: true,
    });

    return {
      ...(session.user || {}),
      expiresAt: session.expiresAt || null,
    };
  } catch (error) {
    if (error?.status === 401 || error?.status === 403) {
      return null;
    }

    throw error;
  }
}

async function render(path) {
  const resolvedPath = cleanPath(path);

  if (resolvedPath === '/logout') {
    await logoutContributor(authRepository);
    renderToast('Session contributor berakhir.', 'success');
    navigate('/login', { replace: true });
    return;
  }

  if (resolvedPath === '/') {
    try {
      const sessionUser = await resolveSessionUser();
      navigate(sessionUser ? '/dashboard' : '/login', { replace: true });
    } catch (error) {
      renderToast(error.message || 'Gagal mengecek sesi aktif.', 'error');
      navigate('/login', { replace: true });
    }
    return;
  }

  if (resolvedPath === '/login') {
    try {
      const sessionUser = await resolveSessionUser();
      if (sessionUser) {
        navigate('/dashboard', { replace: true });
        return;
      }
    } catch (error) {
      renderToast(error.message || 'Gagal mengecek sesi aktif.', 'error');
    }

    setTitle(resolvedPath);
    appContainer.innerHTML = renderLoginPage();
    initLoginPage({ navigate });
    return;
  }

  if (resolvedPath === '/email-verifications') {
    const token = new URLSearchParams(window.location.search).get('token') || '';
    setTitle(resolvedPath);
    appContainer.innerHTML = renderEmailVerificationPage({ token });
    initEmailVerificationPage({ navigate, token });
    return;
  }

  if (resolvedPath === '/password-resets') {
    setTitle(resolvedPath);
    appContainer.innerHTML = renderPasswordResetPage({ token: '' });
    initPasswordResetPage({ navigate, token: '' });
    return;
  }

  const passwordResetMatch = resolvedPath.match(/^\/password-resets\/([^/]+)$/);
  if (passwordResetMatch) {
    const token = decodeURIComponent(passwordResetMatch[1]);
    setTitle('/password-resets');
    appContainer.innerHTML = renderPasswordResetPage({ token });
    initPasswordResetPage({ navigate, token });
    return;
  }

  let sessionUser = null;
  try {
    sessionUser = await resolveSessionUser();
  } catch (error) {
    renderToast(error.message || 'Gagal mengecek sesi aktif.', 'error');
    appContainer.innerHTML = renderNotFoundPage();
    return;
  }

  if (!sessionUser) {
    navigate('/login', { replace: true });
    return;
  }

  setTitle(resolvedPath);

  if (resolvedPath === '/dashboard') {
    appContainer.innerHTML = renderAppShell(renderDashboardPage(), resolvedPath, sessionUser);
    await initDashboardPage({ navigate });
    return;
  }

  if (resolvedPath === '/upload') {
    appContainer.innerHTML = renderAppShell(renderUploadPage(), resolvedPath, sessionUser);
    await initUploadPage({ navigate });
    return;
  }

  if (resolvedPath === '/insight') {
    appContainer.innerHTML = renderAppShell(renderInsightPage(), resolvedPath, sessionUser);
    return;
  }

  if (resolvedPath === '/profile') {
    appContainer.innerHTML = renderAppShell(renderSettingsPage(sessionUser), resolvedPath, sessionUser);
    initSettingsPage({ navigate });
    return;
  }

  const wallpaperMatch = resolvedPath.match(/^\/wallpaper\/([^/]+)$/);
  if (wallpaperMatch) {
    try {
      const wallpaper = await getContributorWallpaper(
        wallpaperRepository,
        decodeURIComponent(wallpaperMatch[1]),
      );
      appContainer.innerHTML = renderAppShell(
        renderWallpaperDetailPage(wallpaper),
        '/dashboard',
        sessionUser,
      );
    } catch (error) {
      renderToast(error.message || 'Gagal memuat detail wallpaper.', 'error');
      appContainer.innerHTML = renderAppShell(
        renderWallpaperDetailPage(null),
        '/dashboard',
        sessionUser,
      );
    }
    return;
  }

  appContainer.innerHTML = renderNotFoundPage();
}

export function navigate(path, options = {}) {
  const resolvedPath = cleanPath(path);

  if (options.replace) {
    window.history.replaceState({ path: resolvedPath }, '', resolvedPath);
  } else {
    window.history.pushState({ path: resolvedPath }, '', resolvedPath);
  }

  render(resolvedPath);
}

export function initRouter() {
  appContainer = document.getElementById('app');

  window.addEventListener('scapes:unauthorized', () => {
    if (window.location.pathname !== '/login') {
      renderToast('Sesi login berakhir. Silakan masuk kembali.', 'warning');
      navigate('/login', { replace: true });
    }
  });

  document.addEventListener('click', async (event) => {
    const logoutButton = event.target.closest('#logout-button');
    if (logoutButton) {
      await logoutContributor(authRepository);
      renderToast('Session contributor berakhir.', 'success');
      navigate('/login', { replace: true });
      return;
    }

    const link = event.target.closest('a[href^="/"]');
    if (!link || link.target || event.defaultPrevented) return;

    event.preventDefault();
    navigate(link.getAttribute('href'));
  });

  window.addEventListener('popstate', () => render(window.location.pathname));
  render(window.location.pathname);
}
