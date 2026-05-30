/**
 * Mock wallpaper data for frontend testing without real API
 * Match to JSON structure of GET /moderation/wallpapers
 */

export const MOCK_WALLPAPERS = [
    {
        id: 1,
        title: "Mountain Sunrise",
        description: "A beautiful golden sunrise over snow-capped mountains",
        status: "pending",
        file_path: "/uploads/wallpapers/nature/mountain-sunrise.jpg",
        tags: [
            { id: 1, name: "nature", slug: "nature" },
            { id: 2, name: "landscape", slug: "landscape" },
            { id: 3, name: "sunrise", slug: "sunrise" },
            { id: 4, name: "4k", slug: "4k" }
        ],
        created_at: "2026-03-01T08:30:00Z",
        contributor: { id: 101, email: "alice@example.com" }
    },
    {
        id: 2,
        title: "Dark Minimalist Abstract",
        description: "Minimalist abstract design with dark tones",
        status: "pending",
        file_path: "/uploads/wallpapers/abstract/dark-minimal.jpg",
        tags: [
            { id: 5, name: "abstract", slug: "abstract" },
            { id: 6, name: "dark", slug: "dark" },
            { id: 7, name: "minimalist", slug: "minimalist" }
        ],
        created_at: "2026-03-02T10:15:00Z",
        contributor: { id: 102, email: "bob@example.com" }
    },
    {
        id: 3,
        title: "Neon Cyberpunk City",
        description: "Futuristic city with neon lights and cyberpunk aesthetic",
        status: "pending",
        file_path: "/uploads/wallpapers/technology/neon-city.jpg",
        tags: [
            { id: 8, name: "neon", slug: "neon" },
            { id: 9, name: "futuristic", slug: "futuristic" },
            { id: 10, name: "cyberpunk", slug: "cyberpunk" }
        ],
        created_at: "2026-03-03T14:45:00Z",
        contributor: { id: 103, email: "charlie@example.com" }
    },
    {
        id: 4,
        title: "Forest Canopy",
        description: "Lush green forest canopy viewed from below",
        status: "pending",
        file_path: "/uploads/wallpapers/nature/forest-canopy.jpg",
        tags: [
            { id: 1, name: "nature", slug: "nature" },
            { id: 11, name: "forest", slug: "forest" },
            { id: 12, name: "green", slug: "green" }
        ],
        created_at: "2026-03-04T09:20:00Z",
        contributor: { id: 104, email: "diana@example.com" }
    },
    {
        id: 5,
        title: "Pastel Gradient Sunset",
        description: "Soft pastel colors blending in a gradient sunset",
        status: "pending",
        file_path: "/uploads/wallpapers/abstract/pastel-sunset.jpg",
        tags: [
            { id: 13, name: "pastel", slug: "pastel" },
            { id: 14, name: "gradient", slug: "gradient" },
            { id: 3, name: "sunset", slug: "sunset" }
        ],
        created_at: "2026-03-05T16:00:00Z",
        contributor: { id: 105, email: "eve@example.com" }
    },
    {
        id: 6,
        title: "Space Galaxy",
        description: "Stunning view of a distant galaxy in space",
        status: "approved",
        file_path: "/uploads/wallpapers/space/galaxy.jpg",
        tags: [
            { id: 15, name: "space", slug: "space" },
            { id: 16, name: "galaxy", slug: "galaxy" },
            { id: 17, name: "astronomy", slug: "astronomy" }
        ],
        created_at: "2026-02-28T12:00:00Z",
        contributor: { id: 106, email: "frank@example.com" }
    },
    {
        id: 7,
        title: "Urban Architecture",
        description: "Modern urban buildings and architecture",
        status: "rejected",
        file_path: "/uploads/wallpapers/urban/architecture.jpg",
        tags: [
            { id: 18, name: "urban", slug: "urban" },
            { id: 19, name: "architecture", slug: "architecture" },
            { id: 20, name: "buildings", slug: "buildings" }
        ],
        created_at: "2026-02-27T11:30:00Z",
        contributor: { id: 107, email: "grace@example.com" }
    }
];

export function getMockWallpaper(id) {
    return MOCK_WALLPAPERS.find(w => w.id === parseInt(id));
}

export function getMockWallpapersByStatus(status) {
    return MOCK_WALLPAPERS.filter(w => w.status === status);
}

export function getAllMockWallpapers() {
    return MOCK_WALLPAPERS;
}