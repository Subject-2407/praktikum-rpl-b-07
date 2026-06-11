export function renderThemeToggle(positionClass = 'fixed right-5 top-6 z-50') {
  return `
    <button
      id="themeToggle"
      type="button"
      aria-label="Switch to dark mode"
      title="Switch to dark mode"
      class="${positionClass} flex h-9 w-9 cursor-pointer items-center justify-center rounded-full shadow-md transition-all duration-300 active:scale-95 focus:outline-none
      bg-yellow-300 border border-yellow-400 text-gray-800 hover:bg-yellow-400 
      dark:bg-slate-800 dark:border-slate-600 dark:text-yellow-300 dark:shadow-lg dark:hover:bg-slate-700"
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