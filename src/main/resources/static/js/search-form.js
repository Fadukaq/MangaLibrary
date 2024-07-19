document.addEventListener('DOMContentLoaded', function() {
    const searchContainer = document.querySelector('.search-container');
    const searchToggle = document.querySelector('.search-toggle');
    const searchFormContainer = document.querySelector('.search-form-container');
    const searchForm = document.querySelector('.search-form');
    const searchInput = searchForm.querySelector('input[type="search"]');

    function toggleSearch() {
        searchFormContainer.classList.toggle('active');
        searchForm.classList.toggle('active');
        searchToggle.classList.toggle('hidden');
        if (searchFormContainer.classList.contains('active')) {
            setTimeout(() => {
                searchInput.focus();
            }, 300);
        }
    }

    searchToggle.addEventListener('click', function(e) {
        e.stopPropagation();
        toggleSearch();
    });

    document.addEventListener('click', function(event) {
        if (!searchContainer.contains(event.target)) {
            if (searchFormContainer.classList.contains('active')) {
                toggleSearch();
            }
        }
    });

    searchFormContainer.addEventListener('click', function(event) {
        event.stopPropagation();
    });

    searchForm.addEventListener('submit', function(event) {
        event.stopPropagation();
    });
});