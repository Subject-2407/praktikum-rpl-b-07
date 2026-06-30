# scapes-admin

Admin moderation portal for [Scapes](https://scapes.my.id). Admins review wallpaper submissions from contributors and approve or reject them with a reason.

## Tech Stack

- HTML5 / Vanilla JavaScript
- Tailwind CSS (via standalone CLI)
- jQuery

## Pages

| File | Purpose |
|---|---|
| `login.html` | Admin login |
| `index.html` | Moderation queue dashboard |
| `review.html` | Individual wallpaper review — approve or reject |

Page flow: `login.html` → `index.html` → `review.html`

## Architecture

Layered JavaScript following Clean Architecture:

```text
.
├── index.html
├── login.html
├── review.html
├── styles/
│   ├── input.css
│   └── output.css
└── scripts/
    ├── app.js
    ├── login.js
    ├── core/
    │   ├── http/api-client.js
    │   └── storage/session-storage.js
    ├── data/
    │   ├── api/
    │   │   ├── auth-api.js
    │   │   └── wallpaper-api.js
    │   └── repositories/
    │       ├── auth-repository.js
    │       └── wallpaper-repository.js
    ├── domain/
    │   └── use-cases/
    │       ├── login-admin.js
    │       ├── logout-admin.js
    │       ├── approve-wallpaper.js
    │       ├── reject-wallpaper.js
    │       ├── list-wallpapers.js
    │       └── get-wallpaper.js
    ├── presentation/
    │   ├── components/status-badge.js
    │   └── pages/
    │       ├── login-page.js
    │       ├── queue-page.js
    │       └── review-page.js
    └── config/environment.js
```

## Local Setup

**1. Start the backend API** (from [scapes-backend](https://github.com/scapes-app/scapes-backend)):

```bash
php -S localhost:8000 router.php
```

**2. Start the admin frontend:**

```bash
php -S localhost:4174
```

**3. Open in browser:**

```text
http://localhost:4174/login.html
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

The default base URL points to `http://localhost:8000`. To change it, update `scripts/config/environment.js`.

For manual testing, you can inject a token directly:

```js
localStorage.setItem('scapes.admin.token', '<jwt-token>');
```
