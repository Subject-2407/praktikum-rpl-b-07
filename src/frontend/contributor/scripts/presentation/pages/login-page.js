import { authRepository } from '../../data/repositories/auth-repository.js';
import { loginContributor } from '../../domain/use-cases/login-contributor.js';
import { registerContributor } from '../../domain/use-cases/register-contributor.js';
import { renderThemeToggle } from '../components/theme-toggle.js';

const INVALID_FIELD_CLASSES = [
  'border-red-400',
  'ring-2',
  'ring-red-200',
  'dark:border-red-500',
  'dark:ring-red-900/40',
];

const REGISTER_STEP_NAME = 1;
const REGISTER_STEP_ACCOUNT = 2;
const PANEL_TRANSITION_DURATION_MS = 260;
const STEP_TRANSITION_DURATION_MS = 220;
const MOTION_CLASSES = [
  'auth-panel-enter',
  'auth-panel-exit',
  'auth-step-enter',
  'auth-step-exit',
  'auth-success-pop',
];
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const registerStepCopy = {
  [REGISTER_STEP_NAME]: {
    badge: 'Step 1 of 2',
    title: 'Create your contributor profile',
    description: 'Start with the name people will see in your workspace.',
  },
  [REGISTER_STEP_ACCOUNT]: {
    badge: 'Step 2 of 2',
    title: 'Lock in your account details',
    description: 'Add your email and password to finish setting up access.',
  },
};

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

function setFieldInvalidState(field, isInvalid) {
  if (!field) return;

  field.setAttribute('aria-invalid', String(isInvalid));
  INVALID_FIELD_CLASSES.forEach((className) => {
    field.classList.toggle(className, isInvalid);
  });
}

function getRegisterFields(registerForm) {
  return {
    displayName: registerForm.querySelector('#register-display-name'),
    email: registerForm.querySelector('#register-email'),
    password: registerForm.querySelector('#register-password'),
    confirmPassword: registerForm.querySelector('#register-confirm'),
    tos: registerForm.querySelector('#register-tos'),
  };
}

function getCurrentRegisterStep(registerForm) {
  return Number(registerForm?.dataset.step || REGISTER_STEP_NAME);
}

function waitForMotion(duration) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, duration);
  });
}

function clearMotionClasses(element) {
  if (!element) return;
  MOTION_CLASSES.forEach((className) => element.classList.remove(className));
}

async function animateElementIn(element, className, duration) {
  if (!element) return;

  clearMotionClasses(element);
  element.classList.remove('hidden');

  window.requestAnimationFrame(() => {
    element.classList.add(className);
  });

  await waitForMotion(duration);
  clearMotionClasses(element);
}

async function animateElementOut(element, className, duration) {
  if (!element || element.classList.contains('hidden')) return;

  clearMotionClasses(element);
  element.classList.add(className);

  await waitForMotion(duration);
  element.classList.add('hidden');
  clearMotionClasses(element);
}

function focusActiveRegisterField(registerForm) {
  const fields = getRegisterFields(registerForm);
  const fieldToFocus = getCurrentRegisterStep(registerForm) === REGISTER_STEP_NAME
    ? fields.displayName
    : fields.email;

  fieldToFocus?.focus();
}

function setRegisterStep(registerForm, step) {
  if (!registerForm) return;

  const stepCopy = registerStepCopy[step] || registerStepCopy[REGISTER_STEP_NAME];
  const stepBadge = registerForm.querySelector('#register-step-badge');
  const stepTitle = registerForm.querySelector('#register-step-title');
  const stepDescription = registerForm.querySelector('#register-step-description');
  const successPanel = registerForm.querySelector('#register-success');

  registerForm.dataset.step = String(step);
  registerForm.dataset.state = 'editing';

  stepBadge.textContent = stepCopy.badge;
  stepTitle.textContent = stepCopy.title;
  stepDescription.textContent = stepCopy.description;

  successPanel?.classList.add('hidden');

  registerForm
    .querySelectorAll('[data-register-step-panel]')
    .forEach((panel) => {
      const panelStep = Number(panel.getAttribute('data-register-step-panel'));
      panel.classList.toggle('hidden', panelStep !== step);
    });

  registerForm
    .querySelectorAll('[data-register-step-indicator]')
    .forEach((indicator) => {
      const indicatorStep = Number(indicator.getAttribute('data-register-step-indicator'));
      const isActive = indicatorStep <= step;
      indicator.className = [
        'h-1.5',
        'flex-1',
        'rounded-full',
        'transition-colors',
        'duration-300',
        isActive
          ? 'bg-scapes-light-primary dark:bg-scapes-dark-primary'
          : 'bg-black/10 dark:bg-white/12',
      ].join(' ');
    });

  const preview = registerForm.querySelector('#register-display-preview');
  if (preview) {
    preview.textContent = registerForm.querySelector('#register-display-name')?.value.trim() || 'Your display name';
  }
}

