const LIGHT_LOGO_SRC = '/assets/scapes-light.png';
const DARK_LOGO_SRC = '/assets/scapes-dark.png';

export function renderBrandLogo({
  href = '/dashboard',
  containerClass = '',
  imageClass = 'h-10 w-auto',
  title = 'Scapes',
  subtitle = '',
  titleClass = 'font-heading text-lg font-bold',
  subtitleClass = 'text-s text-body-muted',
  useDarkBackgroundLogo = false,
} = {}) {
  const logoMarkup = useDarkBackgroundLogo
    ? `<img src="${DARK_LOGO_SRC}" alt="Scapes" class="${imageClass}">`
    : `
      <img src="${LIGHT_LOGO_SRC}" alt="Scapes" class="${imageClass} dark:hidden">
      <img src="${DARK_LOGO_SRC}" alt="Scapes" class="hidden ${imageClass} dark:block">
    `;

  const textMarkup = title || subtitle
    ? `
      <span class="min-w-0">
        ${title ? `<span class="block ${titleClass}">${title}</span>` : ''}
        ${subtitle ? `<span class="block ${subtitleClass}">${subtitle}</span>` : ''}
      </span>
    `
    : '';

  return `
    <a href="${href}" class="${containerClass}">
      <span class="shrink-0">${logoMarkup}</span>
      ${textMarkup}
    </a>
  `;
}
