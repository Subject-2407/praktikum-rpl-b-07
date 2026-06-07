import { Wallpaper, createWallpaperCollection } from '../../domain/entities/wallpaper.js';
import { WallpaperApi } from '../api/wallpaper-api.js';

function extractCollection(data) {
  const collection = Array.isArray(data) ? data : data?.data || data?.wallpapers || [];
  return createWallpaperCollection(collection);
}

function extractItem(data) {
  return data?.data || data?.wallpaper || data || null;
}

function normalizeCategories(data) {
  return Array.isArray(data) ? data : data?.data || data?.categories || [];
}

function normalizeTags(data) {
  return Array.isArray(data) ? data : data?.data || data?.tags || [];
}

export function createWallpaperRepository({ wallpaperApi = WallpaperApi } = {}) {
  return {
    async listContributorWallpapers(filters = {}) {
      return extractCollection(await wallpaperApi.listMine({
        page: 1,
        per_page: 100,
        ...filters,
      }));
    },

    async getWallpaper(id) {
      const wallpapers = extractCollection(await wallpaperApi.listMine({
        page: 1,
        per_page: 100,
      }));
      const wallpaper = wallpapers.find((item) => String(item.id) === String(id));
      return wallpaper || null;
    },

    async submit({ formData }) {
      const response = await wallpaperApi.create(formData);
      return { source: 'remote', wallpaper: new Wallpaper(extractItem(response)) };
    },

    async updateMetadata(id, payload) {
      const response = await wallpaperApi.update(id, payload);
      return new Wallpaper(extractItem(response));
    },

    async delete(id) {
      await wallpaperApi.delete(id);
      return { success: true, source: 'remote' };
    },

    async getCategories() {
      return normalizeCategories(await wallpaperApi.categories());
    },

    async getTags(keyword) {
      return normalizeTags(await wallpaperApi.tags(keyword));
    },
  };
}

export const wallpaperRepository = createWallpaperRepository();