function getVisibleRegisterStage(registerForm) {
  const successPanel = registerForm?.querySelector('#register-success');
  if (successPanel && !successPanel.classList.contains('hidden')) {
    return successPanel;
  }

  return Array.from(registerForm?.querySelectorAll('[data-register-step-panel]') || [])
    .find((panel) => !panel.classList.contains('hidden'));
}

async function transitionRegisterStep(registerForm, step) {
  if (!registerForm) return;

  const currentStage = getVisibleRegisterStage(registerForm);
  const nextStage = registerForm.querySelector(`[data-register-step-panel="${step}"]`);

  if (!currentStage || currentStage === nextStage) {
    setRegisterStep(registerForm, step);
    return;
  }

  await animateElementOut(currentStage, 'auth-step-exit', STEP_TRANSITION_DURATION_MS);
  setRegisterStep(registerForm, step);
  await animateElementIn(nextStage, 'auth-step-enter', STEP_TRANSITION_DURATION_MS);
}

function showRegisterSuccess(registerForm, message, email) {
  if (!registerForm) return;

  const stepBadge = registerForm.querySelector('#register-step-badge');
  const stepTitle = registerForm.querySelector('#register-step-title');
  const stepDescription = registerForm.querySelector('#register-step-description');
  const successPanel = registerForm.querySelector('#register-success');
  const successEmail = registerForm.querySelector('#register-success-email');

  registerForm.dataset.state = 'success';
  registerForm.dataset.step = String(REGISTER_STEP_ACCOUNT);

  stepBadge.textContent = 'Ready to verify';
  stepTitle.textContent = 'Check your inbox';
  stepDescription.textContent = 'We sent a verification link so you can activate your contributor account.';

  registerForm
    .querySelectorAll('[data-register-step-panel]')
    .forEach((panel) => panel.classList.add('hidden'));

  registerForm
    .querySelectorAll('[data-register-step-indicator]')
    .forEach((indicator) => {
      indicator.className = 'h-1.5 flex-1 rounded-full bg-green-500 transition-colors duration-300 dark:bg-green-400';
    });

  if (successEmail) {
    successEmail.textContent = email;
  }

  if (successPanel) {
    successPanel.classList.remove('hidden');
    setMessage(successPanel.querySelector('#register-success-message'), message, 'success');
  }
}

async function transitionRegisterSuccess(registerForm, message, email) {
  const currentStage = getVisibleRegisterStage(registerForm);

  if (currentStage) {
    await animateElementOut(currentStage, 'auth-step-exit', STEP_TRANSITION_DURATION_MS);
  }

  showRegisterSuccess(registerForm, message, email);
  const successPanel = registerForm.querySelector('#register-success');
  if (successPanel) {
    await animateElementIn(successPanel, 'auth-success-pop', PANEL_TRANSITION_DURATION_MS);
  }
}

function highlightRegisterError(registerForm, errorMessage) {
  const normalizedMessage = String(errorMessage || '').toLowerCase();
  const fields = getRegisterFields(registerForm);

  Object.values(fields).forEach((field) => {
    if (field instanceof HTMLElement && field.tagName === 'INPUT') {
      setFieldInvalidState(field, false);
    }
  });

  if (normalizedMessage.includes('display name')) {
    setFieldInvalidState(fields.displayName, true);
    fields.displayName?.focus();
    return;
  }

  if (normalizedMessage.includes('email')) {
    setFieldInvalidState(fields.email, true);
    fields.email?.focus();
    return;
  }

  if (normalizedMessage.includes('confirm')
    || normalizedMessage.includes('confirmation')
    || normalizedMessage.includes('konfirmasi')) {
    setFieldInvalidState(fields.confirmPassword, true);
    fields.confirmPassword?.focus();
    return;
  }

  if (normalizedMessage.includes('password')) {
    setFieldInvalidState(fields.password, true);
    fields.password?.focus();
    return;
  }
}

