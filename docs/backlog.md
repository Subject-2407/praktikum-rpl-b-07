# Product Backlog

### Effort Estimation (Story Points)
- 1 → Very small  
- 3 → Small  
- 5 → Medium  
- 8 → Large  
- 13 → Very complex  

### Dependencies
- Other story IDs (e.g., `US-01`)  
- External systems (e.g., `API Source`, `Storage`, `OS Integration`)  

---

## 👤 User

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| US-01 | Search wallpapers by keyword | User | Must Have | 5 SP | API Source | Core search feature |
| US-02 | Set wallpaper with one click | User | Must Have | 8 SP | US-01, OS Integration | Requires system access |
| US-03 | Auto save and organize wallpapers | User | Must Have | 5 SP | US-02 | Folder structure |
| US-04 | Customize download folder location | User | Should Have | 3 SP | US-03 | Settings feature |
| US-05 | Switch between wallpaper sources | User | Should Have | 8 SP | API Source | Multi-provider support |
| US-06 | Input personal API keys | User | Could Have | 5 SP | US-05 | Advanced users |
| US-07 | Show search recommendations and trending categories | User | Should Have | 5 SP | US-08, Scapes API | Replaces static/mock categories with data-driven recommendations |
| US-08 | Log search keywords for analytics | System | Must Have | 5 SP | US-01, Scapes API, Database | Foundation for trending categories and future ML recommender |

---

## Search Analytics & Recommendation

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| SA-01 | Create search log API endpoint | Backend | Must Have | 3 SP | US-08, Database | Accept normalized keyword events from frontend without blocking search |
| SA-02 | Store search log and privacy-safe metadata | Backend | Must Have | 5 SP | SA-01 | Do not store raw API keys, full local paths, email, or direct PII |
| SA-03 | Aggregate daily user-keyword categories | Backend | Should Have | 5 SP | SA-02, Category/Tag Metadata | Scheduled job groups user keywords into dynamic categories and keeps system category references when matched |
| SA-04 | Expose recommendation/trending endpoints | Backend | Should Have | 3 SP | SA-03 | Public read endpoint returns top 10 user-keyword categories by default, with custom limit and optional system categories |
| SA-05 | Replace frontend mock categories with API-driven data | Frontend User | Should Have | 5 SP | SA-04 | Show user-keyword categories above default categories; fallback to local default list when API fails |
| SA-06 | Prepare ML-ready dataset fields | Backend | Could Have | 3 SP | SA-02 | Keep normalized, timestamped, aggregated data so ML can be introduced later |
| SA-07 | Prioritize matching system categories while typing | Backend + Frontend User | Should Have | 3 SP | SA-04 | Only when `source_slug = scapes`; if query nearly matches a built-in category, e.g. `minimal` -> `Minimalist`, show that system category before trending/user-keyword recommendations |
| SA-08 | Add official tag autocomplete for `#` search | Backend + Frontend User | Should Have | 3 SP | `GET /tags`, SA-04 | Only when `source_slug = scapes`; if user types `#color`, show official tags such as `#colorful`; fallback to normal recommender if none match or source is external |
| SA-09 | Disable internal metadata priority for external sources | Backend + Frontend User | Must Have | 2 SP | SA-04, SA-07, SA-08 | When active source is external, do not prioritize Scapes system categories or official tags; use normal recommender behavior |

---

## Contributor Tag Proposal

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| TP-01 | Validate contributor `#tag` input | Contributor Frontend | Must Have | 2 SP | CO-01, CO-04 | Tags must start with `#` and can be separated by spaces |
| TP-01A | Show official tag suggestions in contributor form | Contributor Frontend | Must Have | 2 SP | TP-01, `GET /tags` | While contributor types a tag, show closest official tags so existing tags can be reused before creating a new proposal |
| TP-02 | Store pending tag proposals | Backend | Must Have | 3 SP | TP-01, Database | New tags are stored in proposal table while wallpaper is pending |
| TP-03 | Upsert tags on wallpaper approval | Backend/Admin | Must Have | 5 SP | TP-02, AD-01 | Insert non-duplicate tags into `tags`; reuse existing tag if slug already exists |
| TP-04 | Discard tag proposals on rejection | Backend/Admin | Should Have | 2 SP | TP-02, AD-01 | Rejected wallpaper tag proposals must not become official tags |

---

## 🎖️ Contributor

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| CO-01 | Upload wallpapers | Contributor | Must Have | 8 SP | Auth System, Storage | Core contributor feature |
| CO-02 | Track moderation status | Contributor | Should Have | 5 SP | CO-01, AD-01 | Dashboard feature |
| CO-03 | Delete wallpapers | Contributor | Should Have | 3 SP | CO-01 | Basic CRUD |
| CO-04 | Edit wallpaper details | Contributor | Could Have | 5 SP | CO-01 | Prefer editing before approval |
| CO-05 | Schedule wallpaper publication | Contributor | Won’t Have | 8 SP | CO-01, AD-01 | Not included in this version |
| CO-06 | Register account with email | Contributor | Must Have | 3 SP | Auth System | New contributor onboarding |
| CO-07 | Manage session (login/logout) | Contributor | Must Have | 3 SP | CO-06 | Session management |
| CO-08 | Request password reset via email | Contributor | Must Have | 3 SP | CO-06 | Account recovery |

---

## 👨🏻‍💻 Admin

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| AD-01 | Moderate wallpapers (approve/reject) | Admin | Must Have | 8 SP | CO-01 | Core quality control |
| AD-02 | Limit contributor uploads | Admin | Won’t Have | 5 SP | CO-01 | Not included in this version |
| AD-03 | Manage session (login/logout) via internal portal | Admin | Must Have | 5 SP | Auth System | Secure admin access |

---
