import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatBytes } from '../../core/utils/formatters.js';
import {
  MIN_WALLPAPER_RESOLUTION_BY_TARGET_DEVICE,
  validateWallpaperDimensions,
  validateWallpaperFile,
} from '../../core/utils/wallpaper-file.js';
import { wallpaperRepository } from '../../data/repositories/wallpaper-repository.js';
import { listWallpaperCategories } from '../../domain/use-cases/list-wallpaper-categories.js';
import { submitWallpaper } from '../../domain/use-cases/submit-wallpaper.js';
import { renderToast } from '../components/toast.js';

const TARGET_DEVICE_LABELS = {
  desktop: 'Desktop',
  mobile: 'Mobile',
  tablet: 'Tablet',
};

function formatTargetDeviceLabel(targetDevice) {
  return TARGET_DEVICE_LABELS[targetDevice] || 'Unknown';
}

function formatResolutionValue({ width, height }) {
  return `${width}x${height}`;
}

function buildTargetDeviceTooltip(targetDevice) {
  const minimumResolution = MIN_WALLPAPER_RESOLUTION_BY_TARGET_DEVICE[targetDevice];
  if (!minimumResolution) {
    return formatTargetDeviceLabel(targetDevice);
  }

  return `${formatTargetDeviceLabel(targetDevice)} minimum ${formatResolutionValue(minimumResolution)}`;
}

function setUploadError(message) {
  const element = document.getElementById('upload-error');
  element.textContent = message;
  element.classList.toggle('hidden', !message);
}

async function loadCategories() {
  const select = document.getElementById('wallpaper-category');
  const categories = await listWallpaperCategories(wallpaperRepository);

  select.innerHTML = '<option value="">Select a category</option>';
  categories.forEach((category) => {
    const option = document.createElement('option');
    option.value = String(category.id);
    option.textContent = category.name || 'Untitled Category';
    select.appendChild(option);
  });
}

async function loadTags() {
  const container = document.getElementById('wallpaper-tags-options');

  try {
    const tags = await wallpaperRepository.getTags('');

    if (!tags.length) {
      container.innerHTML = `
        <p class="rounded-md border border-dashed border-scapes-light-accent p-3 text-sm text-scapes-light-secondary dark:border-scapes-dark-accent dark:text-scapes-dark-secondary">
          No tags are available from the API yet.
        </p>
      `;
      return;
    }

    container.innerHTML = tags.map((tag) => `
      <label class="inline-flex cursor-pointer items-center gap-2 rounded-full border border-scapes-light-accent px-3 py-2 text-sm text-scapes-light-primary transition-colors duration-300 hover:bg-white dark:border-scapes-dark-accent dark:text-scapes-dark-primary dark:hover:bg-gray-900">
        <input
          type="checkbox"
          name="tagIds"
          value="${escapeHtml(String(tag.id))}"
          data-tag-name="${escapeHtml(tag.name || '')}"
          class="h-4 w-4 cursor-pointer accent-scapes-light-primary dark:accent-scapes-dark-primary"
        >
        <span>${escapeHtml(tag.name || 'Untitled Tag')}</span>
      </label>
    `).join('');
  } catch {
    container.innerHTML = `
      <p class="rounded-md border border-dashed border-scapes-light-accent p-3 text-sm text-scapes-light-secondary dark:border-scapes-dark-accent dark:text-scapes-dark-secondary">
        Tags could not be loaded. You can still submit without tags.
      </p>
    `;
  }
}

function bindFilePreview() {
  const input = document.getElementById('wallpaper-file');
  const preview = document.getElementById('image-preview');
  const placeholder = document.getElementById('drop-placeholder');
  const meta = document.getElementById('file-meta');
  const resolution = document.getElementById('file-resolution');
  const dropZone = document.getElementById('drop-zone');
  let currentObjectUrl = null;

  function setDeviceHighlight(targetDevice = '') {
    const options = Array.from(document.querySelectorAll('[data-device-option]'));

    options.forEach((option) => {
      const isActive = option.dataset.deviceOption === targetDevice;
      option.classList.toggle('is-active', isActive);
    });
  }

  function resetPreviewMetadata() {
    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl);
      currentObjectUrl = null;
    }

    preview.removeAttribute('src');
    preview.classList.add('hidden');
    placeholder.classList.remove('hidden');
    meta.textContent = '';
    resolution.textContent = 'No resolution detected yet';
    dropZone.dataset.width = '';
    dropZone.dataset.height = '';
    dropZone.dataset.targetDevice = '';
    setDeviceHighlight('');
  }

  function handleFile(file) {
    const error = validateWallpaperFile(file);
    setUploadError(error);

    if (error) {
      resetPreviewMetadata();
      return;
    }

    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl);
    }

    currentObjectUrl = URL.createObjectURL(file);
    const image = new Image();

    image.onload = () => {
      const width = image.naturalWidth;
      const height = image.naturalHeight;
      const validation = validateWallpaperDimensions(width, height);

      preview.src = currentObjectUrl;
      preview.classList.remove('hidden');
      placeholder.classList.add('hidden');
      meta.textContent = `${file.name} - ${formatBytes(file.size)}`;
      resolution.textContent = `${width} x ${height}px`;
      dropZone.dataset.width = String(width);
      dropZone.dataset.height = String(height);
      dropZone.dataset.targetDevice = validation.targetDevice;
      setDeviceHighlight(validation.targetDevice);
      setUploadError(validation.error);
    };
    image.onerror = () => {
      setUploadError('The wallpaper file could not be processed.');
      resetPreviewMetadata();
    };
    image.src = currentObjectUrl;
  }

  input.addEventListener('change', () => {
    if (input.files[0]) {
      handleFile(input.files[0]);
      return;
    }

    resetPreviewMetadata();
  });

  dropZone.addEventListener('dragover', (event) => {
    event.preventDefault();
    dropZone.classList.add('bg-white', 'dark:bg-gray-900');
  });

  dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('bg-white', 'dark:bg-gray-900');
  });

  dropZone.addEventListener('drop', (event) => {
    event.preventDefault();
    dropZone.classList.remove('bg-white', 'dark:bg-gray-900');

    const file = event.dataTransfer.files[0];
    if (!file) return;

    const transfer = new DataTransfer();
    transfer.items.add(file);
    input.files = transfer.files;
    handleFile(file);
  });

  resetPreviewMetadata();
}

