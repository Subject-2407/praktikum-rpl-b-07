# Scapes Admin Frontend

Alur halaman:

```text
login.html -> index.html -> review.html
```

Admin harus login lebih dulu. Setelah token tersimpan,
Admin bisa masuk ke dashboard moderasi.

## Halaman

| File | Fungsi |
|---|---|
| `login.html` | Form login admin |
| `index.html` | Dashboard moderation queue |
| `review.html` | Review detail wallpaper, approve/reject |

## Use Cases

| Halaman | User Story |
|---|---|
| login.html | AD-03 Manage Admin Session |
| index.html | AD-01 Moderate Wallpapers |
| review.html | AD-01 Moderate Wallpapers |

## Struktur

```text
src/frontend/admin/
  index.html
  login.html
  review.html
  styles/
    input.css
    output.css
  scripts/
    app.js
    login.js
    core/
      http/
        api-client.js
      storage/
        session-storage.js
    data/
      api/
        auth-api.js
        wallpaper-api.js
      repositories/
        auth-repository.js
        wallpaper-repository.js
    domain/
      use-cases/
        login-admin.js
        logout-admin.js
        approve-wallpaper.js
        reject-wallpaper.js
        list-wallpapers.js
        get-wallpaper.js
    presentation/
      components/
        status-badge.js
      pages/
        login-page.js
        queue-page.js
        review-page.js
    config/
      environment.js
```

## Tailwind Standalone CLI

Generate CSS dari folder admin:

```powershell
.\tailwindcss.exe -i .\styles\input.css -o .\styles\output.css --minify
```

Saat development:

```powershell
.\tailwindcss.exe -i .\styles\input.css -o .\styles\output.css --watch
```

## Menjalankan Lokal

Jalankan backend API dari folder `src/backend`:

```powershell
php -S localhost:8000 router.php
```

Jalankan frontend admin dari folder `src/frontend/admin`:

```powershell
php -S localhost:4174
```

Buka:

```text
http://localhost:4174/login.html
```

## Konfigurasi API

Default base URL:

```text
http://localhost:8000
```

Token disimpan setelah login. Untuk testing manual:

```js
localStorage.setItem('scapes.admin.token', '<jwt-token>');
```