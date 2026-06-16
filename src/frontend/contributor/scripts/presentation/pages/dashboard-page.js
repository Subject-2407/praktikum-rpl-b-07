import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatDate } from '../../core/utils/formatters.js';
import { wallpaperRepository } from '../../data/repositories/wallpaper-repository.js';
import { deleteWallpaper } from '../../domain/use-cases/delete-wallpaper.js';
import { listContributorWallpaperPage } from '../../domain/use-cases/list-contributor-wallpapers.js';
import { renderStatusBadge } from '../components/status-badge.js';
import { renderToast } from '../components/toast.js';

const statusFilters = [
  { value: '', label: 'All', icon: 'fa-solid fa-layer-group' },
  { value: 'pending', label: 'Pending', icon: 'fa-solid fa-hourglass-half' },
  { value: 'approved', label: 'Approved', icon: 'fa-solid fa-circle-check' },
  { value: 'rejected', label: 'Rejected', icon: 'fa-solid fa-circle-xmark' },
];

const summaryItems = [
  { value: '', label: 'Total', icon: 'fa-solid fa-layer-group' },
  { value: 'pending', label: 'Pending', icon: 'fa-solid fa-hourglass-half' },
  { value: 'approved', label: 'Approved', icon: 'fa-solid fa-circle-check' },
  { value: 'rejected', label: 'Rejected', icon: 'fa-solid fa-circle-xmark' },
];

const summaryCardPalettes = {
  total: {
    cardClass:
      'border-sky-300 bg-sky-50 dark:border-sky-400/30 dark:bg-sky-900/10',
    iconWrapClass:
      'bg-sky-500 text-white dark:bg-sky-400 dark:text-gray-950',
    labelClass: 'text-sky-800 dark:text-sky-300',
    countClass: 'text-sky-950 dark:text-sky-100',
    activeClass: 'ring-2 ring-sky-500 dark:ring-sky-300',
  },
  pending: {
    cardClass:
      'border-yellow-300 bg-yellow-50 dark:border-yellow-400/30 dark:bg-yellow-900/10',
    iconWrapClass:
      'bg-yellow-400 text-white dark:bg-yellow-400 dark:text-gray-950',
    labelClass: 'text-yellow-800 dark:text-yellow-300',
    countClass: 'text-yellow-950 dark:text-yellow-100',
    activeClass: 'ring-2 ring-yellow-400 dark:ring-yellow-300',
  },
  approved: {
    cardClass:
      'border-green-300 bg-green-50 dark:border-green-400/30 dark:bg-green-900/10',
    iconWrapClass:
      'bg-green-500 text-white dark:bg-green-400 dark:text-gray-950',
    labelClass: 'text-green-800 dark:text-green-300',
    countClass: 'text-green-950 dark:text-green-100',
    activeClass: 'ring-2 ring-green-500 dark:ring-green-300',
  },
  rejected: {
    cardClass:
      'border-red-300 bg-red-50 dark:border-red-400/30 dark:bg-red-900/10',
    iconWrapClass:
      'bg-red-500 text-white dark:bg-red-400 dark:text-gray-950',
    labelClass: 'text-red-800 dark:text-red-300',
    countClass: 'text-red-950 dark:text-red-100',
    activeClass: 'ring-2 ring-red-500 dark:ring-red-300',
  },
};

const dashboardMotds = [
  "Here's what's happening with your wallpapers today.",
  'Your latest uploads are moving through moderation.',
  'A quick snapshot of how your submissions are performing.',
  'Keep an eye on approvals, rejections, and pending work.',
  'Every upload tells a story. Here is the latest chapter.',
];

const state = {
  mode: 'masonry',
  status: '',
  page: 1,
  perPage: 20,
  items: [],
  meta: null,
  isLoading: false,
  hasMore: true,
  summary: {
    pending: null,
    approved: null,
    rejected: null,
  },
  observer: null,
};

function getFirstName(user = {}) {
  const displayName = String(user.display_name || user.displayName || user.name || '').trim();

  if (!displayName) {
    return 'Contributor';
  }

  return displayName.split(/\s+/)[0];
}

function getDashboardMotd(user = {}) {
  if (!dashboardMotds.length) {
    return '';
  }

  const seed = String(user.display_name || user.displayName || user.name || '')
    .split('')
    .reduce((total, character) => total + character.charCodeAt(0), 0);

  return dashboardMotds[seed % dashboardMotds.length];
}

