# User Stories
### 1. 👤 User
  - [US-01](#us-01)
  - [US-02](#us-02)
  - [US-03](#us-03)
### 2. 🎖️ Contributor
  - Soon
### 3. 👨🏻‍💻 Admin
  - Soon

---

## 👤 User

| ID | Story Description | Acceptance Criteria |
|----|------------|---------------------|
| <a name="us-01"></a>**US-01** | **As a** user, **I want to** search for wallpapers by keywords, **so that** I can find specific themes immediately without browsing manually. | **AC-1**: Given the user is on the Search tab, when they type "Minimalist" and press Enter, then the gallery should only display images tagged with "minimalist". <br><br> **AC-2**: Given the user types a nonsensical keyword (e.g., "xyz123"), when they search, then the system should display a "No wallpapers found" message instead of a blank screen. <br><br> **AC-3** *(Edge case)*: Given the user enters symbols like `@#$%`, when searching, then the system should sanitize the input or return no results without crashing the application.|
| <a name="us-02"></a>**US-02** | **As a** user, **I want to** set a wallpaper directly with one click, **so that** the app automatically downloads and applies it without me needing to open OS settings or file explorer. | **AC-1**: Given the user is viewing the gallery, when they click a wallpaper, then the system should download the file to the local folder and immediately update the desktop background. <br><br> **AC-2**: Given the update process is running, when the wallpaper is successfully applied, then a toast notification "Wallpaper updated successfully" should appear. <br><br> **AC-3** *(Edge case)*: Given the wallpaper file was previously downloaded but then deleted manually by the user, when the user clicks it in the gallery, then the system should show "Source file not found". <br><br> **AC-4** *(Edge case)*: Given the user clicks a wallpaper but the internet connection is lost, when the download fails, then the system should show an error "Download failed, please check your connection" and the current wallpaper should not change.  |
| <a name="us-03"></a>**US-03** | **As a** user, **I want** my downloaded wallpapers to be automatically saved and sorted into a specific local folder, **so that** my files stay organized without manual effort. | **AC-1**: Given the user downloads a new wallpaper, when the download is complete, then the file should be moved automatically to a specific local folder. <br><br> **AC-2**: Given the system downloads an image, when saving, then the file should be placed in a sub-folder based on its category (e.g.,`/Scapes/minimalist/`). <br><br> **AC-3** *(Edge case)*: Given the local storage is full, when the user attempts to download, then the system should cancel the process and notify the user to free up space.