# scapes-db

MySQL schema and migrations for [Scapes](https://scapes.my.id).

## Requirements

- MySQL 8.0+

## Files

```text
.
├── scapes_db.sql        # Full schema — create from scratch
└── migrations/          # Incremental changes applied on top of the base schema
    ├── 20260613_update_wallpaper_resolution_constraints.sql
    └── 20260616_add_search_analytics_and_tag_proposals.sql
```

## Setup

**Fresh install** — import the full schema:

```bash
mysql -u root -p < scapes_db.sql
```

This creates the `scapes` database, all tables, constraints, indexes, and seeds the initial `api_sources`, `categories`, and `tags` data.

**Existing install** — apply migrations in order:

```bash
mysql -u root -p scapes < migrations/20260613_update_wallpaper_resolution_constraints.sql
mysql -u root -p scapes < migrations/20260616_add_search_analytics_and_tag_proposals.sql
```

Always run migrations in filename order (they are prefixed by date).

## Schema Overview

| Table | Description |
|---|---|
| `users` | Registered accounts — contributors and admins, distinguished by `role` |
| `email_verifications` | Email verification tokens issued on contributor registration |
| `password_resets` | Password reset tokens — single-use, 24-hour TTL |
| `login_attempts` | Login attempt log for brute-force detection (max 5 consecutive failures) |
| `categories` | Wallpaper categories — slug is used as the local storage sub-folder name |
| `tags` | Free-form tags for keyword search |
| `wallpapers` | Core content entity — metadata, target device, and moderation status |
| `wallpaper_tags` | Many-to-many junction between `wallpapers` and `tags` |
| `moderation_reviews` | Moderation decision history — `reason` is required at the application layer when `decision = rejected` |
| `api_sources` | External wallpaper sources: Scapes (default), Pexels, Unsplash, Pixabay |
| `audit_logs` | Audit trail for important actions — `user_id` is `NULL` for automated system actions |
| `search_logs` | Privacy-safe search log used for recommendations and trending |
| `daily_trending_categories` | Daily aggregation of trending categories derived from search keywords |
| `wallpaper_tag_proposals` | Contributor tag proposals processed during wallpaper moderation |

## Seed Data

`scapes_db.sql` includes seed data for:

- **`api_sources`** — Scapes (default), Unsplash, Pexels, Pixabay
- **`categories`** — Minimalist, Nature, Abstract, Architecture, Dark, Space, Anime, Technology
- **`tags`** — dark, light, neon, pastel, 4k, monochrome, colorful, gradient, retro, futuristic

## Adding a Migration

Name the file `YYYYMMDD_short_description.sql` and place it in `migrations/`. The file should be idempotent where possible (use `IF NOT EXISTS`, `IF EXISTS`, etc.).
