import { Wallpaper, createWallpaperCollection } from '../../domain/entities/wallpaper.js';
import { WallpaperApi } from '../api/wallpaper-api.js';

const LIST_CACHE_TTL_MS = 2 * 60 * 1000;
const listCache = new Map();

function createCacheKey(query = {}) {
  return Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([key, value]) => `${key}:${Array.isArray(value) ? value.join(',') : value}`)
    .join('|');
}

function readListCache(query) {
  const cacheKey = createCacheKey(query);
  const cached = listCache.get(cacheKey);

  if (!cached || Date.now() - cached.createdAt > LIST_CACHE_TTL_MS) {
    listCache.delete(cacheKey);
    return null;
  }

  return cached.value;
}

function writeListCache(query, value) {
  listCache.set(createCacheKey(query), {
    createdAt: Date.now(),
    value,
  });
}

function extractCollection(data) {
  const collection = Array.isArray(data) ? data : data?.data || data?.wallpapers || [];
  return createWallpaperCollection(collection);
}

function extractListResult(data) {
  return {
    items: extractCollection(data),
    meta: data?.meta || {
      current_page: 1,
      per_page: data?.data?.length || 0,
      total: data?.data?.length || 0,
      last_page: 1,
    },
  };
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
    invalidateContributorWallpapersCache() {
      listCache.clear();
    },

    async listContributorWallpaperPage(filters = {}) {
      const query = {
        page: 1,
        per_page: 20,
        ...filters,
      };
      const force = Boolean(query.force);
      delete query.force;

      if (!force) {
        const cached = readListCache(query);
        if (cached) {
          return {
            ...cached,
            fromCache: true,
          };
        }
      }

      const result = extractListResult(await wallpaperApi.listMine(query));
      writeListCache(query, result);

      return {
        ...result,
        fromCache: false,
      };
    },

    async listContributorWallpapers(filters = {}) {
      const result = await this.listContributorWallpaperPage({
        page: 1,
        per_page: 100,
        ...filters,
      });
      return result.items;
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
      this.invalidateContributorWallpapersCache();
      return { source: 'remote', wallpaper: new Wallpaper(extractItem(response)) };
    },

    async updateMetadata(id, payload) {
      const response = await wallpaperApi.update(id, payload);
      this.invalidateContributorWallpapersCache();
      const wallpaper = await this.getWallpaper(id);
      return wallpaper || new Wallpaper(extractItem(response));
    },

    async delete(id) {
      await wallpaperApi.delete(id);
      this.invalidateContributorWallpapersCache();
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
