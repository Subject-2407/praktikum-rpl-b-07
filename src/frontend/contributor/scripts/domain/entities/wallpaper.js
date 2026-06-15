export function normalizeWallpaper(raw = {}) {
  const moderation = raw.moderation && typeof raw.moderation === 'object'
    ? raw.moderation
    : null;
  const category = raw.category && typeof raw.category === 'object'
    ? raw.category
    : null;
  const dimensions = raw.dimensions && typeof raw.dimensions === 'object'
    ? raw.dimensions
    : {};
  const metadata = raw.metadata && typeof raw.metadata === 'object'
    ? raw.metadata
    : {};
  const width = Number(
    raw.width
      || raw.image_width
      || raw.original_width
      || dimensions.width
      || metadata.width
      || 0,
  );
  const height = Number(
    raw.height
      || raw.image_height
      || raw.original_height
      || dimensions.height
      || metadata.height
      || 0,
  );

  return {
    id: raw.id,
    title: raw.title || 'Untitled wallpaper',
    description: raw.description || '',
    categoryId: Number(raw.category_id || category?.id || 0) || null,
    category: category?.name || raw.category || 'Uncategorized',
    categorySlug: category?.slug || raw.category_slug || '',
    tags: Array.isArray(raw.tags) ? raw.tags : [],
    status: raw.status || 'Pending',
    targetDevice: raw.targetDevice || raw.target_device || '',
    moderation,
    rejectionReason: raw.rejectionReason || raw.rejection_reason || moderation?.reason || '',
    createdAt: raw.createdAt || raw.created_at || new Date().toISOString(),
    updatedAt: raw.updatedAt || raw.updated_at || raw.created_at || new Date().toISOString(),
    fileUrl: raw.fileUrl || raw.file_url || raw.file_path || '',
    thumbnailUrl: raw.thumbnailUrl || raw.thumbnail_url || raw.thumbnail_path || '',
    previewUrl: raw.previewUrl || raw.preview_url || raw.file_path || raw.thumbnail_path || '',
    fileSizeKb: Number(raw.fileSizeKb || raw.file_size_kb || 0) || 0,
    mimeType: raw.mimeType || raw.mime_type || '',
    width,
    height,
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

  get isPending() {
    return String(this.status).toLowerCase() === 'pending';
  }
}

export function createWallpaperCollection(items = []) {
  return items.map((item) => new Wallpaper(item));
}
