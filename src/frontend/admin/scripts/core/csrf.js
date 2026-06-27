const CSRF_COOKIE_NAME = 'scapes_csrf_token';
const CSRF_HEADER_NAME = 'X-CSRF-Token';

export function getCsrfToken() {
    return document.cookie
        .split('; ')
        .find((row) => row.startsWith(`${CSRF_COOKIE_NAME}=`))
        ?.split('=')
        .slice(1)
        .join('=') || '';
}

export function csrfHeader() {
    const token = getCsrfToken();

    return token ? { [CSRF_HEADER_NAME]: decodeURIComponent(token) } : {};
}