function validateRegisterNameStep(registerForm, registerMessage) {
  const { displayName } = getRegisterFields(registerForm);
  const normalizedValue = String(displayName?.value || '').replace(/\s+/g, ' ').trim();

  if (!normalizedValue) {
    setFieldInvalidState(displayName, true);
    setMessage(registerMessage, 'Please choose your display name.');
    displayName?.focus();
    return null;
  }

  if (normalizedValue.length > 100) {
    setFieldInvalidState(displayName, true);
    setMessage(registerMessage, 'Display name must be 100 characters or less.');
    displayName?.focus();
    return null;
  }

  setFieldInvalidState(displayName, false);
  setMessage(registerMessage, '');
  displayName.value = normalizedValue;
  return normalizedValue;
}

function validateRegisterAccountStep(registerForm, registerMessage) {
  const fields = getRegisterFields(registerForm);
  const email = String(fields.email?.value || '').trim();
  const password = String(fields.password?.value || '');
  const confirmPassword = String(fields.confirmPassword?.value || '');

  Object.values(fields).forEach((field) => {
    if (field instanceof HTMLElement && field.tagName === 'INPUT') {
      setFieldInvalidState(field, false);
    }
  });

  if (!EMAIL_PATTERN.test(email)) {
    setFieldInvalidState(fields.email, true);
    setMessage(registerMessage, 'Enter a valid email address.');
    fields.email?.focus();
    return null;
  }

  if (password.length < 8) {
    setFieldInvalidState(fields.password, true);
    setMessage(registerMessage, 'Password must be at least 8 characters.');
    fields.password?.focus();
    return null;
  }

  if (!confirmPassword) {
    setFieldInvalidState(fields.confirmPassword, true);
    setMessage(registerMessage, 'Please confirm your password.');
    fields.confirmPassword?.focus();
    return null;
  }

  if (password !== confirmPassword) {
    setFieldInvalidState(fields.confirmPassword, true);
    setMessage(registerMessage, 'Password confirmation does not match.');
    fields.confirmPassword?.focus();
    return null;
  }

  setMessage(registerMessage, '');
  return {
    email,
    password,
    confirmPassword,
  };
}

async function closeRegisterForm(registerForm, registerToggleButton, loginPanel) {
  if (!registerForm || !registerToggleButton || !loginPanel) return;

  await animateElementOut(registerForm, 'auth-panel-exit', PANEL_TRANSITION_DURATION_MS);
  await animateElementIn(loginPanel, 'auth-panel-enter', PANEL_TRANSITION_DURATION_MS);
  registerToggleButton.setAttribute('aria-expanded', 'false');
}

async function openRegisterForm(registerForm, registerToggleButton, loginPanel) {
  if (!registerForm || !registerToggleButton || !loginPanel) return;

  await animateElementOut(loginPanel, 'auth-panel-exit', PANEL_TRANSITION_DURATION_MS);
  await animateElementIn(registerForm, 'auth-panel-enter', PANEL_TRANSITION_DURATION_MS);
  registerToggleButton.setAttribute('aria-expanded', 'true');
}

function resetRegisterForm(registerForm, registerMessage) {
  if (!registerForm) return;

  registerForm.reset();
  setMessage(registerMessage, '');
  Object.values(getRegisterFields(registerForm)).forEach((field) => {
    if (field instanceof HTMLInputElement && field.type !== 'checkbox') {
      setFieldInvalidState(field, false);
    }
  });
  setRegisterStep(registerForm, REGISTER_STEP_NAME);
}

