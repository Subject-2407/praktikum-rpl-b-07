import { escapeHtml } from '../../core/utils/escape-html.js';
import { wallpaperRepository } from '../../data/repositories/wallpaper-repository.js';
import { updateWallpaperMetadata } from '../../domain/use-cases/update-wallpaper-metadata.js';
import { renderStatusBadge } from '../components/status-badge.js';
import { renderToast } from '../components/toast.js';

function formatDetailDate(value) {
  if (!value) return '-';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

function renderTagList(tags = []) {
  if (!Array.isArray(tags) || !tags.length) {
    return '<span class="text-body-muted">-</span>';
  }

  return tags.map((tag) => `
    <span class="rounded-full border border-scapes-light-accent px-2.5 py-1 text-xs font-medium text-body-strong dark:border-scapes-dark-accent">
      ${escapeHtml(tag.name || String(tag))}
    </span>
  `).join('');
}

function getOriginalImageUrl(wallpaper) {
  return wallpaper?.fileUrl || wallpaper?.previewUrl || wallpaper?.thumbnailUrl || '';
}

function renderTagChecklistPlaceholder(message) {
  return `
    <p class="rounded-md border border-dashed border-scapes-light-accent p-3 text-sm text-body-muted dark:border-scapes-dark-accent">
      ${escapeHtml(message)}
    </p>
  `;
}

function renderTagChecklist(tags = [], selectedTagIds = []) {
  if (!Array.isArray(tags) || !tags.length) {
    return renderTagChecklistPlaceholder('No tags are available from the API yet.');
  }

  const selectedIds = new Set(selectedTagIds.map((tagId) => Number(tagId)));

  return tags.map((tag) => `
    <label class="inline-flex cursor-pointer items-center gap-2 rounded-full border border-scapes-light-accent px-3 py-2 text-sm text-body-strong transition-colors duration-300 hover:bg-gray-100 dark:border-scapes-dark-accent dark:hover:bg-gray-800">
      <input
        type="checkbox"
        name="tagIds"
        value="${escapeHtml(String(tag.id))}"
        ${selectedIds.has(Number(tag.id)) ? 'checked' : ''}
        class="h-4 w-4 cursor-pointer accent-scapes-light-primary dark:accent-scapes-dark-primary"
      >
      <span>${escapeHtml(tag.name || 'Untitled Tag')}</span>
    </label>
  `).join('');
}

function renderCategoryOptions(categories = [], selectedCategoryId = null) {
  return [
    '<option value="">Select a category</option>',
    ...categories.map((category) => `
      <option value="${escapeHtml(String(category.id))}" ${Number(category.id) === Number(selectedCategoryId) ? 'selected' : ''}>
        ${escapeHtml(category.name || 'Untitled Category')}
      </option>
    `),
  ].join('');
}

export function renderWallpaperDetailPage(wallpaper) {
  if (!wallpaper) {
    return `
      <section class="min-h-full px-5 py-5 lg:px-8 lg:py-6">
        <h1 class="text-2xl font-bold text-accent-heading">Wallpaper not found</h1>
        <a href="/dashboard" class="secondary-button mt-4">Back to dashboard</a>
      </section>
    `;
  }

  const originalImageUrl = getOriginalImageUrl(wallpaper);
  const originalDimensions = wallpaper.width > 0 && wallpaper.height > 0
    ? `${wallpaper.width} x ${wallpaper.height}px`
    : 'Unknown resolution';

  return `
    <section class="min-h-full space-y-8 bg-white px-5 py-5 dark:bg-gray-950 lg:px-8 lg:py-6">
      <div class="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
        <div class="space-y-3">
          <a href="/dashboard" class="detail-nav-button">
            <i class="fa-solid fa-arrow-left text-xs" aria-hidden="true"></i>
            <span>Back to dashboard</span>
          </a>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          ${renderStatusBadge(wallpaper.status)}
          ${wallpaper.isPending ? `
            <button type="button" data-detail-mode-toggle="edit" class="detail-action-button">
              <i class="fa-solid fa-pen-to-square text-sm" aria-hidden="true"></i>
              <span>Edit details</span>
            </button>
            <button type="button" data-detail-mode-toggle="view" class="detail-cancel-button hidden">
              <i class="fa-solid fa-xmark text-sm" aria-hidden="true"></i>
              <span>Cancel</span>
            </button>
          ` : ''}
        </div>
      </div>

      <div class="grid gap-8 xl:grid-cols-[minmax(0,1.2fr)_minmax(20rem,0.8fr)]">
        <div class="space-y-4">
          ${originalImageUrl ? `
            <a
              href="${escapeHtml(originalImageUrl)}"
              target="_blank"
              rel="noreferrer"
              class="block w-fit max-w-full transition-opacity duration-300 hover:opacity-90"
              title="Open original file"
            >
              <img
                src="${escapeHtml(originalImageUrl)}"
                alt="${escapeHtml(`Original wallpaper: ${wallpaper.title}`)}"
                class="wallpaper-detail-image block h-auto w-auto max-w-full rounded-lg border border-black/8 shadow-[0_8px_28px_rgba(15,23,42,0.08)] dark:border-white/10 dark:shadow-[0_8px_28px_rgba(0,0,0,0.2)]"
                loading="eager"
                decoding="async"
              >
            </a>
          ` : `
            <div class="rounded-xl border border-dashed border-scapes-light-accent p-6 text-sm text-body-muted dark:border-scapes-dark-accent">
              The original wallpaper file is not available.
            </div>
          `}
          <div class="space-y-3">
            <div>
              <div class="flex flex-wrap items-center gap-3">
                <h1 class="text-3xl font-bold text-accent-heading">${escapeHtml(wallpaper.title)}</h1>
                <div class="inline-flex items-center gap-2 rounded-full border border-scapes-light-accent px-3 py-1.5 text-xs font-semibold text-body-strong dark:border-scapes-dark-accent">
                  <i class="fa-solid fa-expand text-[0.7rem]" aria-hidden="true"></i>
                  <span>${escapeHtml(originalDimensions)}</span>
                </div>
              </div>
              <p class="mt-2 text-sm text-body-muted">${escapeHtml(wallpaper.description || 'No description provided.')}</p>
            </div>
          </div>
        </div>

        <div id="wallpaper-detail-view" class="space-y-5">
          <dl class="grid gap-4 sm:grid-cols-2 xl:content-start">
            <div>
              <dt class="text-sm font-semibold text-body-label">Category</dt>
              <dd class="mt-1 text-body-strong">${escapeHtml(wallpaper.category)}</dd>
            </div>
            <div>
              <dt class="text-sm font-semibold text-body-label">Target device</dt>
              <dd class="mt-1 text-body-strong">${escapeHtml(wallpaper.targetDevice || '-')}</dd>
            </div>
            <div class="sm:col-span-2 grid gap-4 sm:grid-cols-2">
              <div>
                <dt class="text-sm font-semibold text-body-label">Tags</dt>
                <dd class="mt-2 flex flex-wrap gap-2">${renderTagList(wallpaper.tags)}</dd>
              </div>
              <div>
                <dt class="text-sm font-semibold text-body-label">File type</dt>
                <dd class="mt-1 text-body-strong">${escapeHtml(wallpaper.mimeType || '-')}</dd>
              </div>
            </div>
            <div>
              <dt class="text-sm font-semibold text-body-label">Last updated</dt>
              <dd class="mt-1 text-body-strong">${formatDetailDate(wallpaper.updatedAt)}</dd>
            </div>
            <div>
              <dt class="text-sm font-semibold text-body-label">Created</dt>
              <dd class="mt-1 text-body-strong">${formatDetailDate(wallpaper.createdAt)}</dd>
            </div>
          </dl>
          ${wallpaper.rejectionReason ? `
            <div class="rounded-md border-l-4 border-red-500 bg-red-50 p-4 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400">
              <strong>Rejection reason:</strong> ${escapeHtml(wallpaper.rejectionReason)}
            </div>
          ` : ''}
        </div>

        ${wallpaper.isPending ? `
          <form id="edit-wallpaper-form" class="hidden space-y-5">
            <div>
              <h2 class="text-xl font-semibold text-accent-heading">Edit wallpaper details</h2>
              <p class="mt-1 text-sm text-body-muted">You can update the title, description, category, and tags before review is completed.</p>
            </div>
            <div id="edit-wallpaper-error" class="hidden rounded-md border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400" role="alert"></div>
            <div class="grid gap-4">
              <div>
                <label for="edit-wallpaper-title" class="mb-1 block text-sm font-semibold text-body-label">Title</label>
                <input
                  id="edit-wallpaper-title"
                  name="title"
                  type="text"
                  required
                  maxlength="255"
                  value="${escapeHtml(wallpaper.title)}"
                  class="field-control"
                  placeholder="Example: Morning Ridge"
                >
              </div>
              <div>
                <label for="edit-wallpaper-description" class="mb-1 block text-sm font-semibold text-body-label">Description</label>
                <textarea
                  id="edit-wallpaper-description"
                  name="description"
                  rows="5"
                  class="field-control"
                  placeholder="Describe the mood or visual story of this wallpaper"
                >${escapeHtml(wallpaper.description || '')}</textarea>
              </div>
              <div>
                <label for="edit-wallpaper-category" class="mb-1 block text-sm font-semibold text-body-label">Category</label>
                <select id="edit-wallpaper-category" name="category" required class="field-control">
                  <option value="${escapeHtml(String(wallpaper.categoryId || ''))}">
                    ${escapeHtml(wallpaper.category || 'Loading category...')}
                  </option>
                </select>
              </div>
              <div>
                <p class="mb-1 block text-sm font-semibold text-body-label">Tags</p>
                <div
                  id="edit-wallpaper-tags-options"
                  class="tag-list-scroll app-scrollbar flex flex-wrap gap-2 rounded-lg border border-scapes-light-accent p-3 dark:border-scapes-dark-accent"
                >
                  ${renderTagChecklistPlaceholder('Loading available tags...')}
                </div>
              </div>
            </div>
            <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
              <button type="submit" id="save-wallpaper-button" class="detail-save-button">
                <i class="fa-solid fa-floppy-disk text-sm" aria-hidden="true"></i>
                <span>Save changes</span>
              </button>
            </div>
          </form>
        ` : ''}
      </div>
    </section>
  `;
}

function setEditWallpaperError(message) {
  const element = document.getElementById('edit-wallpaper-error');
  if (!element) return;

  element.textContent = message;
  element.classList.toggle('hidden', !message);
}

async function loadEditMetadata(wallpaper) {
  const categorySelect = document.getElementById('edit-wallpaper-category');
  const tagContainer = document.getElementById('edit-wallpaper-tags-options');
  const [categoriesResult, tagsResult] = await Promise.allSettled([
    wallpaperRepository.getCategories(),
    wallpaperRepository.getTags(''),
  ]);

  if (categorySelect && categoriesResult.status === 'fulfilled') {
    categorySelect.innerHTML = renderCategoryOptions(categoriesResult.value, wallpaper.categoryId);
  }

  if (tagContainer) {
    const selectedTagIds = Array.isArray(wallpaper.tags)
      ? wallpaper.tags.map((tag) => tag.id)
      : [];

    if (tagsResult.status === 'fulfilled') {
      tagContainer.innerHTML = renderTagChecklist(tagsResult.value, selectedTagIds);
    } else if (Array.isArray(wallpaper.tags) && wallpaper.tags.length) {
      tagContainer.innerHTML = renderTagChecklist(wallpaper.tags, selectedTagIds);
    } else {
      tagContainer.innerHTML = renderTagChecklistPlaceholder(
        'Tags could not be loaded. Your current tags will be kept when you save.',
      );
    }
  }

  const errors = [];
  if (categoriesResult.status === 'rejected') {
    errors.push(categoriesResult.reason?.message || 'Failed to load categories.');
  }
  if (tagsResult.status === 'rejected') {
    errors.push(tagsResult.reason?.message || 'Failed to load tags.');
  }

  if (errors.length) {
    throw new Error(errors.join(' '));
  }
}

function buildMetadataPayload(form, wallpaper) {
  const formData = new FormData(form);
  const tagInputs = Array.from(
    document.querySelectorAll('#edit-wallpaper-tags-options input[name="tagIds"]'),
  );
  const selectedTagIds = tagInputs.length
    ? tagInputs
      .filter((input) => input.checked)
      .slice(0, 15)
      .map((input) => Number.parseInt(input.value, 10))
      .filter((value) => Number.isInteger(value) && value > 0)
    : Array.isArray(wallpaper.tags)
      ? wallpaper.tags
        .map((tag) => Number.parseInt(String(tag.id), 10))
        .filter((value) => Number.isInteger(value) && value > 0)
      : [];

  return {
    title: String(formData.get('title') || '').trim(),
    description: String(formData.get('description') || '').trim(),
    category_id: Number.parseInt(String(formData.get('category') || ''), 10),
    tags: selectedTagIds,
  };
}

export async function initWallpaperDetailPage({ navigate, wallpaper }) {
  if (!wallpaper) return;

  document.title = `Scapes - ${wallpaper.title}`;

  if (!wallpaper.isPending) {
    return;
  }

  const form = document.getElementById('edit-wallpaper-form');
  const submitButton = document.getElementById('save-wallpaper-button');
  const viewContainer = document.getElementById('wallpaper-detail-view');
  const modeButtons = document.querySelectorAll('[data-detail-mode-toggle]');
  let metadataLoaded = false;

  if (!form || !submitButton || !viewContainer) {
    return;
  }

  async function setMode(mode) {
    const isEdit = mode === 'edit';

    if (isEdit && !metadataLoaded) {
      try {
        await loadEditMetadata(wallpaper);
      } catch (error) {
        setEditWallpaperError(error.message || 'Failed to load categories and tags.');
      } finally {
        metadataLoaded = true;
      }
    }

    viewContainer.classList.toggle('hidden', isEdit);
    form.classList.toggle('hidden', !isEdit);
    modeButtons.forEach((button) => {
      const buttonMode = button.dataset.detailModeToggle;
      if (buttonMode === 'edit') {
        button.classList.toggle('hidden', isEdit);
      }
      if (buttonMode === 'view') {
        button.classList.toggle('hidden', !isEdit);
      }
    });
  }

  modeButtons.forEach((button) => {
    button.addEventListener('click', async () => {
      await setMode(button.dataset.detailModeToggle || 'view');
    });
  });

  await setMode('view');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    setEditWallpaperError('');

    const payload = buildMetadataPayload(form, wallpaper);

    if (!payload.title) {
      setEditWallpaperError('Title is required.');
      return;
    }

    if (!Number.isInteger(payload.category_id) || payload.category_id <= 0) {
      setEditWallpaperError('Please select a valid category.');
      return;
    }

    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-sm" aria-hidden="true"></i><span>Saving...</span>';

    try {
      await updateWallpaperMetadata(wallpaperRepository, wallpaper.id, payload);
      renderToast('Wallpaper details updated successfully.', 'success');
      navigate(`/wallpaper/${encodeURIComponent(wallpaper.id)}`, { replace: true });
    } catch (error) {
      setEditWallpaperError(error.message || 'Failed to update wallpaper details.');
    } finally {
      submitButton.disabled = false;
      submitButton.innerHTML = '<i class="fa-solid fa-floppy-disk text-sm" aria-hidden="true"></i><span>Save changes</span>';
    }
  });
}