function buildSubmission(form) {
  const formData = new FormData(form);
  const selectedTags = Array.from(
    document.querySelectorAll('input[name="tagIds"]:checked'),
  ).slice(0, 15);
  const tagIds = selectedTags
    .map((input) => Number.parseInt(input.value, 10))
    .filter((value) => Number.isInteger(value) && value > 0);
  const tagNames = selectedTags
    .map((input) => input.dataset.tagName || '')
    .filter(Boolean);

  const payload = new FormData();
  payload.set('file', formData.get('file'));
  payload.set('title', String(formData.get('title') || '').trim());
  payload.set('description', String(formData.get('description') || '').trim());
  payload.set('category_id', String(formData.get('category') || ''));
  if (tagIds.length) {
    payload.set('tags', JSON.stringify(tagIds));
  }

  return {
    formData: payload,
    title: String(formData.get('title') || '').trim(),
    description: String(formData.get('description') || '').trim(),
    categoryId: String(formData.get('category') || ''),
    categoryName: document.getElementById('wallpaper-category').selectedOptions[0]?.textContent || '',
    tags: tagNames,
  };
}

export function renderUploadPage() {
  return `
    <section class="space-y-6">
      <form id="upload-form" class="rounded-[2rem] bg-white p-5 shadow-[0_18px_42px_rgba(15,23,42,0.08)] dark:bg-gray-950 dark:shadow-[0_22px_56px_rgba(0,0,0,0.24)] lg:p-8">
        <div class="mb-8 space-y-2">
          <h1 class="text-3xl font-bold text-accent-heading">Upload Wallpaper</h1>
          <p class="text-sm text-body-muted">Upload a new wallpaper to enter the moderation queue.</p>
        </div>

        <div class="grid gap-8 xl:grid-cols-[minmax(0,1.15fr)_minmax(22rem,0.85fr)]">
          <div class="space-y-5">
            <label for="wallpaper-file" id="drop-zone" class="flex min-h-[24rem] cursor-pointer flex-col items-center justify-center rounded-[1.75rem] border-2 border-dashed border-scapes-light-accent bg-scapes-light-base/75 p-4 text-center transition-colors duration-300 hover:bg-white dark:border-scapes-dark-accent dark:bg-scapes-dark-base dark:hover:bg-gray-900">
              <input id="wallpaper-file" name="file" type="file" accept="image/jpeg,image/png,image/webp" class="sr-only">
              <img id="image-preview" alt="Preview of the selected wallpaper" class="wallpaper-detail-image hidden h-auto w-auto max-w-full rounded-lg border border-black/8 shadow-[0_8px_28px_rgba(15,23,42,0.08)] dark:border-white/10 dark:shadow-[0_8px_28px_rgba(0,0,0,0.2)]">
              <span id="drop-placeholder" class="px-4">
                <span class="block font-heading text-lg font-bold text-accent-heading">Choose or drop an image</span>
                <span class="mt-2 block text-sm text-body-muted">JPG, PNG, or WebP up to 10MB. Target device will be detected automatically.</span>
              </span>
            </label>

            <div class="space-y-4">
              <p id="file-meta" class="text-sm text-body-muted">No file selected.</p>

              <div class="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
                <div class="min-w-0">
                  <p class="text-xs font-semibold uppercase tracking-[0.18em] text-body-muted">Detected Resolution</p>
                  <p id="file-resolution" class="mt-4 text-xl font-semibold text-body-strong">No resolution detected yet</p>
                </div>

                <div class="md:max-w-[24rem]">
                  <p class="text-xs font-semibold uppercase tracking-[0.18em] text-body-muted">Target Device</p>
                  <div class="mt-3 flex flex-wrap gap-3">
                    <span
                      data-device-option="desktop"
                      class="upload-device-pill"
                      title="${escapeHtml(buildTargetDeviceTooltip('desktop'))}"
                    >
                      <i class="fa-solid fa-desktop text-sm" aria-hidden="true"></i>
                      <span>Desktop</span>
                    </span>
                    <span
                      data-device-option="mobile"
                      class="upload-device-pill"
                      title="${escapeHtml(buildTargetDeviceTooltip('mobile'))}"
                    >
                      <i class="fa-solid fa-mobile-screen-button text-sm" aria-hidden="true"></i>
                      <span>Mobile</span>
                    </span>
                    <span
                      data-device-option="tablet"
                      class="upload-device-pill"
                      title="${escapeHtml(buildTargetDeviceTooltip('tablet'))}"
                    >
                      <i class="fa-solid fa-tablet-screen-button text-sm" aria-hidden="true"></i>
                      <span>Tablet</span>
                    </span>
                  </div>
                </div>
              </div>

            </div>
          </div>

          <div class="space-y-5 border-t border-scapes-light-accent/30 pt-6 dark:border-scapes-dark-accent/40 xl:border-t-0 xl:border-l xl:pt-0 xl:pl-8">
            <div id="upload-error" class="hidden rounded-md border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400" role="alert"></div>
            <div>
              <label for="wallpaper-title" class="mb-1 block text-sm font-semibold text-body-label">Title</label>
              <input id="wallpaper-title" name="title" type="text" required class="field-control" placeholder="Example: Morning Ridge">
            </div>
            <div>
              <label for="wallpaper-description" class="mb-1 block text-sm font-semibold text-body-label">Description</label>
              <textarea id="wallpaper-description" name="description" rows="4" class="field-control" placeholder="Describe the mood of this wallpaper"></textarea>
            </div>
            <div>
              <label for="wallpaper-category" class="mb-1 block text-sm font-semibold text-body-label">Category</label>
              <select id="wallpaper-category" name="category" required class="field-control">
                <option value="">Select a category</option>
              </select>
            </div>
            <div>
              <p class="mb-1 block text-sm font-semibold text-body-label">Tags</p>
              <div id="wallpaper-tags-options" class="tag-list-scroll app-scrollbar flex flex-wrap gap-2 rounded-[1.25rem] border border-scapes-light-accent/70 p-3 dark:border-scapes-dark-accent/70">
                <p class="text-sm text-body-muted">Loading tags...</p>
              </div>
            </div>
            <div class="space-y-0">
              <label class="upload-checklist-item flex items-start gap-3">
                <input id="confirm-content" type="checkbox" required class="mt-1 h-4 w-4 cursor-pointer accent-scapes-light-primary dark:accent-scapes-dark-primary">
                <span>The content does not contain nudity, violence, or hate symbols.</span>
              </label>
              <label class="upload-checklist-item flex items-start gap-3">
                <input id="confirm-rights" type="checkbox" required class="h-4 w-4 cursor-pointer accent-scapes-light-primary dark:accent-scapes-dark-primary">
                <span>I have the rights to distribute this wallpaper.</span>
              </label>
            </div>
            <button id="submit-upload" type="submit" class="upload-submit-button w-full">
              <i class="fa-solid fa-paper-plane text-sm" aria-hidden="true"></i>
              <span>Submit for Review</span>
            </button>
          </div>
        </div>
      </form>
    </section>
  `;
}