export function renderLoginPage() {
  return `
    <main class="relative h-[100dvh] overflow-hidden bg-[#eef1ef] dark:bg-[#071012] lg:grid lg:grid-cols-[1.15fr_0.85fr]">
      ${renderThemeToggle()}
      <section class="relative hidden overflow-hidden bg-[radial-gradient(circle_at_top_left,_rgba(255,255,255,0.9),_rgba(255,255,255,0)_34%),radial-gradient(circle_at_82%_20%,_rgba(249,197,46,0.16),_transparent_30%),linear-gradient(160deg,_#d8e8df_0%,_#bfd6cb_44%,_#9ec0b2_100%)] px-14 py-12 dark:bg-[radial-gradient(circle_at_top_left,_rgba(43,180,193,0.18),_rgba(43,180,193,0)_36%),linear-gradient(160deg,_#102025_0%,_#0d171a_48%,_#081012_100%)] lg:flex lg:items-stretch lg:justify-center xl:px-20">
        <div class="absolute inset-x-0 top-0 h-32 bg-gradient-to-b from-white/55 to-transparent dark:from-white/5"></div>
        <div class="absolute -left-16 top-20 h-52 w-52 rounded-full bg-scapes-light-primary/18 blur-3xl dark:bg-scapes-dark-primary/15"></div>
        <div class="absolute right-6 top-10 h-40 w-40 rounded-full bg-scapes-light-highlight/14 blur-3xl dark:bg-transparent"></div>
        <div class="absolute -right-10 bottom-[-1rem] hidden h-64 w-64 rounded-full bg-scapes-dark-primary/20 blur-3xl dark:block"></div>
        <div class="relative flex h-full w-full max-w-[34rem] flex-col animate-fade-in">
          <div class="hero-copy">
            <p class="mb-6 text-xs font-semibold uppercase tracking-[0.34em] text-accent-heading">
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

      <section class="shell-panel-scroll app-scrollbar box-border h-[100dvh] bg-[linear-gradient(180deg,_#f9faf9_0%,_#f1f4f3_100%)] px-6 py-6 dark:bg-[linear-gradient(180deg,_#050708_0%,_#0b1214_100%)] sm:px-10 sm:py-6 lg:px-12 lg:py-6">
        <div class="flex min-h-full flex-col justify-center">
          <div class="relative mx-auto w-full max-w-[30rem] animate-scale-in">
            <div class="space-y-6 sm:space-y-8">
              <div class="flex justify-center">
                <a
                  href="/login"
                  class="inline-flex items-center justify-center bg-transparent px-2 py-1 shadow-none transition-transform duration-300 hover:scale-[1.02]"
                  aria-label="Scapes contributor login"
                >
                  <img src="/assets/scapes-light.png" alt="Scapes" class="h-14 w-auto dark:hidden sm:h-18">
                  <img src="/assets/scapes-dark.png" alt="Scapes" class="hidden h-14 w-auto dark:block sm:h-18">
                </a>
              </div>

              <div id="login-panel" class="space-y-6 sm:space-y-8">
                <div class="text-center">
                  <h1 class="sr-only">Log in contributor</h1>
                  <p class="mt-1 text-base text-gray-600 dark:text-gray-300">
                    Sign in to continue managing your contributor workspace.
                  </p>
                </div>
                <form id="login-form" class="space-y-4 sm:space-y-5">
                  <div id="login-error" class="hidden rounded-sm border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400" role="alert"></div>
                  <div>
                    <label for="login-email" class="sr-only">Email</label>
                    <input id="login-email" name="email" type="email" autocomplete="email" required class="auth-field" placeholder="Email">
                  </div>
                  <div>
                    <label for="login-password" class="sr-only">Password</label>
                    <input id="login-password" name="password" type="password" autocomplete="current-password" required class="auth-field" placeholder="Password">
                  </div>
                  <a href="/password-resets" class="inline-flex cursor-pointer text-left text-sm font-medium text-gray-700 transition-colors duration-300 hover:text-scapes-light-primary dark:text-gray-200 dark:hover:text-scapes-dark-primary">
                    Forgot your password?
                  </a>
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
              </div>

            <form
              id="register-form"
              class="hidden space-y-5"
              data-step="${REGISTER_STEP_NAME}"
              data-state="editing"
              novalidate
            >
              <div class="register-card relative overflow-hidden rounded-[2rem] border border-black/8 bg-white/90 p-5 shadow-[0_22px_70px_rgba(15,23,42,0.08)] backdrop-blur-sm transition-colors duration-300 dark:border-white/10 dark:bg-[#101719] dark:shadow-[0_24px_70px_rgba(0,0,0,0.34)] sm:p-6">
                <div class="absolute inset-x-0 top-0 h-24 bg-[radial-gradient(circle_at_top_left,_rgba(19,117,134,0.16),_transparent_60%),radial-gradient(circle_at_top_right,_rgba(249,197,46,0.16),_transparent_42%)] dark:bg-[radial-gradient(circle_at_top_left,_rgba(43,180,193,0.22),_transparent_58%),radial-gradient(circle_at_top_right,_rgba(255,193,7,0.15),_transparent_38%)]"></div>
                <div class="relative">
                  <div class="flex items-start justify-between gap-4">
                    <div class="max-w-sm">
                      <p id="register-step-badge" class="text-xs font-semibold uppercase tracking-[0.28em] text-accent-heading">
                        Step 1 of 2
                      </p>
                      <h2 id="register-step-title" class="mt-3 text-2xl font-bold tracking-[-0.03em] text-gray-950 dark:text-white">
                        Create your contributor profile
                      </h2>
                      <p id="register-step-description" class="mt-7 text-sm leading-6 text-gray-600 dark:text-gray-300">
                        Start with the name people will see in your workspace.
                      </p>
                    </div>
                  </div>

                  <div class="mt-5 flex gap-2 mb-5" aria-hidden="true">
                    <span data-register-step-indicator="1" class="h-1.5 flex-1 rounded-full bg-scapes-light-primary transition-colors duration-300 dark:bg-scapes-dark-primary"></span>
                    <span data-register-step-indicator="2" class="h-1.5 flex-1 rounded-full bg-black/10 transition-colors duration-300 dark:bg-white/12"></span>
                  </div>

                  <div id="register-message" class="hidden mt-5 rounded-sm border-l-4 p-3 text-sm" role="alert"></div>

                  <div data-register-step-panel="${REGISTER_STEP_NAME}" class="mt-6 space-y-5">
                    <div class="rounded-[1.75rem] border border-black/8 bg-[linear-gradient(135deg,_rgba(255,255,255,0.92),_rgba(236,244,241,0.92))] p-5 dark:border-white/10 dark:bg-[linear-gradient(145deg,_rgba(15,24,26,0.96),_rgba(11,17,19,0.96))]">
                      <p class="text-xs font-semibold uppercase tracking-[0.28em] text-body-muted">
                        Display name
                      </p>
                      <label for="register-display-name" class="mt-3 block text-xl font-bold tracking-[-0.03em] text-gray-950 dark:text-white">
                        What should we call you?
                      </label>
                      <input
                        id="register-display-name"
                        name="displayName"
                        type="text"
                        autocomplete="nickname"
                        class="auth-field mt-4"
                        placeholder="e.g. Atlas Studio"
                        maxlength="100"
                      >
                      <p class="mt-3 text-sm leading-6 text-gray-600 dark:text-gray-300">
                        This shows up in your contributor workspace and alongside wallpaper submissions.
                      </p>
                    </div>

                    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <p class="text-sm text-gray-500 dark:text-gray-400">
                        You can refine this later from your profile settings.
                      </p>
                      <button id="register-next" type="submit" class="inline-flex min-h-12 items-center justify-center rounded-full bg-scapes-light-primary px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-scapes-light-secondary dark:bg-scapes-dark-primary dark:text-gray-950 dark:hover:bg-[#56c6d1]">
                        Continue
                      </button>
                    </div>
                  </div>

                  <div data-register-step-panel="${REGISTER_STEP_ACCOUNT}" class="hidden mt-6 space-y-5">
                    <div class="flex flex-col gap-3 rounded-[1.5rem] border border-black/8 bg-black/[0.02] p-4 dark:border-white/10 dark:bg-white/[0.03] sm:flex-row sm:items-center">
                      <div>
                        <p class="text-xs font-semibold uppercase tracking-[0.22em] text-body-muted">
                          Creating account for
                        </p>
                        <p id="register-display-preview" class="mt-1 text-lg font-semibold text-gray-950 dark:text-white">
                          Your display name
                        </p>
                      </div>
                      <button id="register-back" type="button" class="secondary-button sm:ml-auto">
                        Back
                      </button>
                    </div>

                    <div class="grid gap-4 sm:grid-cols-2">
                      <div class="sm:col-span-2">
                        <label for="register-email" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Email</label>
                        <input id="register-email" name="email" type="email" autocomplete="email" class="field-control" placeholder="email@example.com">
                      </div>
                      <div>
                        <label for="register-password" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Password</label>
                        <input id="register-password" name="password" type="password" autocomplete="new-password" class="field-control" placeholder="Minimum 8 characters">
                      </div>
                      <div>
                        <label for="register-confirm" class="mb-1 block text-sm font-semibold text-gray-950 dark:text-white">Confirm password</label>
                        <input id="register-confirm" name="confirmPassword" type="password" autocomplete="new-password" class="field-control" placeholder="Retype your password">
                      </div>
                    </div>

                    <label class="flex items-start gap-3 cursor-pointer rounded-[1.25rem] border border-black/8 bg-black/[0.02] p-4 text-sm leading-6 text-gray-700 transition-colors duration-300 dark:border-white/10 dark:bg-white/[0.03] dark:text-gray-300">
                      <input id="register-tos" name="tos" type="checkbox" class="mt-1 h-4 w-4 accent-scapes-light-primary dark:accent-scapes-dark-primary cursor-pointer">
                      <span>
                        I certify that I am 13 or older and agree to the Terms of Service and Privacy Policy.
                      </span>
                    </label>

                    <button id="register-submit" type="submit" class="inline-flex min-h-12 w-full items-center justify-center rounded-full bg-gray-950 px-6 text-sm font-semibold text-white transition-colors duration-300 hover:bg-gray-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-white dark:text-gray-950 dark:hover:bg-gray-200">
                      Get started
                    </button>
                  </div>

                  <div id="register-success" class="hidden mt-6 rounded-[1.75rem] border border-green-500/35 bg-green-50/90 p-5 dark:bg-green-900/10">
                    <div class="flex items-start gap-3">
                      <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-green-500 text-white dark:bg-green-400 dark:text-gray-950">
                        <span class="text-lg font-bold" aria-hidden="true">✓</span>
                      </div>
                      <div class="min-w-0 flex-1">
                        <p class="text-sm font-semibold uppercase tracking-[0.24em] text-green-700 dark:text-green-300">
                          Verification sent
                        </p>
                        <p id="register-success-message" class="mt-2 text-sm leading-6 text-green-700 dark:text-green-200"></p>
                        <p class="mt-3 text-sm text-green-700/90 dark:text-green-200/90">
                          Registered email:
                          <span id="register-success-email" class="font-semibold"></span>
                        </p>
                        <button id="register-success-login" type="button" class="mt-4 inline-flex min-h-11 items-center justify-center rounded-full bg-green-600 px-5 text-sm font-semibold text-white transition-colors duration-300 hover:bg-green-700 dark:bg-green-400 dark:text-gray-950 dark:hover:bg-green-300">
                          Return to login
                        </button>
                      </div>
                    </div>
                  </div>

                  <div class="mt-5 text-center">
                    <p class="text-sm text-gray-600 dark:text-gray-300">
                      Already have an account?
                      <button
                        id="register-cancel"
                        type="button"
                        class="font-semibold text-gray-950 underline decoration-scapes-light-primary decoration-[0.12em] underline-offset-[0.18em] transition-colors duration-300 hover:text-scapes-light-primary dark:text-white dark:decoration-scapes-dark-primary dark:hover:text-scapes-dark-primary"
                      >
                        Log in
                      </button>
                    </p>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>
        </div>
      </section>
    </main>
  `;
}