function getStatusLabel(value) {
  return statusFilters.find((filter) => filter.value === value)?.label || 'All';
}

function getImageUrl(wallpaper) {
  return wallpaper.thumbnailUrl || wallpaper.previewUrl || '';
}

function getAspectRatioValue(wallpaper) {
  if (wallpaper.width > 0 && wallpaper.height > 0) {
    return wallpaper.width / wallpaper.height;
  }

  const device = String(wallpaper.targetDevice || '').toLowerCase();
  if (device === 'mobile') return 9 / 16;
  if (device === 'tablet') return 4 / 3;
  if (device === 'desktop') return 16 / 10;
  return 1;
}

function getAspectRatioStyle(wallpaper) {
  return `--wallpaper-ratio: ${getAspectRatioValue(wallpaper)}; aspect-ratio: var(--wallpaper-ratio);`;
}

function setActiveControls() {
  document.querySelectorAll('[data-view-mode]').forEach((button) => {
    const isActive = button.dataset.viewMode === state.mode;
    button.classList.toggle('bg-scapes-light-primary', isActive);
    button.classList.toggle('text-white', isActive);
    button.classList.toggle('dark:bg-scapes-dark-primary', isActive);
    button.classList.toggle('dark:text-gray-950', isActive);
  });

  document.querySelectorAll('[data-status-filter]').forEach((button) => {
    if (button.dataset.statusCard === 'true') return;

    const isActive = button.dataset.statusFilter === state.status;
    button.classList.toggle('ring-2', isActive);
    button.classList.toggle('ring-scapes-light-primary', isActive);
    button.classList.toggle('dark:ring-scapes-dark-primary', isActive);
  });

  const perPageSelect = document.getElementById('wallpaper-per-page');
  if (perPageSelect) {
    perPageSelect.disabled = state.mode !== 'list';
    perPageSelect.classList.toggle('hidden', state.mode !== 'list');
  }
}

function renderSummary() {
  const summary = document.getElementById('dashboard-summary');
  if (!summary) return;

  const hasPendingCount = state.summary.pending !== null;
  const hasApprovedCount = state.summary.approved !== null;
  const hasRejectedCount = state.summary.rejected !== null;
  const totalCount = hasPendingCount && hasApprovedCount && hasRejectedCount
    ? state.summary.pending + state.summary.approved + state.summary.rejected
    : null;

  const statusEntries = summaryItems.map((filter) => {
      const paletteKey = filter.value || 'total';
      const palette = summaryCardPalettes[paletteKey];
      const count = filter.value ? state.summary[filter.value] : totalCount;
      const isActive = state.status === filter.value;

      return {
        ...filter,
        count,
        isActive,
        palette,
      };
    });

  const statusBar = `
    <div class="flex min-h-10 items-center overflow-hidden rounded-lg border border-scapes-light-accent bg-white text-body-strong shadow-sm dark:border-scapes-dark-accent dark:bg-gray-900 xl:hidden">
      ${statusEntries.map((entry, index) => `
        <button
          type="button"
          data-status-filter="${entry.value}"
          data-status-card="true"
          class="inline-flex min-w-0 flex-1 items-center justify-center gap-1 px-1.5 py-2 text-[0.68rem] font-semibold transition-colors duration-300 hover:bg-gray-100 dark:hover:bg-gray-800 sm:gap-1.5 sm:px-3 sm:text-xs ${entry.isActive ? 'bg-scapes-light-primary/10 text-scapes-light-primary dark:bg-scapes-dark-primary/10 dark:text-scapes-dark-primary' : ''}"
          aria-label="${entry.label}: ${entry.count === null ? 'loading' : entry.count}"
          aria-pressed="${entry.isActive ? 'true' : 'false'}"
          title="${entry.label}: ${entry.count === null ? '...' : entry.count}"
        >
          <i class="${entry.icon} shrink-0 ${entry.palette.labelClass}" aria-hidden="true"></i>
          <span class="dashboard-status-label min-w-0 truncate">${entry.label}</span>
          <span class="shrink-0">${entry.count === null ? '...' : entry.count}</span>
        </button>
        ${index < statusEntries.length - 1 ? '<span class="h-5 w-px shrink-0 bg-scapes-light-accent/60 dark:bg-scapes-dark-accent/70" aria-hidden="true"></span>' : ''}
      `).join('')}
    </div>
  `;

  const desktopCards = `
    <div class="hidden gap-3 xl:grid xl:grid-cols-4">
      ${statusEntries.map((entry) => `
        <button
          type="button"
          data-status-filter="${entry.value}"
          data-status-card="true"
          class="rounded-lg border p-3 text-left transition duration-300 hover:-translate-y-0.5 ${entry.palette.cardClass} ${entry.isActive ? entry.palette.activeClass : ''}"
          aria-pressed="${entry.isActive ? 'true' : 'false'}"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="truncate text-xs font-semibold ${entry.palette.labelClass}">${entry.label}</p>
              <p class="mt-1.5 text-2xl font-bold ${entry.palette.countClass}">${entry.count === null ? '...' : entry.count}</p>
            </div>
            <div class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${entry.palette.iconWrapClass}">
              <i class="${entry.icon} text-base" aria-hidden="true"></i>
            </div>
          </div>
        </button>
      `).join('')}
    </div>
  `;

  summary.innerHTML = `${statusBar}${desktopCards}`;
}

