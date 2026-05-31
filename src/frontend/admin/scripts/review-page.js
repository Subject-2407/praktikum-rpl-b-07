/**
 * Review page initialization
 * Reads wallpaper ID from URL param and renders details
 */
import { requireAuth } from "./core/auth-guard.js";
import { getMockWallpaper } from "./data/mock/mock-wallpapers.js";

export async function bootstrapReviewPage() {
    // check auth
    const isAuth = await requireAuth("./login.html");
    if (!isAuth) return;

    // unhide page if authorized
    const appContent = document.getElementById("app-content");
    if (appContent) appContent.style.display = "block";

    const urlParams = new URLSearchParams(window.location.search);
    const wallpaperId = urlParams.get("id");

    if (!wallpaperId) {
        console.error("No wallpaper ID in URL");
        window.location.href = "./index.html";
        return;
    }

    const wallpaper = getMockWallpaper(wallpaperId);

    if (!wallpaper) {
        console.error(`Wallpaper not found: ${wallpaperId}`);
        window.location.href = "./index.html";
        return;
    }

    function formatDate(isoDate) {
        const date = new Date(isoDate);
        const day = String(date.getUTCDate()).padStart(2, "0");
        const month = String(date.getUTCMonth() + 1).padStart(2, "0");
        const year = String(date.getUTCFullYear()).slice(-2);
        return `${day}/${month}/${year}`;
    }

    function getContributorName(email) {
        return email.split("@")[0];
    }

    const pageTitle = document.querySelector("h1");
    if (pageTitle) {
        pageTitle.textContent = wallpaper.title;
    }

    const titleField = document.evaluate("//span[text()='Title']/following-sibling::span", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if (titleField) titleField.textContent = wallpaper.title;

    const descField = document.evaluate("//span[text()='Description']/following-sibling::span", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if (descField) descField.textContent = wallpaper.description || "No description provided.";

    const contribField = document.evaluate("//span[text()='Contributor']/following-sibling::span", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if (contribField) contribField.textContent = `@${getContributorName(wallpaper.contributor.email)}`;

    const tagsField = document.evaluate("//span[text()='Tags']/following-sibling::span", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if (tagsField) tagsField.textContent = wallpaper.tags.map(t => t.name).join(", ");

    const dateField = document.evaluate("//span[text()='Date']/following-sibling::span", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if (dateField) dateField.textContent = formatDate(wallpaper.created_at);

    // image preview update
    const imagePreview = document.querySelector("img[alt='Preview']");
    if (imagePreview && wallpaper.file_path) {
        // relative path for mock data, to do absolute URL for real API
        if (!wallpaper.file_path.startsWith('/uploads')) {
           imagePreview.src = wallpaper.file_path;
        }
    }

    // action buttons
    const confirmApproveBtn = document.getElementById("confirmApproveBtn");
    const confirmRejectBtn = document.getElementById("confirmRejectBtn");
    const mainApproveBtn = document.getElementById("approveBtn");
    const mainRejectBtn = document.getElementById("rejectBtn");

    if (confirmApproveBtn) {
        confirmApproveBtn.addEventListener("click", async () => {
            // hide modal
            document.getElementById("approveModal").classList.add("hidden");
            
            // loading state
            mainApproveBtn.textContent = "Processing...";
            
            // mock API call
            console.log(`[API MOCK] POST /v1/queue/${wallpaperId}/approve`);
            await new Promise(resolve => setTimeout(resolve, 800)); // Fake 0.8s network delay

            // update UI
            mainApproveBtn.textContent = "✓ \u00A0 Approved!";
            mainApproveBtn.className = "w-full bg-emerald-500 text-white font-semibold rounded-xl py-3.5 text-sm cursor-default";
            mainApproveBtn.disabled = true;
            mainRejectBtn.disabled = true;
            mainRejectBtn.classList.add("opacity-40", "cursor-not-allowed");
        });
    }

    if (confirmRejectBtn) {
        confirmRejectBtn.addEventListener("click", async () => {
            const reasonInput = document.getElementById("rejectReason");
            const reason = reasonInput.value.trim();

            // validation: require reason for rejection
            if (!reason) {
                reasonInput.classList.add("border-red-400");
                return;
            }

            // hide modal
            document.getElementById("rejectModal").classList.add("hidden");
            
            // loading state
            mainRejectBtn.textContent = "Processing...";
            
            // mock API call
            console.log(`[API MOCK] POST /queue/${wallpaperId}/reject`, { reason });
            await new Promise(resolve => setTimeout(resolve, 800));

            // update UI
            mainRejectBtn.textContent = "✕ \u00A0 Rejected";
            mainRejectBtn.className = "w-full bg-red-100 text-red-400 font-semibold rounded-xl py-3.5 text-sm cursor-default";
            mainRejectBtn.disabled = true;
            mainApproveBtn.disabled = true;
            mainApproveBtn.classList.add("opacity-40", "cursor-not-allowed");
        });
    }

    // remove red border when user starts typing again
    const reasonInput = document.getElementById("rejectReason");
    if (reasonInput) {
        reasonInput.addEventListener("input", () => {
            reasonInput.classList.remove("border-red-400");
        });
    }

    console.log("[REVIEW PAGE] Loaded wallpaper:", wallpaper.title);
}

bootstrapReviewPage();