export function initLoginPage({ navigate }) {
  const loginForm = document.getElementById('login-form');
  const loginPanel = document.getElementById('login-panel');
  const registerForm = document.getElementById('register-form');
  const loginError = document.getElementById('login-error');
  const registerMessage = document.getElementById('register-message');
  const registerToggleButton = document.getElementById('register-toggle');
  const loginEmailInput = document.getElementById('login-email');
  const registerFields = getRegisterFields(registerForm);

  registerToggleButton?.addEventListener('click', async () => {
    if (registerForm?.dataset.motionLock === 'true') return;

    registerForm.dataset.motionLock = 'true';

    if (registerForm?.dataset.state === 'success') {
      resetRegisterForm(registerForm, registerMessage);
    }

    await openRegisterForm(registerForm, registerToggleButton, loginPanel);
    registerForm?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    focusActiveRegisterField(registerForm);
    registerForm.dataset.motionLock = 'false';
  });

  registerForm?.querySelector('#register-back')?.addEventListener('click', async () => {
    if (registerForm?.dataset.motionLock === 'true') return;

    registerForm.dataset.motionLock = 'true';
    setMessage(registerMessage, '');
    await transitionRegisterStep(registerForm, REGISTER_STEP_NAME);
    focusActiveRegisterField(registerForm);
    registerForm.dataset.motionLock = 'false';
  });

  registerForm?.querySelector('#register-success-login')?.addEventListener('click', async () => {
    if (registerForm?.dataset.motionLock === 'true') return;

    registerForm.dataset.motionLock = 'true';
    resetRegisterForm(registerForm, registerMessage);
    await closeRegisterForm(registerForm, registerToggleButton, loginPanel);
    loginEmailInput?.focus();
    registerForm.dataset.motionLock = 'false';
  });

  registerForm?.querySelector('#register-cancel')?.addEventListener('click', async () => {
    if (registerForm?.dataset.motionLock === 'true') return;

    registerForm.dataset.motionLock = 'true';
    await closeRegisterForm(registerForm, registerToggleButton, loginPanel);
    loginEmailInput?.focus();
    registerForm.dataset.motionLock = 'false';
  });

  Object.values(registerFields).forEach((field) => {
    if (!field) return;

    const eventName = field.type === 'checkbox' ? 'change' : 'input';
    field.addEventListener(eventName, () => {
      if (field instanceof HTMLInputElement && field.type !== 'checkbox') {
        setFieldInvalidState(field, false);
      }

      if (registerForm?.dataset.state === 'success') {
        setRegisterStep(registerForm, getCurrentRegisterStep(registerForm));
      }

      if (field === registerFields.displayName && getCurrentRegisterStep(registerForm) === REGISTER_STEP_ACCOUNT) {
        setRegisterStep(registerForm, REGISTER_STEP_ACCOUNT);
      }

      setMessage(registerMessage, '');
    });
  });

  registerFields.confirmPassword?.addEventListener('input', () => {
    const password = String(registerFields.password?.value || '');
    const confirmation = String(registerFields.confirmPassword?.value || '');
    const isMismatch = confirmation.length > 0 && confirmation !== password;
    setFieldInvalidState(registerFields.confirmPassword, isMismatch);
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

    if (registerForm.dataset.motionLock === 'true') {
      return;
    }

    if (registerForm.dataset.state === 'success') {
      registerForm.dataset.motionLock = 'true';
      await closeRegisterForm(registerForm, registerToggleButton, loginPanel);
      loginEmailInput?.focus();
      registerForm.dataset.motionLock = 'false';
      return;
    }

    const currentStep = getCurrentRegisterStep(registerForm);
    const displayName = validateRegisterNameStep(registerForm, registerMessage);

    if (!displayName) {
      return;
    }

    if (currentStep === REGISTER_STEP_NAME) {
      registerForm.dataset.motionLock = 'true';
      await transitionRegisterStep(registerForm, REGISTER_STEP_ACCOUNT);
      focusActiveRegisterField(registerForm);
      registerForm.dataset.motionLock = 'false';
      return;
    }

    const accountInput = validateRegisterAccountStep(registerForm, registerMessage);
    if (!accountInput) {
      return;
    }

    if (!registerFields.tos?.checked) {
      setMessage(registerMessage, 'Please agree to the Terms of Service and Privacy Policy.');
      registerFields.tos?.focus();
      return;
    }

    const submitButton = document.getElementById('register-submit');
    const payload = {
      displayName,
      ...accountInput,
    };

    submitButton.disabled = true;
    submitButton.textContent = 'Creating account...';
    setMessage(registerMessage, '');

    try {
      const result = await registerContributor(authRepository, payload);
      loginEmailInput.value = payload.email;
      registerForm.reset();
      registerFields.displayName.value = displayName;
      registerForm.dataset.motionLock = 'true';
      await transitionRegisterSuccess(registerForm, result.message, payload.email);
      registerForm.dataset.motionLock = 'false';
    } catch (error) {
      highlightRegisterError(registerForm, error.message);
      setMessage(registerMessage, error.message);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Get started';
    }
  });

  setRegisterStep(registerForm, REGISTER_STEP_NAME);
}
