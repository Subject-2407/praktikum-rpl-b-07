export function renderThemeToggle(positionClass = 'absolute right-4 top-4 z-30 sm:right-6 sm:top-6') {
  return `
    <button
      id="themeToggle"
      type="button"
      aria-label="Switch to dark mode"
      title="Switch to dark mode"
      class="${positionClass} flex h-10 w-10 cursor-pointer items-center justify-center rounded-full border border-black/8 bg-white/35 text-gray-500 backdrop-blur-sm transition-all duration-300 hover:bg-white/60 hover:text-gray-700 active:scale-95 focus:outline-none dark:border-white/10 dark:bg-white/[0.04] dark:text-gray-400 dark:hover:bg-white/[0.08] dark:hover:text-gray-200"
    >
      <span id="sunIcon" class="hidden text-sm" aria-hidden="true">
        <i class="fa-solid fa-sun"></i>
      </span>
      <span id="moonIcon" class="text-sm" aria-hidden="true">
        <i class="fa-solid fa-moon"></i>
      </span>
    </button>
  `;
}
