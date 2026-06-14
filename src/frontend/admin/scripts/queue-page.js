/**
 * Queue/Index page initialization
 * Renders wallpapers table and stats cards
 */
import { requireAuth } from "./core/auth-guard.js";
import { populateReviewUI } from "./review-page.js";
import { ENV } from "./config/environment.js";

export let displayName = "";

export async function bootstrapQueuePage() {
    // check auth
    const isAuth = await requireAuth("./login.html");
    if (!isAuth) return;

    // get admin name
    try {
        const sessionRes = await fetch(`${ENV.API_BASE_URL}/sessions/current`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Accept': 'application/json' }
        });

        if (sessionRes.ok) {
            const sessionJson = await sessionRes.json();
            displayName = sessionJson.data?.user?.display_name || sessionJson.data?.display_name || sessionJson.display_name;
            
            if (displayName) {
                const welcomeText = document.getElementById("admin");
                if (welcomeText) {
                    welcomeText.textContent = `Welcome back, ${displayName}`;
                }
            }
        }
    } catch (e) {
        console.error("Failed to fetch admin name", e);
    }

    // unhide page if authorized
    const appContent = document.getElementById("app-content");
    if (appContent) {
        appContent.style.display = "flex";
    }

    let currentPage = 1;
    let itemsPerPage = 20;
    let currentStatus = 'pending';
    let currentOrder = 'desc'; // 'desc' = latest first, 'asc' = oldest first
    
    // create caches for table data and stats
    const tableCache = new Map();
    const statsCache = new Map();

    // cache invalidation
    window.refreshQueueData = () => { 
        tableCache.clear(); 
        statsCache.clear();
        loadTableData(); 
        loadStatCards(); 
    };

    async function loadTableData() {
        const baseUrl = `${ENV.API_BASE_URL}/moderation/wallpapers`;
        
        const statusQuery = currentStatus === 'total' ? '' : `status=${currentStatus}&`;
        const orderQuery = `order_by=date&order=${currentOrder}&`;
        const queryKey = `${statusQuery}${orderQuery}page=${currentPage}&per_page=${itemsPerPage}`;
        const url = `${baseUrl}?${queryKey}`;
        
        // cache check
        if (tableCache.has(queryKey)) {
            const cached = tableCache.get(queryKey);
            renderTable(cached.data || [], cached.meta || { total: 0, last_page: 1 });
            return; 
        }

        try {
            const res = await fetch(url, {
                method: 'GET',
                cache: 'no-store',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error("API Fetch failed");
            
            const json = await res.json();
            
            // save to cache
            tableCache.set(queryKey, json);
            renderTable(json.data || [], json.meta || { total: 0, last_page: 1 });
        } catch (e) {
            console.error(e);
            renderTable([], { total: 0, last_page: 1 }); 
        }
    }
    
    async function loadStatCards() {
        // cache check
        if (statsCache.has('all_stats')) {
            updateStatUI(statsCache.get('all_stats'));
            return;
        }

        const baseUrl = `${ENV.API_BASE_URL}/moderation/wallpapers`;
        const fetchOpts = { method: 'GET', credentials: 'include', headers: { 'Accept': 'application/json' } };

        try {
            const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
                fetch(`${baseUrl}?status=pending&per_page=1`, fetchOpts),
                fetch(`${baseUrl}?status=approved&per_page=1`, fetchOpts),
                fetch(`${baseUrl}?status=rejected&per_page=1`, fetchOpts)
            ]);

            const pendingJson = await pendingRes.json();
            const approvedJson = await approvedRes.json();
            const rejectedJson = await rejectedRes.json();

            const pendingCount = pendingJson.meta?.total || 0;
            const approvedCount = approvedJson.meta?.total || 0;
            const rejectedCount = rejectedJson.meta?.total || 0;
            const totalCount = pendingCount + approvedCount + rejectedCount;

            const stats = { totalCount, pendingCount, approvedCount, rejectedCount };
            
            // save to cache
            statsCache.set('all_stats', stats);
            updateStatUI(stats);
            
        } catch (e) {
            console.error("Failed to load stat cards", e);
        }
    }

    function updateStatUI(stats) {
        const statCards = document.querySelectorAll("[data-stat-grid] div p.text-3xl");
        if (statCards.length >= 4) {
            statCards[0].textContent = stats.totalCount;      // Total Submitted
            statCards[1].textContent = stats.pendingCount;    // Need Review
            statCards[2].textContent = stats.approvedCount;   // Approved
            statCards[3].textContent = stats.rejectedCount;   // Rejected
        }
    }

    const cardDivs = document.querySelectorAll("[data-stat-grid] div");
    if (cardDivs.length >= 4) {
        cardDivs.forEach((div, index) => {
            if (index === 0) return; 
            div.classList.add("cursor-pointer", "transition-all", "hover:ring-2", "hover:ring-brand/30");
        });

        if (!cardDivs[1].dataset.hasListener) {
            cardDivs[1].dataset.hasListener = "true";
            cardDivs[2].dataset.hasListener = "true";
            cardDivs[3].dataset.hasListener = "true";

            cardDivs[1].addEventListener('click', () => {
                currentPage = 1;
                currentStatus = 'pending';
                loadTableData();
            });

            cardDivs[2].addEventListener('click', () => {
                currentPage = 1;
                currentStatus = 'approved';
                loadTableData();
            });

            cardDivs[3].addEventListener('click', () => {
                currentPage = 1;
                currentStatus = 'rejected';
                loadTableData();
            });
        }
    }

    const sortDateBtn = document.getElementById("sortDateBtn");
    const sortIcon = document.getElementById("sortIcon");

    if (sortDateBtn) {
        sortDateBtn.addEventListener("click", () => {
            currentOrder = currentOrder === "desc" ? "asc" : "desc";
            sortIcon.textContent = currentOrder === "desc" ? "↓" : "↑";
            
            currentPage = 1; 
            loadTableData();
        });
    }

    function renderTable(dataArray, meta) {
        const itemCountSpan = document.getElementById("queueItemCount");
        if (itemCountSpan) {
            itemCountSpan.textContent = `Showing ${dataArray.length} items (Total: ${meta.total})`;
        }
        
        const tbody = document.querySelector("table tbody");
        if (!tbody) {
            console.warn("Could not find table tbody");
            return;
        }

        tbody.innerHTML = "";

        // empty state fallback
        const emptyStateDiv = document.getElementById("table-empty-state");
        const thead = document.querySelector("thead");
        
        if (dataArray.length === 0) {
            tbody.style.display = "none"; 
            thead.style.display = "none";
            
            emptyStateDiv.classList.remove("hidden");
            emptyStateDiv.classList.add("flex"); 
            return;
        }

        thead.style.display = "table-row-group";
        tbody.style.display = "table-row-group"; 
        emptyStateDiv.classList.add("hidden");
        emptyStateDiv.classList.remove("flex");

        // render table rows
        dataArray.forEach(wallpaper => {
            const row = document.createElement("tr");
            
            let rowClass = "";
            let statusBadgeClass = "";
            let statusDotColor = "";
            
            const isPending = wallpaper.status === "pending";

            if (isPending) {
                rowClass = "bg-white hover:bg-gray-100 hover:shadow-md transition-all duration-200 cursor-pointer group relative z-0 hover:z-10";
                statusBadgeClass = "bg-amber-50 text-amber-600 border border-amber-300";
                statusDotColor = "bg-amber-400";
            } else if (wallpaper.status === "approved") {
                rowClass = "bg-gray-50/50 hover:bg-gray-200 transition-colors duration-200 cursor-pointer group opacity-80";
                statusBadgeClass = "bg-emerald-50 text-emerald-600 border border-emerald-300";
                statusDotColor = "bg-emerald-400";
            } else if (wallpaper.status === "rejected") {
                rowClass = "bg-gray-50/50 hover:bg-gray-200 transition-colors duration-200 cursor-pointer group opacity-80";
                statusBadgeClass = "bg-red-50 text-red-600 border border-red-300";
                statusDotColor = "bg-red-400";
            }

            const date = new Date(wallpaper.created_at);
            const formattedDate = `${String(date.getUTCDate()).padStart(2, "0")}/${String(date.getUTCMonth() + 1).padStart(2, "0")}/${String(date.getUTCFullYear()).slice(-2)}`;
            const contributorName = wallpaper.contributor.display_name;
            const statusText = wallpaper.status.charAt(0).toUpperCase() + wallpaper.status.slice(1).replace("_", " ");

            row.className = rowClass;
            
            // pass ID to URL
            row.onclick = () => {
                openReviewScreen(wallpaper);
            };

            // replace with real data when available
            row.innerHTML = `
                <td class="px-4 sm:px-6 py-4">
                    <div class="w-20 h-14 rounded-lg bg-gray-200 overflow-hidden ${isPending ? 'group-hover:ring-2 group-hover:ring-brand/30' : ''} transition-all duration-150">
                        <img src="${wallpaper.thumbnail_path}" alt="thumbnail" class="w-full h-full object-cover">
                    </div>
                </td>
                <td class="px-0 sm:pl-8 pr-4 py-4 font-medium text-gray-800 text-xs sm:text-sm">
                    <div class="max-w-[140px] min-[440px]:max-w-[210px] sm:max-w-[260px] min-[550px]:max-w-[340px] md:max-w-[300px] min-[880px]:max-w-[380px] lg:max-w-[420px] min-[1300px]:max-w-[600px] xl:max-w-[650px] truncate">
                        ${wallpaper.title}
                    </div>
                </td>
                <td class="hidden lg:table-cell px-4 py-4 text-gray-500 font-mono text-xs">@${contributorName}</td>
                <td class="px-2 sm:px-4 py-4 text-center whitespace-nowrap">
                    <span class="inline-flex items-center justify-center gap-1.5 px-2 py-1 rounded-lg text-[10px] sm:text-xs font-semibold ${statusBadgeClass}">
                        <span class="w-1.5 h-1.5 rounded-full ${statusDotColor}"></span>${statusText}
                    </span>
                </td>
                <td class="hidden lg:table-cell px-4 sm:px-6 py-4 text-gray-400 font-mono text-xs">${formattedDate}</td>
            `;

            tbody.appendChild(row);
        });

        renderPagination(meta.total, meta.last_page);
    }

    function renderPagination(totalItems, totalPages) {
        const container = document.getElementById("paginationControls");
        if (!container) return;

        if (totalItems === 0) {
            container.classList.add("hidden");
            return;
        }
        
        container.classList.remove("hidden");

        container.innerHTML = `
            <div class="flex flex-1 items-center justify-between w-full">
                <div class="flex items-center gap-4">
                    <p class="text-xs sm:text-sm text-gray-700 whitespace-nowrap">
                        Page <span class="font-bold text-gray-900">${currentPage}</span> of <span class="font-bold text-gray-900">${totalPages}</span>
                    </p>
                    <span class="text-gray-300">|</span>
                    <div class="flex items-center gap-2">
                        <label for="perPageSelect" class="text-[0.625rem] sm:text-xs text-gray-500 uppercase tracking-wider font-semibold">Show:</label>
                        <select id="perPageSelect" class="text-xs sm:text-sm border-gray-300 rounded-md py-1 pl-2 pr-4 sm:pr-8 focus:ring-brand focus:border-brand shadow-sm cursor-pointer">
                            <option value="20" ${itemsPerPage === 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${itemsPerPage === 50 ? 'selected' : ''}>50</option>
                            <option value="75" ${itemsPerPage === 75 ? 'selected' : ''}>75</option>
                            <option value="100" ${itemsPerPage === 100 ? 'selected' : ''}>100</option>
                        </select>
                    </div>
                </div>
                
                <div>
                    <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                        <button id="prevPageBtn" ${currentPage === 1 ? 'disabled' : ''} class="relative inline-flex items-center rounded-l-md px-1 py-1 sm:px-2 sm:py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
                            <span class="sr-only">Previous</span>
                            <svg class="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clip-rule="evenodd" />
                            </svg>
                        </button>
                        <button id="nextPageBtn" ${currentPage === totalPages ? 'disabled' : ''} class="relative inline-flex items-center rounded-r-md px-1 py-1 sm:px-2 sm:py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
                            <span class="sr-only">Next</span>
                            <svg class="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clip-rule="evenodd" />
                            </svg>
                        </button>
                    </nav>
                </div>
            </div>
        `;

        const prevBtn = document.getElementById("prevPageBtn");
        const nextBtn = document.getElementById("nextPageBtn");

        if (prevBtn) {
            prevBtn.addEventListener("click", () => {
                if (currentPage > 1) {
                    currentPage--;
                    loadTableData(); 
                }
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener("click", () => {
                if (currentPage < totalPages) {
                    currentPage++;
                    loadTableData();
                }
            });
        }

        // items per page selector
        const selectBtn = document.getElementById("perPageSelect");
        if (selectBtn) {
            selectBtn.addEventListener("change", (e) => {
                itemsPerPage = parseInt(e.target.value); 
                currentPage = 1; 
                loadTableData();
            });
        }
    }
    loadStatCards();
    loadTableData();
}

bootstrapQueuePage();

// SPA state management for review page
let currentReviewId = null;

function openReviewScreen(wallpaper) {
    currentReviewId = wallpaper.id; 
    console.log("ISI DATA WALLPAPER UTUH:", wallpaper);
    console.log("ISI DATA MODERATION:", wallpaper.moderation);

    // hide Queue page and show Review page
    document.getElementById('queueView').classList.add('hidden');
    document.getElementById('reviewView').classList.remove('hidden');
    
    document.getElementById('adminSidebar').classList.add('hidden');
    document.querySelector('main').classList.remove('md:ml-72');

    populateReviewUI(wallpaper, () => { 
        closeReviewScreen(); 
        window.refreshQueueData(); 
    });
}

export function closeReviewScreen() {
    currentReviewId = null;
    document.getElementById('reviewView').classList.add('hidden');
    document.getElementById('queueView').classList.remove('hidden');
    
    document.getElementById('adminSidebar').classList.remove('hidden');
    document.querySelector('main').classList.add('md:ml-72');
}

const backBtn = document.getElementById('backToQueueBtn');
if (backBtn) {
    backBtn.addEventListener('click', closeReviewScreen);
}