function renderStatusTabs() {
  return statusFilters.map((filter) => `
    <button
      type="button"
      data-status-filter="${filter.value}"
      class="inline-flex h-6 shrink-0 items-center gap-1.5 rounded-full border border-scapes-light-accent bg-white px-2.5 text-[0.7rem] font-semibold text-body-strong transition-colors duration-300 hover:bg-gray-100 dark:border-scapes-dark-accent dark:bg-gray-900 dark:hover:bg-gray-800 sm:h-9 sm:gap-2 sm:px-3 sm:text-xs"
      aria-pressed="${state.status === filter.value ? 'true' : 'false'}"
    >
      <i class="${filter.icon}" aria-hidden="true"></i>
      <span>${filter.label}</span>
    </button>
  `).join('');
}

function renderMasonryCard(wallpaper) {
  const imageUrl = getImageUrl(wallpaper);

  return `
    <article class="mb-4 break-inside-avoid overflow-hidden rounded-lg shadow-sm transition duration-300 hover:-translate-y-0.5 hover:shadow-md">
      <a href="/wallpaper/${encodeURIComponent(wallpaper.id)}" class="group block">
        <div class="relative bg-scapes-light-base dark:bg-scapes-dark-base" style="${getAspectRatioStyle(wallpaper)}">
          ${imageUrl ? `
            <img
              data-masonry-image
              src="${escapeHtml(imageUrl)}"
              alt="${escapeHtml(wallpaper.title)}"
              class="h-full w-full object-cover transition duration-300 group-hover:scale-[1.02]"
              loading="lazy"
              decoding="async"
            >
          ` : `
            <div class="flex h-full items-center justify-center text-sm text-body-muted">No thumbnail</div>
          `}
          <div class="absolute left-3 top-3">${renderStatusBadge(wallpaper.status, { forceLightPalette: true })}</div>
          <div class="absolute inset-x-0 bottom-0 p-3 text-white drop-shadow-[0_2px_5px_rgba(0,0,0,0.85)]">
            <h3 class="truncate font-heading text-sm font-bold">${escapeHtml(wallpaper.title)}</h3>
            <p class="mt-1 truncate text-xs font-medium">${escapeHtml(wallpaper.category || 'Uncategorized')}</p>
          </div>
        </div>
      </a>
    </article>
  `;
}

