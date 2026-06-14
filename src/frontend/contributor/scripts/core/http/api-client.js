import { Environment } from '../../config/environment.js';

function buildUrl(path, query = {}) {
  const url = new URL(`${Environment.apiBaseUrl.replace(/\/$/, '')}${path}`);

  Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .forEach(([key, value]) => {
      if (Array.isArray(value)) {
        value
          .filter((entry) => entry !== undefined && entry !== null && entry !== '')
          .forEach((entry) => url.searchParams.append(key, entry));
        return;
      }

      url.searchParams.set(key, value);
    });

  return url.toString();
}

function createHttpError(message, status) {
  const error = new Error(message);
  error.status = status;
  return error;
}

export async function request(path, options = {}) {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), Environment.requestTimeoutMs);
  const headers = new Headers(options.headers || {});
  const isFormData = options.body instanceof FormData;

  if (options.body && !isFormData) {
    headers.set('Content-Type', 'application/json');
  }

  try {
    const response = await fetch(buildUrl(path, options.query), {
      method: options.method || 'GET',
      body: isFormData ? options.body : options.body ? JSON.stringify(options.body) : undefined,
      headers,
      credentials: 'include',
      signal: controller.signal,
    });
    const contentType = response.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await response.json() : await response.text();

    if (response.status === 401 || response.status === 403) {
      if (!options.suppressUnauthorizedEvent) {
        window.dispatchEvent(new CustomEvent('scapes:unauthorized'));
      }

      throw createHttpError(
        data?.message || 'Sesi berakhir. Silakan login ulang.',
        response.status,
      );
    }

    if (!response.ok) {
      throw createHttpError(data?.message || 'Request gagal diproses.', response.status);
    }

    return data;
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error('Request timeout. Coba beberapa saat lagi.');
    }

    if (error instanceof TypeError) {
      throw new Error('Tidak dapat terhubung ke API Scapes.');
    }

    if (error instanceof Error) {
      throw error;
    }

    throw new Error('Tidak dapat terhubung ke API Scapes.');
  } finally {
    window.clearTimeout(timeout);
  }
}
