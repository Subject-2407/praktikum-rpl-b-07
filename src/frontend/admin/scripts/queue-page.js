/**
 * Queue/Index page initialization
 * Renders wallpapers table and stats cards
 */
import { requireAuth } from "./core/auth-guard.js";
import { getAllMockWallpapers } from "./data/mock/mock-wallpapers.js";
import { populateReviewUI } from "./review-page.js";

export async function bootstrapQueuePage() {
    // check auth
    const isAuth = await requireAuth("./login.html");
    if (!isAuth) return;

    // unhide page if authorized
    const appContent = document.getElementById("app-content");
    if (appContent) {
        appContent.style.display = "flex";
    }

    let currentPage = 1;
    let itemsPerPage = 20;
    let currentStatus = 'pending';

    async function loadTableData() {
        const baseUrl = 'http://localhost:8000/moderation/wallpapers';
        
        const statusQuery = currentStatus === 'total' ? '' : `status=${currentStatus}&`;
        const url = `${baseUrl}?${statusQuery}page=${currentPage}&per_page=${itemsPerPage}`;
        
        try {
            const res = await fetch(url, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error("API Fetch failed");
            
            const json = await res.json();
            renderTable(json.data || [], json.meta || { total: 0, last_page: 1 });
        } catch (e) {
            console.error(e);
            renderTable([], { total: 0, last_page: 1 }); 
        }
    }
    
    async function loadStatCards() {
        const baseUrl = 'http://localhost:8000/moderation/wallpapers';
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

            const statCards = document.querySelectorAll(".grid.grid-cols-1.sm\\:grid-cols-4 > div p.text-3xl");
            if (statCards.length >= 4) {
                statCards[0].textContent = totalCount;      // Total Submitted
                statCards[1].textContent = pendingCount;    // Need Review
                statCards[2].textContent = approvedCount;   // Approved
                statCards[3].textContent = rejectedCount;   // Rejected
            }
        } catch (e) {
            console.error("Failed to load stat cards", e);
        }
    }

    const cardDivs = document.querySelectorAll(".grid.grid-cols-1.sm\\:grid-cols-4 > div");
    if (cardDivs.length >= 4) {
        cardDivs.forEach(div => {
            div.classList.add("cursor-pointer", "transition-all", "hover:ring-2", "hover:ring-brand/30");
        });

        if (!cardDivs[0].dataset.hasListener) {
            cardDivs[0].dataset.hasListener = "true";
            cardDivs[1].dataset.hasListener = "true";
            cardDivs[2].dataset.hasListener = "true";
            cardDivs[3].dataset.hasListener = "true";

            cardDivs[0].addEventListener('click', () => {
                currentPage = 1;
                // renderTable(queues.total);
                currentStatus = 'total';
                loadTableData();
            });

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
        if (dataArray.length === 0) {
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
        dataArray.forEach(wallpaper => {
            const row = document.createElement("tr");
            
            let rowClass = "";
            let statusBadgeClass = "";
            let statusDotColor = "";
            let isClickable = wallpaper.status === "pending";

            if (isClickable) {
                rowClass = "hover:bg-white hover:shadow-md transition-all duration-200 cursor-pointer group relative z-0 hover:z-10";
                statusBadgeClass = "bg-amber-50 text-amber-600 border border-amber-100";
                statusDotColor = "bg-amber-400";
            } else if (wallpaper.status === "in_review") {
                rowClass = "transition-colors duration-100 group opacity-60 cursor-not-allowed";
                statusBadgeClass = "bg-brand-light text-brand border border-brand-muted/40";
                statusDotColor = "bg-brand";
            } else if (wallpaper.status === "approved") {
                rowClass = "transition-colors duration-100 group opacity-60 cursor-not-allowed";
                statusBadgeClass = "bg-emerald-50 text-emerald-600 border border-emerald-100";
                statusDotColor = "bg-emerald-400";
            } else if (wallpaper.status === "rejected") {
                rowClass = "transition-colors duration-100 group opacity-60 cursor-not-allowed";
                statusBadgeClass = "bg-red-50 text-red-600 border border-red-100";
                statusDotColor = "bg-red-400";
            }

            const date = new Date(wallpaper.created_at);
            const formattedDate = `${String(date.getUTCDate()).padStart(2, "0")}/${String(date.getUTCMonth() + 1).padStart(2, "0")}/${String(date.getUTCFullYear()).slice(-2)}`;
            const contributorName = wallpaper.contributor.email.split("@")[0];
            const statusText = wallpaper.status.charAt(0).toUpperCase() + wallpaper.status.slice(1).replace("_", " ");

            row.className = rowClass;
            
            // pass ID to URL
            if (isClickable) {
                row.onclick = () => {
                    openReviewScreen(wallpaper);
                };
            }

            // replace with real data when available
            row.innerHTML = `
                <td class="px-4 sm:px-6 py-4 w-32">
                    <div class="w-20 h-14 rounded-lg bg-gray-200 overflow-hidden ${isClickable ? 'group-hover:ring-2 group-hover:ring-brand/30' : ''} transition-all duration-150">
                        <img src="${wallpaper.file_path}" alt="thumbnail" class="w-full h-full object-cover">
                    </div>
                </td>
                <td class="px-4 sm:px-6 py-4 font-medium text-gray-800">${wallpaper.title}</td>
                <td class="hidden md:table-cell px-4 sm:px-6 py-4 text-gray-500 font-mono text-xs">@${contributorName}</td>
                <td class="px-4 sm:px-6 py-4"><span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold ${statusBadgeClass}"><span class="w-1.5 h-1.5 rounded-full ${statusDotColor}"></span>${statusText}</span></td>
                <td class="hidden md:table-cell px-4 sm:px-6 py-4 text-gray-400 font-mono text-xs">${formattedDate}</td>
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
                    <p class="text-sm text-gray-700">
                        Page <span class="font-bold text-gray-900">${currentPage}</span> of <span class="font-bold text-gray-900">${totalPages}</span>
                    </p>
                    <span class="text-gray-300">|</span>
                    <div class="flex items-center gap-2">
                        <label for="perPageSelect" class="text-xs text-gray-500 uppercase tracking-wider font-semibold">Show:</label>
                        <select id="perPageSelect" class="text-sm border-gray-300 rounded-md py-1 pl-2 pr-8 focus:ring-brand focus:border-brand shadow-sm cursor-pointer">
                            <option value="20" ${itemsPerPage === 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${itemsPerPage === 50 ? 'selected' : ''}>50</option>
                            <option value="75" ${itemsPerPage === 75 ? 'selected' : ''}>75</option>
                            <option value="100" ${itemsPerPage === 100 ? 'selected' : ''}>100</option>
                        </select>
                    </div>
                </div>
                
                <div>
                    <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                        <button id="prevPageBtn" ${currentPage === 1 ? 'disabled' : ''} class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
                            <span class="sr-only">Previous</span>
                            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clip-rule="evenodd" />
                            </svg>
                        </button>
                        <button id="nextPageBtn" ${currentPage === totalPages ? 'disabled' : ''} class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 disabled:opacity-50 disabled:cursor-not-allowed transition-all">
                            <span class="sr-only">Next</span>
                            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
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

    // hide Queue page and show Review page
    document.getElementById('queueView').classList.add('hidden');
    document.getElementById('reviewView').classList.remove('hidden');
    
    document.getElementById('adminSidebar').classList.add('hidden');
    document.querySelector('main').classList.remove('md:ml-72');

    populateReviewUI(wallpaper, () => { 
        closeReviewScreen(); 
        bootstrapQueuePage(); 
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