function renderListCard(wallpaper) {
  const imageUrl = getImageUrl(wallpaper);

  return `
    <article
      class="cursor-pointer rounded-lg bg-white p-3 transition-colors duration-300 hover:bg-gray-100 dark:bg-gray-900 dark:hover:bg-gray-800 sm:p-4"
      data-wallpaper-link="/wallpaper/${encodeURIComponent(wallpaper.id)}"
      tabindex="0"
      role="link"
    >
      <div class="grid grid-cols-[5.75rem_minmax(0,1fr)] gap-3 sm:grid-cols-[8rem_1fr_auto] sm:items-center sm:gap-4">
        <div
          class="overflow-hidden rounded-md bg-scapes-light-base dark:bg-scapes-dark-base"
          style="aspect-ratio: 16 / 10;"
        >
          ${imageUrl ? `
            <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(wallpaper.title)}" class="h-full w-full object-cover" loading="lazy" decoding="async">
          ` : `
            <div class="flex h-full items-center justify-center text-xs text-body-muted">No thumbnail</div>
          `}
        </div>
        <div class="min-w-0">
          <div class="flex flex-wrap items-center gap-2">
            <h3 class="truncate font-heading text-base font-bold text-accent-heading sm:text-lg">${escapeHtml(wallpaper.title)}</h3>
            ${renderStatusBadge(wallpaper.status)}
          </div>
          <p class="mt-1 line-clamp-1 text-xs text-body-muted sm:line-clamp-2 sm:text-sm">${escapeHtml(wallpaper.description || 'No description yet.')}</p>
          <p class="mt-1 truncate text-[0.68rem] text-body-muted sm:mt-2 sm:text-xs">${escapeHtml(wallpaper.category || 'Uncategorized')} &bull; Updated ${formatDate(wallpaper.updatedAt)}</p>
          ${wallpaper.rejectionReason ? `<p class="mt-2 rounded-md border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400">${escapeHtml(wallpaper.rejectionReason)}</p>` : ''}
        </div>
        <div class="col-span-2 flex shrink-0 flex-wrap justify-end gap-2 sm:col-span-1 sm:justify-end">
          <button type="button" class="secondary-button min-h-8 px-3 py-1 text-xs sm:min-h-10 sm:px-4 sm:py-2 sm:text-sm" data-delete-wallpaper="${escapeHtml(wallpaper.id)}">Delete</button>
        </div>
      </div>
    </article>
  `;
}

function syncMasonryImageRatios() {
  document.querySelectorAll('[data-masonry-image]').forEach((image) => {
    const applyNaturalRatio = () => {
      if (image.naturalWidth <= 0 || image.naturalHeight <= 0) return;

      image.parentElement?.style.setProperty(
        '--wallpaper-ratio',
        String(image.naturalWidth / image.naturalHeight),
      );
    };

    if (image.complete) {
      applyNaturalRatio();
      return;
    }

    image.addEventListener('load', applyNaturalRatio, { once: true });
  });
}

function renderPagination() {
  const pagination = document.getElementById('wallpaper-pagination');
  if (!pagination) return;

  if (state.mode !== 'list') {
    pagination.innerHTML = '';
    pagination.classList.add('hidden');
    pagination.classList.remove('flex');
    return;
  }

  const currentPage = Number(state.meta?.current_page || state.page || 1);
  const lastPage = Number(state.meta?.last_page || 1);
  const total = Number(state.meta?.total || state.items.length || 0);

  pagination.classList.remove('hidden');
  pagination.classList.add('flex');
  pagination.innerHTML = `
    <p class="text-sm text-body-muted">Page ${currentPage} of ${lastPage} &bull; ${total} items</p>
    <div class="flex gap-2">
      <button type="button" id="prev-wallpaper-page" class="secondary-button" ${currentPage <= 1 ? 'disabled' : ''}>
        <i class="fa-solid fa-chevron-left" aria-hidden="true"></i>
      </button>
      <button type="button" id="next-wallpaper-page" class="secondary-button" ${currentPage >= lastPage ? 'disabled' : ''}>
        <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
      </button>
    </div>
  `;
}

function renderList() {
  const list = document.getElementById('wallpaper-list');
  if (!list) return;

  list.className = state.mode === 'masonry'
    ? 'dashboard-content-scroll app-scrollbar pr-1 sm:pr-2'
    : 'dashboard-content-scroll app-scrollbar space-y-2 pr-1 sm:pr-2';

  if (!state.items.length && state.isLoading) {
    list.innerHTML = `
      <div class="flex min-h-full items-center justify-center p-4 text-center text-sm text-body-muted">
        Loading wallpapers...
      </div>
    `;
    renderPagination();
    return;
  }

  if (!state.items.length) {
    const emptyMessage = state.status
      ? `No ${escapeHtml(getStatusLabel(state.status).toLowerCase())} wallpapers found.`
      : 'No wallpaper uploaded yet';

    list.innerHTML = `
      <div class="flex min-h-full items-center justify-center p-5 text-center text-sm text-body-muted">
        ${emptyMessage}
      </div>
    `;
    renderPagination();
    return;
  }

  const cards = state.mode === 'masonry'
    ? `
      <div style="column-width: clamp(9.75rem, 42vw, 18rem); column-gap: clamp(0.625rem, 2vw, 1rem);">
        ${state.items.map(renderMasonryCard).join('')}
      </div>
    `
    : state.items.map(renderListCard).join('');

  list.innerHTML = `
    ${cards}
    <div id="wallpaper-scroll-sentinel" class="${state.mode === 'masonry' ? 'h-8' : 'hidden'}"></div>
    ${state.mode === 'masonry' && state.isLoading ? `
      <div class="p-4 text-center text-sm text-body-muted">
        Loading more thumbnails...
      </div>
    ` : ''}
  `;

  renderPagination();
  syncMasonryImageRatios();
  observeMasonrySentinel();
}

