# scapes-contributor

Contributor web portal for [Scapes](https://scapes.my.id). Contributors can register, upload wallpapers, track moderation status, and manage their submissions.

## Tech Stack

- HTML5 / Vanilla JavaScript
- Tailwind CSS (via standalone CLI)
- jQuery

## Features

- Register and verify account via email
- Login / logout
- Reset password via email link
- Upload wallpapers (JPG, PNG, WebP — max 10 MB, min 1920×1080 for desktop)
- Track moderation status per wallpaper (pending, approved, rejected)
- View rejection reasons
- Edit wallpaper metadata (title, description, category, tags)
- Delete submitted wallpapers
- Contributor insights (view count, download count per wallpaper)
- Profile page

## Architecture

Layered JavaScript following Clean Architecture:

```text
.
├── index.html
├── ...other pages
├── favicon.ico
├── styles/
├── scripts/
│   ├── core/
│   │   ├── http/api-client.js
│   │   └── storage/session-storage.js
│   ├── data/
│   │   ├── api/
│   │   └── repositories/
│   ├── domain/
│   │   └── use-cases/
│   ├── presentation/
│   │   ├── components/
│   │   └── pages/
│   └── config/environment.js
└── assets/
```

## Local Setup

**1. Start the backend API** (from [scapes-backend](https://github.com/scapes-app/scapes-backend)):

```bash
php -S localhost:8000 router.php
```

**2. Start the contributor portal:**

```bash
php -S localhost:4173
```

**3. Open in browser:**

```text
http://localhost:4173
```

## Tailwind CSS

Build CSS once:

```bash
./tailwindcss -i ./styles/input.css -o ./styles/output.css --minify
```

Watch mode during development:

```bash
./tailwindcss -i ./styles/input.css -o ./styles/output.css --watch
```

## API Configuration

Update `scripts/config/environment.js` to change the backend base URL. The default points to `http://localhost:8000`.

## Live

[contributor.scapes.my.id](https://contributor.scapes.my.id)
