document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchInput');
    const searchResults = document.getElementById('searchResults');
    let debounceTimer;

    searchInput.addEventListener('input', function() {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            const query = this.value.trim();
            if (query.length > 2) {
                fetchSearchResults(query);
            } else {
                searchResults.innerHTML = '';
            }
        }, 300);
    });

    function fetchSearchResults(query) {
        fetch(`/search?q=${encodeURIComponent(query)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        })
            .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
            .then(data => {
            displayResults(data);
        })
            .catch(error => {
            console.error('Error:', error);
            searchResults.innerHTML = '<li>Ми нічого не знайшли</li>';
        });
    }

    function displayResults(results) {
        searchResults.innerHTML = '';
        if (results.length === 0) {
            searchResults.innerHTML = '<li class="nothing-found">Ми нічого не знайшли <i style="font-size:20px;" class="fa-regular fa-face-frown"></i></li>';
            return;
        }
        results.forEach(manga => {
            const li = document.createElement('li');
            li.innerHTML = `
        <a href="/manga/${manga.id}" class="search-result-item">
            <img src="${manga.mangaPosterImg}" alt="${manga.mangaName}" class="search-result-image">
            <span class="search-result-title">${manga.mangaName}</span>
            <span class="rating">
                <i class="fas fa-star"></i> ${manga.averageRating}
            </span>
        </a>
        `;
            searchResults.appendChild(li);
        });
    }
});