function buildQuery({ force = false } = {}) {
  return {
    status: state.status,
    page: state.page,
    per_page: state.perPage,
    force,
  };
}

async function loadSummary(force = false) {
  try {
    const results = await Promise.all(['pending', 'approved', 'rejected'].map(async (status) => {
      const result = await listContributorWallpaperPage(wallpaperRepository, {
        status,
        page: 1,
        per_page: 1,
        force,
      });

      return [status, Number(result.meta?.total || result.items.length || 0)];
    }));

    results.forEach(([status, total]) => {
      state.summary[status] = total;
    });
    renderSummary();
    setActiveControls();
  } catch {
    state.summary.pending = 0;
    state.summary.approved = 0;
    state.summary.rejected = 0;
    renderSummary();
    setActiveControls();
  }
}

async function loadWallpapers({ append = false, force = false } = {}) {
  if (state.isLoading) return;

  state.isLoading = true;
  renderList();

  try {
    const result = await listContributorWallpaperPage(wallpaperRepository, buildQuery({ force }));
    state.meta = result.meta;
    state.items = append ? [...state.items, ...result.items] : result.items;

    const currentPage = Number(result.meta?.current_page || state.page || 1);
    const lastPage = Number(result.meta?.last_page || currentPage);
    state.hasMore = currentPage < lastPage;
  } catch (error) {
    state.items = append ? state.items : [];
    state.hasMore = false;
    renderToast(error.message || 'Failed to load the contributor dashboard.', 'error');
  } finally {
    state.isLoading = false;
    renderList();
    setActiveControls();
  }
}

function resetWallpapers() {
  state.page = 1;
  state.perPage = state.mode === 'masonry' ? 24 : Number(document.getElementById('wallpaper-per-page')?.value || 10);
  state.items = [];
  state.meta = null;
  state.hasMore = true;
}

async function applyStatusFilter(status) {
  state.status = state.status === status && status ? '' : status;
  resetWallpapers();
  renderSummary();
  setActiveControls();
  await loadWallpapers();
}

async function applyViewMode(mode) {
  if (state.mode === mode) return;

  state.mode = mode;
  resetWallpapers();
  setActiveControls();
  await loadWallpapers();
}

function observeMasonrySentinel() {
  if (state.observer) {
    state.observer.disconnect();
    state.observer = null;
  }

  if (state.mode !== 'masonry' || !state.hasMore || state.isLoading) return;

  const root = document.getElementById('wallpaper-list');
  const sentinel = document.getElementById('wallpaper-scroll-sentinel');
  if (!root || !sentinel) return;

  state.observer = new IntersectionObserver((entries) => {
    if (!entries.some((entry) => entry.isIntersecting) || state.isLoading || !state.hasMore) {
      return;
    }

    state.page += 1;
    loadWallpapers({ append: true });
  }, {
    root,
    rootMargin: '320px',
  });

  state.observer.observe(sentinel);
}

