/**
 * Queue/Index page initialization
 * Renders wallpapers table and stats cards
 */
import { requireAuth } from "./core/auth-guard.js";
import { getAllMockWallpapers } from "./data/mock/mock-wallpapers.js";

export async function bootstrapQueuePage() {
    // check auth
    const isAuth = await requireAuth("./login.html");
    if (!isAuth) return;

    // unhide page if authorized
    const appContent = document.getElementById("app-content");
    if (appContent) {
        appContent.style.display = "flex";
    }

    let wallpapers = [];
    let counts = { total: 0, pending: 0, done: 0 };
    
    // DEV_MODE toggle for testing without backend. Set to false to fetch real data.
    const DEV_MODE = false; 

    try {
        if (DEV_MODE) {
            console.log("[DEV MODE] Loading mock data...");
            wallpapers = getAllMockWallpapers();
            counts.total = wallpapers.length;
            counts.pending = wallpapers.filter(w => w.status === 'pending').length;
            counts.done = wallpapers.filter(w => w.status === 'approved' || w.status === 'rejected').length;
        } else {
            console.log("Fetching real data from backend...");
            const baseUrl = 'http://localhost:8000/moderation/wallpapers';
            const fetchOpts = {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            };

            const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
                fetch(`${baseUrl}?status=pending`, fetchOpts),
                fetch(`${baseUrl}?status=approved`, fetchOpts),
                fetch(`${baseUrl}?status=rejected`, fetchOpts)
            ]);

            if (!pendingRes.ok) throw new Error("Failed to fetch pending queue");

            const pendingJson = await pendingRes.json();
            const approvedJson = await approvedRes.json();
            const rejectedJson = await rejectedRes.json();

            const pendingTotal = pendingJson.meta?.total || 0;
            const approvedTotal = approvedJson.meta?.total || 0;
            const rejectedTotal = rejectedJson.meta?.total || 0;

            counts.total = pendingTotal + approvedTotal + rejectedTotal;
            counts.pending = pendingTotal;
            counts.done = approvedTotal + rejectedTotal;

            wallpapers = pendingJson.data || [];
        }
    } catch (e) {
        console.warn("Could not load data, falling back to empty state.", e);
        wallpapers = []; 
    }

    const statCards = document.querySelectorAll(".grid.grid-cols-1.sm\\:grid-cols-3 > div p.text-3xl");
    if (statCards.length >= 3) {
        statCards[0].textContent = counts.total;   // Total Submitted
        statCards[1].textContent = counts.pending; // Need Review
        statCards[2].textContent = counts.done;    // Done
    }
    const itemCountSpan = document.getElementById("queueItemCount");
    if (itemCountSpan) {
        itemCountSpan.textContent = `${wallpapers.length} items`;
    }
    const tbody = document.querySelector("table tbody");
    if (!tbody) {
        console.warn("Could not find table tbody");
        return;
    }

    tbody.innerHTML = "";

    // empty state fallback
    if (wallpapers.length === 0) {
        const emptyRow = document.createElement("tr");
        emptyRow.id = "emptyStateRow";
        emptyRow.className = "hover:none";
        emptyRow.innerHTML = `
            <td colspan="5" class="px-4 sm:px-6 py-16 text-center">
                <div class="flex flex-col items-center">
                    <svg class="w-16 h-16 mx-auto text-gray-200 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
                    </svg>
                    <p class="text-gray-500 text-sm font-medium">No submissions to review</p>
                    <p class="text-gray-400 text-xs mt-1">All caught up! Check back later.</p>
                </div>
            </td>
        `;
        tbody.appendChild(emptyRow);
        return;
    }

    // render table rows
    wallpapers.forEach(wallpaper => {
        const row = document.createElement("tr");
        
        let rowClass = "";
        let statusBadgeClass = "";
        let statusDotColor = "";

        if (wallpaper.status === "pending") {
            rowClass = "hover:bg-white cursor-pointer";
            statusBadgeClass = "bg-amber-50 text-amber-600 border border-amber-100";
            statusDotColor = "bg-amber-400";
        } else if (wallpaper.status === "in_review") {
            rowClass = "opacity-60 cursor-not-allowed";
            statusBadgeClass = "bg-brand-light text-brand border border-brand-muted/40";
            statusDotColor = "bg-brand";
        } else if (wallpaper.status === "approved") {
            rowClass = "opacity-60 cursor-not-allowed";
            statusBadgeClass = "bg-emerald-50 text-emerald-600 border border-emerald-100";
            statusDotColor = "bg-emerald-400";
        } else if (wallpaper.status === "rejected") {
            rowClass = "opacity-60 cursor-not-allowed";
            statusBadgeClass = "bg-red-50 text-red-600 border border-red-100";
            statusDotColor = "bg-red-400";
        }

        const date = new Date(wallpaper.created_at);
        const formattedDate = `${String(date.getUTCDate()).padStart(2, "0")}/${String(date.getUTCMonth() + 1).padStart(2, "0")}/${String(date.getUTCFullYear()).slice(-2)}`;
        const contributorName = wallpaper.contributor.email.split("@")[0];
        const statusText = wallpaper.status.charAt(0).toUpperCase() + wallpaper.status.slice(1).replace("_", " ");

        row.className = rowClass;
        
        // pass ID to URL
        if (wallpaper.status === "pending") {
            row.onclick = () => window.location.href = `./review.html?id=${wallpaper.id}`;
        }

        // replace with real data when available
        row.innerHTML = `
            <td class="px-4 sm:px-6 py-4"><div class="w-20 h-14 rounded-lg bg-gray-200"></div></td>
            <td class="px-4 sm:px-6 py-4 font-medium text-gray-800">${wallpaper.title}</td>
            <td class="hidden md:table-cell px-4 sm:px-6 py-4 text-gray-500 font-mono text-xs">@${contributorName}</td>
            <td class="px-4 sm:px-6 py-4"><span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold ${statusBadgeClass}"><span class="w-1.5 h-1.5 rounded-full ${statusDotColor}"></span>${statusText}</span></td>
            <td class="hidden md:table-cell px-4 sm:px-6 py-4 text-gray-400 font-mono text-xs">${formattedDate}</td>
        `;

        tbody.appendChild(row);
    });
}

bootstrapQueuePage();