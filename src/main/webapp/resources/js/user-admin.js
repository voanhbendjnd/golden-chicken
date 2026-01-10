function handleSearch() {
    try {
        // Get the search input value and trim whitespace
        const searchInput = document.getElementById("searchFullName");
        if (!searchInput) {
            console.error("Search input not found!");
            return;
        }
        
        const searchTerm = searchInput.value.trim();
        console.log("Searching for:", searchTerm);
        
        // Get current URL parameters
        const currentUrl = new URL(window.location);
        const params = new URLSearchParams(currentUrl.search);
        
        // Handle filter parameter
        if (searchTerm) {
            // Use Spring Filter syntax for case-insensitive like search
            params.set('filter', `fullName ~~ '*${searchTerm}*'`);
        } else {
            params.delete('filter');
        }
        
        // Reset to first page when searching
        params.set('page', '1');
        
        // Keep other parameters like size
        if (!params.has('size')) {
            params.set('size', '5');
        }
        
        // Build new URL
        const newUrl = `${currentUrl.pathname}?${params.toString()}`;
        console.log("Navigating to:", newUrl);
        
        // Navigate to the new URL
        window.location.href = newUrl;
        
    } catch (error) {
        console.error("Error in handleSearch:", error);
        alert("An error occurred while searching. Please try again.");
    }
}

// Initialize event listeners when page loads
document.addEventListener("DOMContentLoaded", function() {
    console.log("DOM loaded, initializing search functionality");
    
    const searchInput = document.getElementById("searchFullName");
    if (searchInput) {
        console.log("Search input found, setting up event listeners");
        
        // Add Enter key support
        searchInput.addEventListener("keypress", function(event) {
            if (event.key === "Enter") {
                console.log("Enter key pressed, triggering search");
                event.preventDefault(); // Prevent form submission if any
                handleSearch();
            }
        });
        
        // Add real-time search with debounce (optional)
        let debounceTimer;
        searchInput.addEventListener("input", function(event) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                // Auto-search after user stops typing for 500ms
                // handleSearch(); // Uncomment if you want auto-search
            }, 500);
        });
        
        // Focus on search input when page loads (optional)
        // searchInput.focus();
        
    } else {
        console.error("Search input element not found in the DOM");
    }
});

// Additional utility function to clear search
function clearSearch() {
    const searchInput = document.getElementById("searchFullName");
    if (searchInput) {
        searchInput.value = "";
        handleSearch();
    }
}