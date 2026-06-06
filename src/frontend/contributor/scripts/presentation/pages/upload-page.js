import { submitWallpaper } from '../../domain/use-cases/submit-wallpaper.js';
import { WallpaperRepository } from '../../data/repositories/wallpaper-repository.js';

/**
 * Renders and initializes the Upload page.
 */

export async function initUploadPage(container) {
    container.innerHTML = `
        <div class="max-w-5xl">
            <h1 class="text-3xl font-bold text-gray-900 mb-1">Upload</h1>
            <p class="text-gray-500 mb-8">Share your artwork with the community. Files will be reviewed within 24-48 hours.</p>

            <div id="upload-error" class="hidden mb-6 p-3 rounded-lg bg-red-50 text-red-700 text-sm"></div>

            <div class="bg-gray-100 rounded-2xl p-6 flex flex-col lg:flex-row gap-8">

            <!-- Left: image upload -->
            <div class="flex-1">
                <p class="text-sm font-medium text-gray-700 mb-3">Image Upload</p>

                <div
                id="drop-zone"
                class="relative bg-gray-200 rounded-xl flex flex-col items-center justify-center cursor-pointer transition-colors hover:bg-gray-300 aspect-video">
                <input id="file-input" type="file" accept=".jpg,.jpeg,.png,.webp" class="hidden"/>
                    <div id="drop-placeholder">
                    <svg class="w-12 h-12 text-gray-500 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M12 12V4m0 0L8 8m4-4l4 4" />
                    </svg>
                    <p class="text-sm text-gray-600 text-center">Drop your image here or click to browse</p>
                    <p class="text-xs text-gray-400 text-center mt-1">PNG, JPG, WebP up to 10MB</p>
                </div>
                <img id="image-preview" class="hidden absolute inset-0 w-full h-full object-cover rounded-xl" />
                <div id="preview-overlay" class="hidden absolute inset-0 flex items-center justify-center rounded-xl bg-black/20">
                <svg class="w-10 h-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                </div>
            </div>

            <div id="image-meta" class="hidden mt-3 text-sm text-gray-600 space-y-1">
                <p id="image-resolution"></p>
                <div class="flex items-center gap-2 mt-1">
                    <span class="text-sm text-gray-500">Device:</span>
                <!-- Desktop -->
                <svg class="w-6 h-6 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17H4.5A2.25 2.25 0 012.25 14.75V6A2.25 2.25 0 014.5 3.75h15A2.25 2.25 0 0121.75 6v8.75A2.25 2.25 0 0119.5 17h-5.25m-4.5 0v2.25m0 0H7.5m2.25 0h4.5" />
                </svg>
                <!-- Tablet -->
                <svg class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <rect x="4" y="2" width="16" height="20" rx="2" />
                    <circle cx="12" cy="18" r="1" />
                </svg>
                <!-- Mobile -->
                <svg class="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <rect x="6" y="2" width="12" height="20" rx="2" />
                    <circle cx="12" cy="18" r="1" />
                </svg>
                </div>
            </div>

                <p class="text-xs text-gray-400 mt-3 text-center">Minimum resolution: 1920×1080 (4K recommended)</p>
            </div>

            <!-- Right: metadata form -->
            <div class="flex-1 flex flex-col gap-4">

            <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
            <input
                id="upload-title"
                type="text"
                class="w-full px-3 py-2 bg-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#0d6374]"/>
            </div>

            <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <input
                id="upload-description"
                type="text"
                class="w-full px-3 py-2 bg-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#0d6374]"/>
            </div>

            <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <select
                id="upload-category"
                class="w-full px-3 py-2 bg-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#0d6374] appearance-none">
                <option value="">Loading...</option>
            </select>
            </div>

            <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Tags</label>
                <div id="tag-chips" class="flex flex-wrap gap-2 mb-2"></div>
                <div class="relative">
                <input id="tag-search" type="text" placeholder="Search tags..."
                class="w-full px-3 py-2 bg-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#0d6374]"/>
            <div id="tag-suggestions" class="hidden absolute z-10 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-40 overflow-y-auto">
            </div>
            </div>
            <p class="text-xs text-gray-400 mt-1">Add up to maximum 15 tags</p>
            </div>

            <div>
            <p class="text-sm font-medium text-gray-700 mb-2">Content Confirmation</p>
            <label class="flex items-start gap-3 mb-2 cursor-pointer">
                <input id="confirm-content" type="checkbox" class="mt-0.5 w-4 h-4 accent-[#0d6374] shrink-0" />
                <span class="text-sm text-gray-600">This image contains no nudity, violence, or hate symbols</span>
            </label>
            <label class="flex items-start gap-3 cursor-pointer">
                <input id="confirm-rights" type="checkbox" class="mt-0.5 w-4 h-4 accent-[#0d6374] shrink-0" />
                <span class="text-sm text-gray-600">I confirm this is my original work or I have rights to distribute this wallpaper</span>
            </label>
            </div>

            <div class="flex items-center justify-end mt-2">
            <button
                id="submit-review-btn"
                class="px-5 py-2 bg-gray-300 hover:bg-[#0d6374] hover:text-white text-gray-800 text-sm font-medium rounded-lg transition-colors">
                Submit for Review
            </button>
            </div>

            </div>
            </div>
        </div>
    `;

    // element references
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const dropPlaceholder = document.getElementById('drop-placeholder');
    const imagePreview = document.getElementById('image-preview');
    const previewOverlay = document.getElementById('preview-overlay');
    const imageMeta = document.getElementById('image-meta');
    const imageResolution = document.getElementById('image-resolution');
    const uploadError = document.getElementById('upload-error');
    const categorySelect = document.getElementById('upload-category');

    let selectedFile = null;

    // load categories for select dropdown
    const catResult = await WallpaperRepository.getCategories();

    if (catResult.success) {
    categorySelect.innerHTML = '<option value="">Select a category</option>';
    catResult.categories.forEach((cat) => {
        const opt = document.createElement('option');
        opt.value = cat.id;
        opt.textContent = cat.name;
        categorySelect.appendChild(opt);
    });
    } else {
    categorySelect.innerHTML = '<option value="">Failed to load categories</option>';   
    }

    // dropzone handlers
    dropZone.addEventListener('click', () => fileInput.click());
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('bg-gray-300');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('bg-gray-300');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('bg-gray-300');
        const file = e.dataTransfer.files[0];
        if (file) handleFileSelect(file);
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files[0]) handleFileSelect(fileInput.files[0]);
    });

    /**
    * Handles file selection: shows preview and resolution.
    */

    function handleFileSelect(file) {
        selectedFile = file;

        const url = URL.createObjectURL(file);
        const img = new Image();
        img.onload = () => {
            imagePreview.src = url;
            imagePreview.classList.remove('hidden');
            previewOverlay.classList.remove('hidden');
            dropPlaceholder.classList.add('hidden');
            imageResolution.textContent = `Resolution: ${img.naturalWidth}×${img.naturalHeight}`;
            imageMeta.classList.remove('hidden');
        };
        img.src = url;
    }

    // tag search and selection
    const selectedTags = [];
    let debounceTimer = null;

    const tagSearch = document.getElementById('tag-search');
    const tagSuggestions = document.getElementById('tag-suggestions');
    const tagChips = document.getElementById('tag-chips');

    tagSearch.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const keyword = tagSearch.value.trim();

        if (keyword.length < 2) {
        tagSuggestions.classList.add('hidden');
        tagSuggestions.innerHTML = '';
        return;
        }

    debounceTimer = setTimeout(async () => {
        const result = await WallpaperRepository.getTags(keyword);
        if (!result.success || result.tags.length === 0) {
            tagSuggestions.classList.add('hidden');
            return;
        }

        // filter out already selected tags
        const filtered = result.tags.filter(
        (t) => !selectedTags.find((s) => s.id === t.id)
        );

        if (filtered.length === 0) {
            tagSuggestions.classList.add('hidden');
            return;
        }

        tagSuggestions.innerHTML = filtered
            .map(
                (t) => `
                <button
                type="button"
                data-id="${t.id}"
                data-name="${t.name}"
                class="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
            ${t.name}
            </button>`)
        .join('');

        tagSuggestions.classList.remove('hidden');

        tagSuggestions.querySelectorAll('button').forEach((btn) => {
            btn.addEventListener('click', () => {
                if (selectedTags.length >= 15) return;
                selectedTags.push({ id: parseInt(btn.dataset.id, 10), name: btn.dataset.name });
                
                renderChips();
                tagSearch.value = '';
                tagSuggestions.classList.add('hidden');
                
                if (selectedTags.length >= 15) {
                    tagSearch.disabled = true;
                    tagSearch.placeholder = 'Maximum 15 tags reached';
                }
            });
        });
    }, 300);
    });

    // close suggestions on outside click
    document.addEventListener('click', (e) => {
    if (!tagSearch.contains(e.target) && !tagSuggestions.contains(e.target)) {
      tagSuggestions.classList.add('hidden');
    }
    });

    function renderChips() {
        tagChips.innerHTML = selectedTags
        .map(
            (t) => `
            <span class="inline-flex items-center gap-1 px-2 py-1 bg-[#0d6374] text-white text-xs rounded-full">
            ${t.name}
            <button type="button" data-id="${t.id}" class="remove-chip hover:text-gray-200">×</button>
            </span>`)
        .join('');

        tagChips.querySelectorAll('.remove-chip').forEach((btn) => {
            btn.addEventListener('click', () => {
            const id = parseInt(btn.dataset.id, 10);
            const idx = selectedTags.findIndex((t) => t.id === id);
            if (idx !== -1) selectedTags.splice(idx, 1);
            renderChips();
            tagSearch.disabled = false;
            tagSearch.placeholder = 'Search tags...';
            });
        });
    }

    // form submission
    function setError(msg) {
        uploadError.textContent = msg;
        uploadError.classList.toggle('hidden', !msg);
    }

    function getPayload() {
    const categoryId = parseInt(categorySelect.value, 10);

    return {
        file: selectedFile,
        title: document.getElementById('upload-title').value,
        description: document.getElementById('upload-description').value,
        categoryId,
        tagIds: selectedTags.map((t) => t.id),
        confirmContent: document.getElementById('confirm-content').checked,
        confirmRights: document.getElementById('confirm-rights').checked,
    };  
    }

    function showSuccessModal(message) {
        const modal = document.getElementById('success-modal');
        document.getElementById('success-modal-message').textContent = message;
        modal.classList.remove('hidden');
        setTimeout(() => modal.classList.add('hidden'), 3000);
    }

    document.getElementById('submit-review-btn').addEventListener('click', async () => {
        setError('');
        const btn = document.getElementById('submit-review-btn');
        btn.textContent = 'Submitting...';
        btn.disabled = true;

        const result = await submitWallpaper(getPayload());

        if (result.success) {
        showSuccessModal('Successfully submitted artwork. You can track the approval status in your Portfolio.');
        } else {
        setError(result.message);
        }

        btn.textContent = 'Submit for Review';
        btn.disabled = false;
    }); 
}