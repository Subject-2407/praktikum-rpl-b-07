const WALLPAPER_STORE_KEY = 'scapes_contributor_wallpapers';

function readStorage() {
  try {
    return JSON.parse(localStorage.getItem(WALLPAPER_STORE_KEY) || '[]');
  } catch {
    return [];
  }
}

function writeStorage(wallpapers) {
  localStorage.setItem(WALLPAPER_STORE_KEY, JSON.stringify(wallpapers));
}

export const LocalWallpaperStorage = {
  getAll() {
    return readStorage();
  },

  saveAll(wallpapers) {
    writeStorage(wallpapers);
  },

  add(wallpaper) {
    const wallpapers = readStorage();
    const nextWallpaper = {
      id: wallpaper.id || `local-${crypto.randomUUID()}`,
      status: 'Pending',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      ...wallpaper,
    };

    writeStorage([nextWallpaper, ...wallpapers]);
    return nextWallpaper;
  },

  remove(id) {
    writeStorage(readStorage().filter((wallpaper) => String(wallpaper.id) !== String(id)));
  },
};
