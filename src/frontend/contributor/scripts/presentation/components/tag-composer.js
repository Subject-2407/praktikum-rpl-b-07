import { escapeHtml } from '../../core/utils/escape-html.js';

const DEFAULT_MAX_TAG_COUNT = 15;
const DEFAULT_TAG_SUGGESTION_LIMIT = 8;
const DEFAULT_TAG_AUTOCOMPLETE_DELAY_MS = 180;
const DEFAULT_IDLE_MESSAGE = 'Separated by space.';

export function normalizeTagToken(rawValue) {
  const trimmed = String(rawValue || '').trim();
  if (!trimmed) {
    return {
      value: '',
      error: '',
    };
  }

  if (!trimmed.startsWith('#')) {
    return {
      value: '',
      error: 'Each tag must start with #.',
    };
  }

  const normalized = trimmed
    .slice(1)
    .toLowerCase()
    .replace(/[^a-z0-9\s-]+/g, '')
    .replace(/[\s-]+/g, '-')
    .replace(/^-+|-+$/g, '');

  if (!normalized) {
    return {
      value: '',
      error: 'Each tag must contain letters or numbers.',
    };
  }

  return {
    value: `#${normalized}`,
    error: '',
  };
}

export function createTagComposer({
  repository,
  hiddenInputId,
  badgesId,
  inputId,
  suggestionsId,
  fieldId,
  composerId,
  statusId,
  initialTags = [],
  maxTagCount = DEFAULT_MAX_TAG_COUNT,
  suggestionLimit = DEFAULT_TAG_SUGGESTION_LIMIT,
  suggestionDelayMs = DEFAULT_TAG_AUTOCOMPLETE_DELAY_MS,
  idleMessage = DEFAULT_IDLE_MESSAGE,
} = {}) {
  const hiddenInput = document.getElementById(hiddenInputId);
  const badges = document.getElementById(badgesId);
  const input = document.getElementById(inputId);
  const suggestions = document.getElementById(suggestionsId);
  const field = document.getElementById(fieldId);
  const composer = document.getElementById(composerId);
  const status = document.getElementById(statusId);

  if (!hiddenInput || !badges || !input || !suggestions || !field || !composer || !status || !repository) {
    return null;
  }

  const state = {
    tags: [],
    suggestions: [],
    activeSuggestionIndex: -1,
    debounceTimer: 0,
    requestSequence: 0,
  };

  function setStatus(message, tone = 'muted') {
    status.textContent = message;
    status.className = tone === 'error'
      ? 'mt-2 text-sm text-red-600 dark:text-red-400'
      : 'mt-2 text-sm text-body-muted';
  }

  function syncHiddenValue() {
    hiddenInput.value = state.tags.join(' ');
  }

  function closeSuggestions() {
    state.suggestions = [];
    state.activeSuggestionIndex = -1;
    suggestions.classList.add('hidden');
    suggestions.innerHTML = '';
  }

  function renderBadges() {
    badges.innerHTML = state.tags.map((tag) => `
      <span class="inline-flex items-center gap-2 rounded-full bg-scapes-light-primary px-3 py-1.5 text-sm font-medium text-white dark:bg-scapes-dark-primary dark:text-scapes-dark-base">
        <span>${escapeHtml(tag)}</span>
        <button
          type="button"
          class="inline-flex h-5 w-5 items-center justify-center rounded-full bg-white/20 text-[0.72rem] text-white transition-colors duration-200 hover:bg-white/35"
          data-remove-tag="${escapeHtml(tag)}"
          aria-label="Remove ${escapeHtml(tag)}"
        >
          <i class="fa-solid fa-xmark" aria-hidden="true"></i>
        </button>
      </span>
    `).join('');
    syncHiddenValue();
  }

  function renderSuggestions() {
    if (!state.suggestions.length) {
      closeSuggestions();
      return;
    }

    suggestions.innerHTML = state.suggestions.map((tag, index) => {
      const token = normalizeTagToken(`#${tag.slug || tag.name || ''}`).value;
      const isActive = index === state.activeSuggestionIndex;

      return `
        <button
          type="button"
          class="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-2 text-left text-sm transition-colors duration-200 ${isActive ? 'bg-scapes-light-primary text-white dark:bg-scapes-dark-primary dark:text-scapes-dark-base' : 'text-body-strong hover:bg-scapes-light-base dark:hover:bg-scapes-dark-base'}"
          data-tag-suggestion="${escapeHtml(token)}"
        >
          <span class="font-medium">${escapeHtml(token)}</span>
          <span class="text-xs opacity-70">${escapeHtml(tag.name || tag.slug || '')}</span>
        </button>
      `;
    }).join('');
    suggestions.classList.remove('hidden');
  }

  function addTag(rawValue) {
    const result = normalizeTagToken(rawValue);
    if (result.error) {
      setStatus(result.error, 'error');
      return false;
    }

    if (!result.value) {
      return true;
    }

    if (state.tags.includes(result.value)) {
      setStatus(`${result.value} is already added.`, 'muted');
      return true;
    }

    if (state.tags.length >= maxTagCount) {
      setStatus(`You can add up to ${maxTagCount} tags.`, 'error');
      return false;
    }

    state.tags.push(result.value);
    renderBadges();
    return true;
  }

  function removeTag(value) {
    state.tags = state.tags.filter((tag) => tag !== value);
    renderBadges();
    setStatus('Tag removed.');
  }

  function consumeCompletedTokens() {
    const value = input.value;
    const segments = value.split(/\s+/);
    const completed = /\s$/.test(value) ? segments : segments.slice(0, -1);
    const remainder = /\s$/.test(value) ? '' : (segments.at(-1) || '');

    if (!completed.length) {
      return;
    }

    let allValid = true;
    completed
      .map((segment) => segment.trim())
      .filter(Boolean)
      .forEach((segment) => {
        allValid = addTag(segment) && allValid;
      });

    input.value = allValid ? remainder : value.trimStart();
  }

  async function fetchSuggestions() {
    const query = input.value.trim();
    if (!query || !query.startsWith('#')) {
      closeSuggestions();
      if (query && !query.startsWith('#')) {
        setStatus('Each tag must start with #.', 'error');
      } else if (!state.tags.length) {
        setStatus(idleMessage);
      }
      return;
    }

    const sequence = ++state.requestSequence;
    try {
      const items = await repository.getTags(query.slice(1), {
        match: 'prefix',
        limit: suggestionLimit,
      });

      if (sequence !== state.requestSequence) {
        return;
      }

      state.suggestions = items.filter((tag) => {
        const token = normalizeTagToken(`#${tag.slug || tag.name || ''}`).value;
        return token && !state.tags.includes(token);
      });
      state.activeSuggestionIndex = state.suggestions.length ? 0 : -1;

      setStatus(idleMessage);
      renderSuggestions();
    } catch {
      closeSuggestions();
      setStatus(idleMessage, 'muted');
    }
  }

  function scheduleSuggestions() {
    window.clearTimeout(state.debounceTimer);
    state.debounceTimer = window.setTimeout(fetchSuggestions, suggestionDelayMs);
  }

  function commitPendingInput() {
    const pending = input.value.trim();
    if (!pending) {
      closeSuggestions();
      return true;
    }

    const added = addTag(pending);
    if (!added) {
      return false;
    }

    input.value = '';
    closeSuggestions();
    return true;
  }

  function setTags(tags = []) {
    const normalized = [];
    const seen = new Set();

    tags.forEach((tag) => {
      const label = typeof tag === 'object'
        ? (tag.name || tag.tag_text || tag.tagText || tag.slug || '')
        : tag;
      const result = normalizeTagToken(String(label).startsWith('#') ? label : `#${label}`);
      if (!result.value) {
        return;
      }

      if (!seen.has(result.value)) {
        seen.add(result.value);
        normalized.push(result.value);
      }
    });

    state.tags = normalized.slice(0, maxTagCount);
    renderBadges();
    closeSuggestions();
    setStatus(idleMessage);
  }

  function reset() {
    state.tags = [];
    state.suggestions = [];
    state.activeSuggestionIndex = -1;
    window.clearTimeout(state.debounceTimer);
    input.value = '';
    renderBadges();
    closeSuggestions();
    setStatus(idleMessage);
  }

  composer.addEventListener('click', (event) => {
    const removeButton = event.target.closest('[data-remove-tag]');
    if (removeButton) {
      removeTag(removeButton.dataset.removeTag || '');
      input.focus();
      return;
    }

    input.focus();
  });

  suggestions.addEventListener('click', (event) => {
    const button = event.target.closest('[data-tag-suggestion]');
    if (!button) {
      return;
    }

    addTag(button.dataset.tagSuggestion || '');
    input.value = '';
    closeSuggestions();
    input.focus();
  });

  input.addEventListener('input', () => {
    consumeCompletedTokens();
    scheduleSuggestions();
  });

  input.addEventListener('focus', () => {
    if (input.value.trim()) {
      scheduleSuggestions();
      return;
    }

    if (!state.tags.length) {
      setStatus(idleMessage);
    }
  });

  input.addEventListener('keydown', (event) => {
    if (event.key === 'Backspace' && !input.value && state.tags.length) {
      removeTag(state.tags[state.tags.length - 1]);
      return;
    }

    if (event.key === 'Escape') {
      closeSuggestions();
      return;
    }

    if (event.key === 'ArrowDown' && state.suggestions.length) {
      event.preventDefault();
      state.activeSuggestionIndex = state.activeSuggestionIndex < state.suggestions.length - 1
        ? state.activeSuggestionIndex + 1
        : 0;
      renderSuggestions();
      return;
    }

    if (event.key === 'ArrowUp' && state.suggestions.length) {
      event.preventDefault();
      state.activeSuggestionIndex = state.activeSuggestionIndex > 0
        ? state.activeSuggestionIndex - 1
        : state.suggestions.length - 1;
      renderSuggestions();
      return;
    }

    if (event.key === 'Enter' && state.suggestions.length) {
      event.preventDefault();
      const selected = state.suggestions[state.activeSuggestionIndex] || state.suggestions[0];
      addTag(`#${selected.slug || selected.name || ''}`);
      input.value = '';
      closeSuggestions();
      return;
    }

    if (event.key === 'Enter' && input.value.trim()) {
      event.preventDefault();
      if (commitPendingInput()) {
        setStatus('Tag added.');
      }
      return;
    }

    if (event.key === ' ' && !input.value.trim()) {
      event.preventDefault();
    }
  });

  field.addEventListener('focusout', () => {
    window.setTimeout(() => {
      if (!field.contains(document.activeElement)) {
        closeSuggestions();
      }
    }, 0);
  });

  field.addEventListener('click', (event) => {
    if (!composer.contains(event.target) && !suggestions.contains(event.target)) {
      closeSuggestions();
    }
  });

  renderBadges();
  setTags(initialTags);

  return {
    commitPendingInput,
    reset,
    setTags,
  };
}