export function renderDashboardPage(user = {}) {
  const firstName = getFirstName(user);
  const motd = getDashboardMotd(user);

  return `
    <section id="dashboard-page" class="flex h-full min-h-0 flex-col overflow-hidden">
      <div class="sticky top-0 z-20 space-y-2 border-b border-scapes-light-accent bg-scapes-light-base/95 px-3 py-3 backdrop-blur dark:border-scapes-dark-accent dark:bg-scapes-dark-base/95 sm:space-y-4 sm:px-6 sm:py-5 lg:px-8">
        <div class="flex items-end justify-between gap-3">
          <div class="min-w-0">
            <h1 class="truncate text-lg font-bold text-accent-heading sm:text-2xl xl:text-3xl">Welcome back, ${escapeHtml(firstName)}.</h1>
            <p class="mt-0.5 truncate text-[0.7rem] text-body-muted sm:mt-2 sm:text-xs xl:text-sm">${escapeHtml(motd)}</p>
          </div>
          <a
            href="/upload"
            class="group inline-flex min-h-9 shrink-0 items-center justify-center gap-1.5 rounded-md border border-green-600/20 bg-gradient-to-r from-green-600 via-emerald-600 to-green-700 px-3 py-1.5 text-xs font-semibold text-white shadow-[0_10px_30px_rgba(22,163,74,0.28)] transition-all duration-300 hover:-translate-y-0.5 hover:from-green-500 hover:via-emerald-500 hover:to-green-600 hover:shadow-[0_10px_38px_rgba(22,163,74,0.36)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-green-400 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:border-green-400/30 dark:from-green-500 dark:via-emerald-500 dark:to-green-600 dark:text-gray-950 dark:shadow-[0_7px_34px_rgba(34,197,94,0.32)] dark:hover:from-green-400 dark:hover:via-emerald-400 dark:hover:to-green-500 dark:hover:text-gray-950 dark:focus-visible:ring-offset-gray-900 sm:min-h-10 sm:gap-2 sm:px-4 sm:py-2 sm:text-sm"
          >
            <i class="fa-solid fa-cloud-arrow-up text-base transition-transform duration-300" aria-hidden="true"></i>
            <span class="sm:hidden">Upload</span>
            <span class="hidden sm:inline">Upload Wallpaper</span>
          </a>
        </div>

        <div id="dashboard-summary">
          <div class="h-10 animate-pulse rounded-lg border border-scapes-light-accent bg-white dark:border-scapes-dark-accent dark:bg-gray-900 xl:hidden"></div>
          <div class="hidden gap-3 xl:grid xl:grid-cols-4">
            <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
            <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
            <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
            <div class="panel-card animate-pulse"><div class="h-12 rounded-md bg-gray-100 dark:bg-gray-800"></div></div>
          </div>
        </div>
      </div>

      <div class="flex min-h-0 flex-1 flex-col bg-white px-3 py-2 dark:bg-gray-900 sm:px-6 sm:py-4 lg:px-8">
        <div class="mb-2 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-5 sm:mb-4 sm:gap-3">
          <div class="app-scrollbar flex min-w-0 flex-nowrap pt-1 pl-1 gap-2 overflow-x-auto pb-3" aria-label="Status filter">
            ${renderStatusTabs()}
          </div>
          <div class="flex shrink-0 items-center gap-1.5 sm:gap-2 pb-2">
            <select id="wallpaper-per-page" class="field-control hidden h-5 w-auto min-w-20 shrink-0 px-2 py-0 text-[0.7rem] sm:h-9 sm:min-w-24 sm:text-xs" aria-label="Items per page">
              <option value="10">10 / page</option>
              <option value="20">20 / page</option>
              <option value="50">50 / page</option>
            </select>
            <div class="inline-flex h-6 items-center rounded-md border border-scapes-light-accent bg-white dark:border-scapes-dark-accent dark:bg-gray-900 sm:h-9">
              <button type="button" data-view-mode="masonry" class="inline-flex h-5 items-center justify-center rounded px-2 text-xs font-semibold text-body-strong transition-colors sm:h-8 sm:px-3 sm:text-sm" aria-label="Masonry view" title="Masonry view">
                <i class="fa-solid fa-grip" aria-hidden="true"></i>
              </button>
              <button type="button" data-view-mode="list" class="inline-flex h-5 items-center justify-center rounded px-2 text-xs font-semibold text-body-strong transition-colors sm:h-8 sm:px-3 sm:text-sm" aria-label="List view" title="List view">
                <i class="fa-solid fa-list" aria-hidden="true"></i>
              </button>
            </div>
            <button id="refresh-wallpapers" type="button" class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-md text-xs text-body-strong transition-colors duration-300 hover:bg-gray-100 dark:hover:bg-gray-800 sm:h-9 sm:w-9 sm:text-sm" aria-label="Refresh wallpapers" title="Refresh wallpapers">
              <i class="fa-solid fa-rotate-right" aria-hidden="true"></i>
            </button>
          </div>
        </div>

        <div id="wallpaper-list" class="dashboard-content-scroll app-scrollbar pr-1 sm:pr-2">
          <div class="rounded-md border border-scapes-light-accent p-4 text-sm text-body-muted dark:border-scapes-dark-accent">Loading data...</div>
        </div>

        <div id="wallpaper-pagination" class="hidden items-center justify-between gap-3 border-t border-scapes-light-accent pt-4 dark:border-scapes-dark-accent"></div>
      </div>

      <div id="delete-modal" class="fixed inset-0 z-40 hidden items-center justify-center bg-black/50 p-4 dark:bg-black/70">
        <div class="w-full max-w-sm animate-scale-in rounded-lg border border-scapes-light-accent bg-white p-5 shadow-lg dark:border-scapes-dark-accent dark:bg-gray-900">
          <h2 class="text-lg font-bold text-accent-heading">Delete wallpaper?</h2>
          <p class="mt-2 text-sm text-body-muted">This wallpaper will be removed from your contributor dashboard.</p>
          <div class="mt-5 flex justify-end gap-3">
            <button id="cancel-delete" type="button" class="secondary-button">Cancel</button>
            <button id="confirm-delete" type="button" class="primary-button bg-red-600 hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600">Delete</button>
          </div>
        </div>
      </div>
    </section>
  `;
}

