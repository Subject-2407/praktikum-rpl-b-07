import { escapeHtml } from '../../core/utils/escape-html.js';
import { formatBytes } from '../../core/utils/formatters.js';
import { validateWallpaperFile } from '../../core/utils/wallpaper-file.js';
import { wallpaperRepository } from '../../data/repositories/wallpaper-repository.js';
import { listWallpaperCategories } from '../../domain/use-cases/list-wallpaper-categories.js';
import { submitWallpaper } from '../../domain/use-cases/submit-wallpaper.js';
import { renderToast } from '../components/toast.js';

function setUploadError(message) {
  const element = document.getElementById('upload-error');
  element.textContent = message;
  element.classList.toggle('hidden', !message);
}

async function loadCategories() {
  const select = document.getElementById('wallpaper-category');
  const categories = await listWallpaperCategories(wallpaperRepository);

  select.innerHTML = '<option value="">Pilih kategori</option>';
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
          Belum ada tag yang tersedia dari API.
        </p>
      `;
      return;
    }

    container.innerHTML = tags.map((tag) => `
      <label class="inline-flex items-center gap-2 rounded-full border border-scapes-light-accent px-3 py-2 text-sm text-scapes-light-primary transition-colors duration-300 hover:bg-white dark:border-scapes-dark-accent dark:text-scapes-dark-primary dark:hover:bg-gray-900">
        <input
          type="checkbox"
          name="tagIds"
          value="${escapeHtml(String(tag.id))}"
          data-tag-name="${escapeHtml(tag.name || '')}"
          class="h-4 w-4 accent-scapes-light-primary dark:accent-scapes-dark-primary"
        >
        <span>${escapeHtml(tag.name || 'Untitled Tag')}</span>
      </label>
    `).join('');
  } catch {
    container.innerHTML = `
      <p class="rounded-md border border-dashed border-scapes-light-accent p-3 text-sm text-scapes-light-secondary dark:border-scapes-dark-accent dark:text-scapes-dark-secondary">
        Tag belum dapat dimuat. Kamu masih bisa submit tanpa tag.
      </p>
    `;
  }
}

function bindFilePreview() {
  const input = document.getElementById('wallpaper-file');
  const preview = document.getElementById('image-preview');
  const placeholder = document.getElementById('drop-placeholder');
  const meta = document.getElementById('file-meta');
  const dropZone = document.getElementById('drop-zone');
  let currentObjectUrl = null;

  function handleFile(file) {
    const error = validateWallpaperFile(file);
    setUploadError(error);

    if (error) return;

    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl);
    }

    currentObjectUrl = URL.createObjectURL(file);
    const image = new Image();

    image.onload = () => {
      preview.src = currentObjectUrl;
      preview.classList.remove('hidden');
      placeholder.classList.add('hidden');
      meta.textContent = `${file.name} - ${formatBytes(file.size)} - ${image.naturalWidth}x${image.naturalHeight}`;
    };
    image.src = currentObjectUrl;
  }

  input.addEventListener('change', () => {
    if (input.files[0]) handleFile(input.files[0]);
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
      <div>
        <h1 class="text-3xl font-bold text-scapes-light-primary dark:text-scapes-dark-primary">Upload Wallpaper</h1>
        <p class="mt-2 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">Kirim wallpaper baru untuk masuk antrean moderasi.</p>
      </div>

      <form id="upload-form" class="grid gap-6 lg:grid-cols-[1fr_24rem]">
        <div class="panel-card">
          <label for="wallpaper-file" id="drop-zone" class="flex aspect-video cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-scapes-light-accent bg-scapes-light-base text-center transition-colors duration-300 hover:bg-white dark:border-scapes-dark-accent dark:bg-scapes-dark-base dark:hover:bg-gray-900">
            <input id="wallpaper-file" name="file" type="file" accept="image/jpeg,image/png,image/webp" class="sr-only">
            <img id="image-preview" alt="Preview wallpaper yang dipilih" class="hidden h-full w-full rounded-md object-cover">
            <span id="drop-placeholder" class="px-4">
              <span class="block font-heading text-lg font-bold text-scapes-light-primary dark:text-scapes-dark-primary">Pilih atau drop image</span>
              <span class="mt-2 block text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">JPG, PNG, atau WebP maksimal 10MB. Minimum 1920x1080 disarankan.</span>
            </span>
          </label>
          <p id="file-meta" class="mt-3 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary"></p>
        </div>

        <div class="panel-card space-y-4">
          <div id="upload-error" class="hidden rounded-md border-l-4 border-red-500 bg-red-50 p-3 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-400" role="alert"></div>
          <div>
            <label for="wallpaper-title" class="mb-1 block text-sm font-semibold text-scapes-light-primary dark:text-scapes-dark-primary">Judul</label>
            <input id="wallpaper-title" name="title" type="text" required class="field-control" placeholder="Contoh: Morning Ridge">
          </div>
          <div>
            <label for="wallpaper-description" class="mb-1 block text-sm font-semibold text-scapes-light-primary dark:text-scapes-dark-primary">Deskripsi</label>
            <textarea id="wallpaper-description" name="description" rows="4" class="field-control" placeholder="Ceritakan suasana wallpaper ini"></textarea>
          </div>
          <div>
            <label for="wallpaper-category" class="mb-1 block text-sm font-semibold text-scapes-light-primary dark:text-scapes-dark-primary">Kategori</label>
            <select id="wallpaper-category" name="category" required class="field-control">
              <option value="">Pilih kategori</option>
            </select>
          </div>
          <div>
            <p class="mb-1 block text-sm font-semibold text-scapes-light-primary dark:text-scapes-dark-primary">Tags</p>
            <div id="wallpaper-tags-options" class="tag-list-scroll app-scrollbar flex flex-wrap gap-2 rounded-lg border border-scapes-light-accent p-3 dark:border-scapes-dark-accent">
              <p class="text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">Memuat daftar tag...</p>
            </div>
            <p class="mt-1 text-xs text-scapes-light-secondary dark:text-scapes-dark-secondary">Pilih maksimal 15 tag yang tersedia dari API.</p>
          </div>
          <label class="flex items-start gap-3 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">
            <input id="confirm-content" type="checkbox" required class="mt-1 h-4 w-4 accent-scapes-light-primary dark:accent-scapes-dark-primary">
            <span>Konten tidak mengandung nudity, violence, atau hate symbols.</span>
          </label>
          <label class="flex items-start gap-3 text-sm text-scapes-light-secondary dark:text-scapes-dark-secondary">
            <input id="confirm-rights" type="checkbox" required class="mt-1 h-4 w-4 accent-scapes-light-primary dark:accent-scapes-dark-primary">
            <span>Saya memiliki hak untuk mendistribusikan wallpaper ini.</span>
          </label>
          <button id="submit-upload" type="submit" class="primary-button w-full">Submit for Review</button>
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
    setUploadError(error.message || 'Gagal memuat metadata upload.');
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

    submitButton.disabled = true;
    submitButton.textContent = 'Submitting...';

    try {
      await submitWallpaper(wallpaperRepository, buildSubmission(form));
      renderToast('Wallpaper submitted for review.', 'success');
      form.reset();
      document.getElementById('image-preview').classList.add('hidden');
      document.getElementById('drop-placeholder').classList.remove('hidden');
      document.getElementById('file-meta').textContent = '';
      navigate('/dashboard');
    } catch (error) {
      setUploadError(error.message);
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Submit for Review';
    }
  });
}
