export async function submitWallpaper(wallpaperRepository, submission) {
  const title = String(submission?.title || '').trim();
  const categoryId = String(submission?.categoryId || '').trim();
  const description = String(submission?.description || '').trim();
  const categoryName = String(submission?.categoryName || categoryId).trim();
  const tags = Array.isArray(submission?.tags)
    ? submission.tags.map((tag) => String(tag).trim()).filter(Boolean).slice(0, 15)
    : [];

  if (!title) {
    throw new Error('Judul wallpaper wajib diisi.');
  }

  if (!categoryId) {
    throw new Error('Kategori wajib dipilih.');
  }

  return wallpaperRepository.submit({
    formData: submission.formData,
    draft: {
      title,
      description,
      categoryId,
      categoryName,
      tags,
    },
  });
}
