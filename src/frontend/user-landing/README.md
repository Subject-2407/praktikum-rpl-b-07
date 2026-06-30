# scapes-landing

Public marketing and download page for [Scapes](https://scapes.my.id) — a desktop wallpaper browser and setter for Windows and Android.

## Tech Stack

- HTML5 / CSS / JavaScript
- Tailwind CSS (via standalone CLI)

## Local Setup

Open `index.html` directly in a browser, or serve it with any static file server:

```bash
npx serve .
# or
python -m http.server 3000
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
