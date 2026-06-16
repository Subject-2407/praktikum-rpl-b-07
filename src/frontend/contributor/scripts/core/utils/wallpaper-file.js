export const MIN_WALLPAPER_RESOLUTION_BY_TARGET_DEVICE = {
  desktop: { width: 1920, height: 1080 },
  mobile: { width: 360, height: 800 },
  tablet: { width: 768, height: 1024 },
};

export function validateWallpaperFile(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const maxSize = 10 * 1024 * 1024;

  if (!file) {
    return 'Please choose a wallpaper file first.';
  }

  if (!allowedTypes.includes(file.type)) {
    return 'The file must be in JPG, PNG, or WebP format.';
  }

  if (file.size > maxSize) {
    return 'The maximum file size is 10MB.';
  }

  return '';
}

export function detectWallpaperTargetDevice(width, height) {
  const ratio = height === 0 ? 1 : Number((width / height).toFixed(2));

  if (ratio >= 1.5) {
    return 'desktop';
  }

  if (ratio <= 0.6) {
    return 'mobile';
  }

  return 'tablet';
}

export function getMinimumWallpaperResolution(targetDevice) {
  return MIN_WALLPAPER_RESOLUTION_BY_TARGET_DEVICE[targetDevice]
    || MIN_WALLPAPER_RESOLUTION_BY_TARGET_DEVICE.desktop;
}

export function validateWallpaperDimensions(width, height) {
  const targetDevice = detectWallpaperTargetDevice(width, height);
  const minimumResolution = getMinimumWallpaperResolution(targetDevice);
  const isValid = width >= minimumResolution.width && height >= minimumResolution.height;

  return {
    targetDevice,
    minimumResolution,
    isValid,
    error: isValid
      ? ''
      : `Minimum resolution for ${targetDevice} is ${minimumResolution.width}x${minimumResolution.height}px.`,
  };
}
