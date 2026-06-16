import { request } from '../../core/http/api-client.js';

export const WallpaperApi = {
  listMine(query = {}) {
    return request('/me/wallpapers', {
      query,
    });
  },

  create(formData) {
    return request('/me/wallpapers', {
      method: 'POST',
      body: formData,
    });
  },

  update(id, payload) {
    return request(`/me/wallpapers/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      body: payload,
    });
  },

  delete(id) {
    return request(`/me/wallpapers/${encodeURIComponent(id)}`, {
      method: 'DELETE',
    });
  },

  categories() {
    return request('/categories');
  },

  tags(keyword = '', options = {}) {
    return request('/tags', {
      query: {
        q: keyword,
        match: options.match,
        limit: options.limit,
      },
    });
  },
};