export async function initUploadPage({ navigate }) {
  bindFilePreview();
  try {
    await loadCategories();
    await loadTags();
  } catch (error) {
    setUploadError(error.message || 'Failed to load upload metadata.');
  }

  const form = document.getElementById('upload-form');
  const submitButton = document.getElementById('submit-upload');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    setUploadError('');

    const file = document.getElementById('wallpaper-file').files[0];
    const fileError = validateWallpaperFile(file);
    if (fileError) {
      setUploadError(fileError);
      return;
    }

    const dropZone = document.getElementById('drop-zone');
    const width = Number.parseInt(dropZone.dataset.width || '', 10);
    const height = Number.parseInt(dropZone.dataset.height || '', 10);

    if (!Number.isInteger(width) || !Number.isInteger(height)) {
      setUploadError('Wallpaper resolution could not be detected. Please choose the file again.');
      return;
    }

    const dimensionValidation = validateWallpaperDimensions(width, height);
    if (!dimensionValidation.isValid) {
      setUploadError(dimensionValidation.error);
      return;
    }

    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-sm" aria-hidden="true"></i><span>Submitting...</span>';

    try {
      await submitWallpaper(wallpaperRepository, buildSubmission(form));
      renderToast('Wallpaper submitted for review.', 'success');
      form.reset();
      document.getElementById('wallpaper-file').dispatchEvent(new Event('change'));
      navigate('/dashboard');
    } catch (error) {
      setUploadError(error.message);
    } finally {
      submitButton.disabled = false;
      submitButton.innerHTML = '<i class="fa-solid fa-paper-plane text-sm" aria-hidden="true"></i><span>Submit for Review</span>';
    }
  });
}