export async function initDashboardPage({ navigate } = {}) {
  const dashboardRoot = document.getElementById('dashboard-page');
  const modal = document.getElementById('delete-modal');
  let deleteTarget = null;

  resetWallpapers();
  renderSummary();
  setActiveControls();
  await Promise.all([
    loadSummary(),
    loadWallpapers(),
  ]);

  document.getElementById('refresh-wallpapers')?.addEventListener('click', async () => {
    wallpaperRepository.invalidateContributorWallpapersCache?.();
    resetWallpapers();
    await Promise.all([
      loadSummary(true),
      loadWallpapers({ force: true }),
    ]);
  });

  document.getElementById('wallpaper-per-page')?.addEventListener('change', async () => {
    if (state.mode !== 'list') return;
    resetWallpapers();
    await loadWallpapers();
  });

  document.querySelectorAll('[data-view-mode]').forEach((button) => {
    button.addEventListener('click', () => applyViewMode(button.dataset.viewMode));
  });

  dashboardRoot?.addEventListener('click', async (event) => {
    const statusButton = event.target.closest('[data-status-filter]');
    if (statusButton && document.getElementById('dashboard-summary')?.contains(statusButton)
      || statusButton && statusButton.closest('[aria-label="Status filter"]')) {
      await applyStatusFilter(statusButton.dataset.statusFilter || '');
      return;
    }

    const previousButton = event.target.closest('#prev-wallpaper-page');
    if (previousButton) {
      state.page = Math.max(1, state.page - 1);
      await loadWallpapers();
      return;
    }

    const nextButton = event.target.closest('#next-wallpaper-page');
    if (nextButton) {
      state.page += 1;
      await loadWallpapers();
      return;
    }
  });

  document.getElementById('wallpaper-list')?.addEventListener('click', (event) => {
    const button = event.target.closest('[data-delete-wallpaper]');
    if (button) {
      deleteTarget = button.dataset.deleteWallpaper;
      modal.classList.remove('hidden');
      modal.classList.add('flex');
      return;
    }

    const row = event.target.closest('[data-wallpaper-link]');
    if (!row) return;

    const href = row.dataset.wallpaperLink;
    if (href && typeof navigate === 'function') {
      navigate(href);
    }
  });

  document.getElementById('wallpaper-list')?.addEventListener('keydown', (event) => {
    if (event.key !== 'Enter' && event.key !== ' ') return;

    const row = event.target.closest('[data-wallpaper-link]');
    if (!row) return;

    event.preventDefault();
    const href = row.dataset.wallpaperLink;
    if (href && typeof navigate === 'function') {
      navigate(href);
    }
  });

  document.getElementById('cancel-delete')?.addEventListener('click', () => {
    deleteTarget = null;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  });

  document.getElementById('confirm-delete')?.addEventListener('click', async () => {
    if (!deleteTarget) return;

    try {
      await deleteWallpaper(wallpaperRepository, deleteTarget);
      renderToast('Wallpaper removed from your dashboard.', 'success');
      deleteTarget = null;
      modal.classList.add('hidden');
      modal.classList.remove('flex');
      resetWallpapers();
      await Promise.all([
        loadSummary(true),
        loadWallpapers({ force: true }),
      ]);
    } catch (error) {
      renderToast(error.message || 'Failed to delete the wallpaper.', 'error');
    }
  });
}
