import { authRepository } from '../../data/repositories/auth-repository.js';
import { loginContributor } from '../../domain/use-cases/login-contributor.js';
import { registerContributor } from '../../domain/use-cases/register-contributor.js';

function renderHeroCollage() {
  const tiles = [
    'collage-tile collage-tile-1 collage-palette-a',
    'collage-tile collage-tile-2 collage-palette-b',
    'collage-tile collage-tile-3 collage-palette-c',
    'collage-tile collage-tile-4 collage-palette-d',
    'collage-tile collage-tile-5 collage-palette-e',
    'collage-tile collage-tile-6 collage-palette-f',
    'collage-tile collage-tile-7 collage-palette-g',
    'collage-tile collage-tile-8 collage-palette-h',
    'collage-tile collage-tile-9 collage-palette-i',
    'collage-tile collage-tile-10 collage-palette-j',
  ];

  return `
    <div class="hero-collage mt-10" aria-hidden="true">
      ${tiles.map((tileClass) => `
        <div class="${tileClass}">
          <div class="collage-image"></div>
          <div class="collage-meta">
            <span class="collage-line collage-line-lg"></span>
            <span class="collage-line collage-line-sm"></span>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

function setMessage(element, message, type = 'error') {
  if (!element) return;

  const palette = type === 'success'
    ? 'border-green-500 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
    : 'border-red-500 bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400';

  element.className = `rounded-sm border-l-4 p-3 text-sm ${palette}`;
  element.textContent = message;
  element.classList.toggle('hidden', !message);
}

export function renderLoginPage() {
  return `
    <main class="min-h-screen bg-[#eef1ef] dark:bg-[#071012] lg:grid lg:h-screen lg:grid-cols-[1.15fr_0.85fr] lg:overflow-hidden">
      <section class="relative hidden overflow-hidden bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.9),_rgba(255,255,255,0)_34%),radial-gradient(circle_at_82%_20%,_rgba(249,197,46,0.16),_transparent_30%),linear-gradient(160deg,_#d8e8df_0%,_#bfd6cb_44%,_#9ec0b2_100%)] px-14 py-12 dark:bg-[radial-gradient(circle_at_top_left,_rgba(43,180,193,0.18),_rgba(43,180,193,0)_36%),linear-gradient(160deg,_#102025_0%,_#0d171a_48%,_#081012_100%)] lg:flex lg:items-stretch lg:justify-center xl:px-20">
        <div class="absolute inset-x-0 top-0 h-32 bg-gradient-to-b from-white/55 to-transparent dark:from-white/5"></div>
        <div class="absolute -left-16 top-20 h-52 w-52 rounded-full bg-scapes-light-primary/18 blur-3xl dark:bg-scapes-dark-primary/15"></div>
        <div class="absolute right-6 top-10 h-40 w-40 rounded-full bg-scapes-light-highlight/14 blur-3xl dark:bg-transparent"></div>
        <div class="absolute -right-10 bottom-[-1rem] hidden h-64 w-64 rounded-full bg-scapes-dark-primary/20 blur-3xl dark:block"></div>
        <div class="relative flex h-full w-full max-w-[34rem] flex-col animate-fade-in">
          <div class="hero-copy">
            <p class="mb-6 text-xs font-semibold uppercase tracking-[0.34em] text-scapes-light-primary dark:text-scapes-dark-secondary">
              Contributor portal
            </p>
            <h1 class="text-[2.5rem] font-bold leading-[1.02] tracking-[-0.04em] text-gray-900 dark:text-white xl:text-[3.35rem]">
              Share your walls with the world.
              <br>
              Build with a community of
              <span class="underline decoration-scapes-light-primary decoration-[0.16em] underline-offset-[0.15em] dark:decoration-scapes-dark-primary">
                Scapes
              </span>
              creators.
            </h1>
  
          </div>
          ${renderHeroCollage()}
        </div>
      </section>

      <section class="flex min-h-screen items-center justify-center bg-[linear-gradient(180deg,_#f9faf9_0%,_#f1f4f3_100%)] px-6 py-8 dark:bg-[linear-gradient(180deg,_#050708_0%,_#0b1214_100%)] sm:px-10 lg:min-h-0 lg:overflow-y-auto lg:px-12 lg:py-6">
        <div class="w-full max-w-[29rem] animate-scale-in">
          <div class="mb-8 lg:hidden">
            <p class="max-w-sm text-2xl font-bold leading-tight tracking-[-0.03em] text-gray-950 dark:text-white">
              Share your walls with the world.
              <span class="block text-scapes-light-primary dark:text-scapes-dark-primary">Join Scapes.</span>
            </p>
          </div>

          <div class="space-y-8">
            <div class="flex justify-center">
              <a
                href="/login"
                class="inline-flex items-center justify-center bg-transparent px-2 py-1 shadow-none transition-transform duration-300 hover:scale-[1.02]"
                aria-label="Scapes contributor login"
              >
                <img src="/assets/scapes-light.png" alt="Scapes" class="h-16 w-auto dark:hidden sm:h-18">
                <img src="/assets/scapes-dark.png" alt="Scapes" class="hidden h-16 w-auto dark:block sm:h-18">
              </a>
            </div>

            <div class="text-center">
              <h1 class="sr-only">Log in contributor</h1>
              <p class="mt-1 text-base text-gray-600 dark:text-gray-300">
                Sign in to continue managing your contributor workspace.
              </p>
            </div>

            <form id="login-form" class="space-y-5">
              <div id="login-error" class="hidden rounded-sm border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400" role="alert"></div>
              <div>
                <label for="login-email" class="sr-only">Email</label>
                <input id="login-email" name="email" type="email" autocomplete="email" required class="auth-field" placeholder="Email">
              </div>
              <div>
                <label for="login-password" class="sr-only">Password</label>
                <input id="login-password" name="password" type="password" autocomplete="current-password" required class="auth-field" placeholder="Password">
              </div>
              <button type="button" class="cursor-pointer text-left text-sm font-medium text-gray-700 transition-colors duration-300 hover:text-scapes-light-primary dark:text-gray-200 dark:hover:text-scapes-dark-primary">
                Forgot your password?
              </button>
              <div class="pt-1 text-center">
                <button id="login-submit" type="submit" class="inline-flex min-h-14 min-w-[13rem] cursor-pointer items-center justify-center rounded-full bg-scapes-light-primary px-8 text-lg font-semibold text-white transition-colors duration-300 hover:bg-scapes-light-secondary disabled:cursor-not-allowed disabled:opacity-65 dark:bg-scapes-dark-primary dark:text-gray-950 dark:hover:bg-[#56c6d1]">
                  Log in
                </button>
              </div>
            </form>

            <div class="text-center">
              <p class="text-base text-gray-700 dark:text-gray-300">
                New here?
                <button
                  id="register-toggle"
                  type="button"
                  class="cursor-pointer font-semibold text-gray-950 underline decoration-scapes-light-primary decoration-[0.12em] underline-offset-[0.18em] transition-colors duration-300 hover:text-scapes-light-primary dark:text-white dark:decoration-scapes-dark-primary dark:hover:text-scapes-dark-primary"
                  aria-expanded="false"
                  aria-controls="register-form"
                >
                  Create an account
                </button>
              </p>
            </div>

            <form id="register-form" class="hidden space-y-4 border-t border-black/10 pt-6 dark:border-white/10">
              <div>
                <h2 class="text-xl font-bold text-gray-950 dark:text-white">Create account</h2>
                <p class="mt-2 text-sm text-gray-700 dark:text-gray-300">Buat akun contributor baru lalu verifikasi email sebelum login.</p>
              </div>
              <div id="register-message" class="hidden rounded-sm border-l-4 p-3 text-sm" role="alert"></div>
              <div>
                <label for="register-email" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Email</label>
                <input id="register-email" name="email" type="email" required class="field-control" placeholder="email@example.com">
              </div>
              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label for="register-password" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Password</label>
                  <input id="register-password" name="password" type="password" required class="field-control">
                </div>
                <div>
                  <label for="register-confirm" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Konfirmasi</label>
                  <input id="register-confirm" name="confirmPassword" type="password" required class="field-control">
                </div>
              </div>
              <label class="flex items-start gap-3 text-sm text-gray-700 dark:text-gray-300">
                <input id="register-tos" type="checkbox" required class="mt-1 h-4 w-4 accent-scapes-light-primary dark:accent-scapes-dark-primary">
                <span>Saya menyetujui Terms of Service dan Privacy Policy Scapes.</span>
              </label>
              <button id="register-submit" type="submit" class="secondary-button w-full">Create account</button>
            </form>
          </div>
        </div>
      </section>
    </main>
  `;
}

export function initLoginPage({ navigate }) {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const loginError = document.getElementById('login-error');
  const registerMessage = document.getElementById('register-message');
  const registerToggleButton = document.getElementById('register-toggle');

  registerToggleButton?.addEventListener('click', () => {
    const willOpen = registerForm?.classList.contains('hidden');
    registerForm?.classList.toggle('hidden', !willOpen);
    registerToggleButton.setAttribute('aria-expanded', String(willOpen));
    registerToggleButton.textContent = willOpen ? 'Hide form' : 'Create an account';

    if (willOpen) {
      document.getElementById('register-email')?.focus();
    }
  });

  loginForm?.addEventListener('submit', async (event) => {
    event.preventDefault();

    const submitButton = document.getElementById('login-submit');
    const formData = new FormData(loginForm);
    submitButton.disabled = true;
    submitButton.textContent = 'Logging in...';
    setMessage(loginError, '');

    try {
      await loginContributor(authRepository, {
        email: String(formData.get('email') || '').trim(),
        password: String(formData.get('password') || ''),
      });
      navigate('/dashboard', { replace: true });
    } catch (error) {
      setMessage(loginError, error.message);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Log in';
    }
  });

  registerForm?.addEventListener('submit', async (event) => {
    event.preventDefault();

    const submitButton = document.getElementById('register-submit');
    const formData = new FormData(registerForm);
    submitButton.disabled = true;
    submitButton.textContent = 'Creating account...';
    setMessage(registerMessage, '');

    try {
      const result = await registerContributor(authRepository, {
        email: String(formData.get('email') || '').trim(),
        password: String(formData.get('password') || ''),
        confirmPassword: String(formData.get('confirmPassword') || ''),
      });
      registerForm.reset();
      setMessage(registerMessage, result.message, 'success');
    } catch (error) {
      setMessage(registerMessage, error.message);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Create account';
    }
  });
}
