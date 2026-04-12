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

---

## 🎖️ Contributor

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| CO-01 | Upload wallpapers | Contributor | Must Have | 8 SP | Auth System, Storage | Core contributor feature |
| CO-02 | Track moderation status | Contributor | Should Have | 5 SP | CO-01, AD-01 | Dashboard feature |
| CO-03 | Delete wallpapers | Contributor | Should Have | 3 SP | CO-01 | Basic CRUD |
| CO-04 | Edit wallpaper details | Contributor | Could Have | 5 SP | CO-01 | Prefer editing before approval |
| CO-05 | Schedule wallpaper publication | Contributor | Won’t Have | 8 SP | CO-01, AD-01 | Not included in this version |

---

## 👨🏻‍💻 Admin

| ID Story | Description | Role | MoSCoW Priority | Effort Estimation | Dependencies | Notes |
|----------|------------|------|------------------|-------------------|--------------|--------|
| AD-01 | Moderate wallpapers (approve/reject) | Admin | Must Have | 8 SP | CO-01 | Core quality control |
| AD-02 | Limit contributor uploads | Admin | Won’t Have | 5 SP | CO-01 | Not included in this version |

---