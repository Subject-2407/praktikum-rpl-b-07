/**
 * Review page initialization
 * Reads wallpaper ID from URL param and renders details
 */
import { requireAuth } from "./core/auth-guard.js";
import { getMockWallpaper } from "./data/mock/mock-wallpapers.js";

const DEV_MODE = false;
let currentWallpaperId = null;
let currentSuccessCallback = null;

export function populateReviewUI(wallpaper, onSuccess) {
    currentWallpaperId = wallpaper.id;
    currentSuccessCallback = onSuccess; 

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

    // populate details
    const titleEl = document.getElementById("reviewTitle");
    if (titleEl) titleEl.textContent = wallpaper.title;

    const titleField = document.getElementById("review-title");
    if (titleField) titleField.textContent = wallpaper.title;

    const descField = document.getElementById("review-desc");
    if (descField) descField.textContent = wallpaper.description || "No description provided.";

    const contribField = document.getElementById("review-contrib");
    if (contribField) contribField.textContent = `@${getContributorName(wallpaper.contributor.email)}`;

    const tagsField = document.getElementById("review-tags");
    if (tagsField) tagsField.textContent = wallpaper.tags && wallpaper.tags.length > 0 ? wallpaper.tags.map(t => t.name).join(", ") : "None";

    const dateField = document.getElementById("review-date");
    if (dateField) dateField.textContent = formatDate(wallpaper.created_at);

    // populate image
    const imagePreview = document.getElementById("reviewImage");
    if (imagePreview && wallpaper.file_path) {
        imagePreview.src = wallpaper.file_path;
    }
}

    const mainApproveBtn = document.getElementById("approveBtn");
    const mainRejectBtn = document.getElementById("rejectBtn");

    if (mainApproveBtn) {
        mainApproveBtn.textContent = "✓ \u00A0 Approve";
        mainApproveBtn.className = "w-full bg-brand hover:bg-brand-dark active:scale-[0.99] text-white font-semibold rounded-xl py-3.5 text-sm transition-all duration-200 shadow-sm hover:shadow-md";
        mainApproveBtn.disabled = false;
    }

    if (mainRejectBtn) {
        mainRejectBtn.textContent = "✕ \u00A0 Reject";
        mainRejectBtn.className = "w-full bg-white hover:bg-red-200 active:scale-[0.99] text-red-500 border-2 border-red-300 hover:border-red-300 font-semibold rounded-xl py-3.5 text-sm transition-all duration-200";
        mainRejectBtn.disabled = false;
        mainRejectBtn.classList.remove("opacity-40", "cursor-not-allowed");
    }
    
    const reasonInput = document.getElementById("rejectReason");
    if (reasonInput) {
        reasonInput.value = "";
        reasonInput.classList.remove("border-red-400");
        reasonInput.classList.replace("mb-1", "mb-5");
        document.getElementById("rejectErrorText").classList.add("hidden");
    }

    // action buttons
    const confirmApproveBtn = document.getElementById("confirmApproveBtn");
    const confirmRejectBtn = document.getElementById("confirmRejectBtn");

    if (confirmApproveBtn) {
        confirmApproveBtn.addEventListener("click", async () => {
            // hide modal
            document.getElementById("approveModal").classList.add("hidden");
            
            // loading state
            mainApproveBtn.textContent = "Processing...";
            
            // API call
            try {
                if (DEV_MODE) {
                    console.log(`[DEV MOCK] PATCH /moderation/wallpapers/${currentWallpaperId} -> APPROVED`);
                    await new Promise(resolve => setTimeout(resolve, 800));
                } else {
                    const res = await fetch(`http://localhost:8000/moderation/wallpapers/${currentWallpaperId}`, {
                        method: 'PATCH',
                        credentials: 'include',
                        headers: { 
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        },
                        body: JSON.stringify({ decision: 'approved' })
                    });
                    if (!res.ok) throw new Error("API rejected the approval");
                }

                // update UI
                mainApproveBtn.textContent = "✓ \u00A0 Approved!";
                mainApproveBtn.className = "w-full bg-emerald-500 text-white font-semibold rounded-xl py-3.5 text-sm cursor-default";
                mainApproveBtn.disabled = true;
                mainRejectBtn.disabled = true;
                mainRejectBtn.classList.add("opacity-40", "cursor-not-allowed");
                
                setTimeout(() => {
                    if (currentSuccessCallback) currentSuccessCallback();
                }, 800);

            } catch (err) {
                console.error(err);
                mainApproveBtn.textContent = "Error! Try Again.";
                mainApproveBtn.classList.add("bg-red-500");
            }
        });
    }

    if (confirmRejectBtn) {
        confirmRejectBtn.addEventListener("click", async () => {
            const reason = reasonInput.value.trim();

            // validation: require reason for rejection
            if (!reason) {
                reasonInput.classList.add("border-red-400");
                reasonInput.classList.replace("mb-5", "mb-1");
                document.getElementById("rejectErrorText").classList.remove("hidden");
                return;
            }

            // hide modal
            document.getElementById("rejectModal").classList.add("hidden");
            
            // loading state
            mainRejectBtn.textContent = "Processing...";
            
            // API call
            try {
                if (DEV_MODE) {
                    console.log(`[DEV MOCK] PATCH /moderation/wallpapers/${currentWallpaperId} -> REJECTED. Reason:`, reason);
                    await new Promise(resolve => setTimeout(resolve, 800));
                } else {
                    const res = await fetch(`http://localhost:8000/moderation/wallpapers/${currentWallpaperId}`, {
                        method: 'PATCH',
                        credentials: 'include',
                        headers: { 
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        },
                        body: JSON.stringify({ decision: 'rejected', reason: reason })
                    });
                    if (!res.ok) throw new Error("API rejected the rejection");
                }

                // update UI
                mainRejectBtn.textContent = "✕ \u00A0 Rejected";
                mainRejectBtn.className = "w-full bg-red-100 text-red-400 font-semibold rounded-xl py-3.5 text-sm cursor-default";
                mainRejectBtn.disabled = true;
                mainApproveBtn.disabled = true;
                mainApproveBtn.classList.add("opacity-40", "cursor-not-allowed");

                setTimeout(() => {
                    if (currentSuccessCallback) currentSuccessCallback();
                }, 800);

            } catch (err) {
                console.error(err);
                mainRejectBtn.textContent = "Error! Try Again.";
            }
        });
    }

    // remove red warning when user starts typing again
    if (reasonInput) {
        reasonInput.addEventListener("input", () => {
            reasonInput.classList.remove("border-red-400");
            reasonInput.classList.replace("mb-1", "mb-5");
            document.getElementById("rejectErrorText").classList.add("hidden");
        });
    }