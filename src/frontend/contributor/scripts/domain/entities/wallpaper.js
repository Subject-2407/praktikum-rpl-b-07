export function normalizeWallpaper(raw = {}) {
  const moderation = raw.moderation && typeof raw.moderation === 'object'
    ? raw.moderation
    : null;

  return {
    id: raw.id,
    title: raw.title || 'Untitled wallpaper',
    description: raw.description || '',
    category: raw.category?.name || raw.category || 'Uncategorized',
    tags: Array.isArray(raw.tags) ? raw.tags : [],
    status: raw.status || 'Pending',
    targetDevice: raw.targetDevice || raw.target_device || '',
    moderation,
    rejectionReason: raw.rejectionReason || raw.rejection_reason || moderation?.reason || '',
    createdAt: raw.createdAt || raw.created_at || new Date().toISOString(),
    updatedAt: raw.updatedAt || raw.updated_at || raw.created_at || new Date().toISOString(),
    previewUrl: raw.previewUrl || raw.preview_url || raw.file_path || '',
    isReviewOverdue: Boolean(raw.isReviewOverdue || raw.is_review_overdue),
  };
}

export class Wallpaper {
  constructor(payload) {
    Object.assign(this, normalizeWallpaper(payload));
  }

  get isRejected() {
    return String(this.status).toLowerCase() === 'rejected';
  }
}

export function createWallpaperCollection(items = []) {
  return items.map((item) => new Wallpaper(item));
}
