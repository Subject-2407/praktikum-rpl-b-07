# Scapes

> Browse, download, and apply wallpapers directly from the internet — no browser needed. One click and it's on your desktop.

Scapes is a Kotlin Multiplatform wallpaper manager for Windows and Android. It also ships a contributor portal for uploading wallpapers and an admin portal for moderation.

## Repositories

| Repo | Description |
|---|---|
| [scapes-app](https://github.com/scapes-app/scapes-app) | Kotlin Multiplatform app — Windows & Android |
| [scapes-backend](https://github.com/scapes-app/scapes-backend) | PHP REST API — auth, wallpapers, moderation |
| [scapes-contributor](https://github.com/scapes-app/scapes-contributor) | Web portal for contributors |
| [scapes-admin](https://github.com/scapes-app/scapes-admin) | Web dashboard for admins |
| [scapes-landing](https://github.com/scapes-app/scapes-landing) | Public landing and download page |
| [scapes-db](https://github.com/scapes-app/scapes-db) | MySQL schema and migrations |

## Features

**Contributor Portal**
1. Register and verify account via email
2. Login and logout
3. Reset password via email link
4. Upload wallpapers (JPG, PNG, WebP — max 10 MB, min 1920×1080)
5. Track moderation status per wallpaper (pending, approved, rejected)
6. Delete submitted wallpapers
7. Contributor insights — views and downloads per wallpaper
8. Profile page

**Admin Portal**
9. Dashboard — summary of pending, approved, and rejected wallpapers
10. Review and approve or reject wallpaper submissions with a reason

## Tech Stack

| Layer | Technology |
|---|---|
| Desktop & Mobile App | Kotlin Multiplatform, Compose Multiplatform |
| Contributor & Admin Web | HTML / JavaScript / Tailwind CSS / jQuery |
| Backend API | PHP 8.1, MySQL, Redis, JWT |
| Infrastructure | SMTP (email), PDO, PHPUnit, PHPStan |

## Live

| Portal | URL |
|---|---|
| User app | _coming soon_ |
| Contributor | [contributor.scapes.my.id](https://contributor.scapes.my.id) |
| Admin | [admin.scapes.my.id](https://admin.scapes.my.id) |

## Team

| Name | NIM | Role |
|---|---|---|
| Alifa Fitra Faiha | L0124036 | Support Developer & QA |
| Allia Nur Shafira | L0124037 | Front-End & Design |
| Bintang A'raaf Stevan Putra | L0124091 | Lead Developer |
| Allyssa Hatitya Pratiwi | L0124146 | Documentation & QA |

## Screenshots

| Feature | Preview |
|---|---|
| Contributor — Register | ![Register](docs/images/contributorRegister.png) |
| Contributor — Login | ![Login](docs/images/contributorLogin.png) |
| Contributor — Reset Password | ![Reset Password](docs/images/contributorReset.png) |
| Contributor — Upload Wallpaper | ![Upload](docs/images/contributorUpload.png) |
| Contributor — Moderation Status | ![Status](docs/images/contributorModerasi.png) |
| Contributor — Delete Wallpaper | ![Delete](docs/images/contributorDelete.png) |
| Contributor — Insights | ![Insights](docs/images/contributorInsight.png) |
| Contributor — Profile | ![Profile](docs/images/contributorProfile.png) |
| Admin — Dashboard | ![Dashboard](docs/images/adminDashboard.png) |
| Admin — Review Wallpaper | ![Review](docs/images/adminReview.png) |
