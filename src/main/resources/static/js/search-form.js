document.addEventListener('DOMContentLoaded', function() {
    const searchToggle = document.querySelector('.search-toggle');
    const searchFormContainer = document.querySelector('.search-form-container');
    const closeBtn = document.querySelector('.close-btn');
    const searchOverlay = document.querySelector('.search-overlay');

    if (searchToggle && searchFormContainer && closeBtn && searchOverlay) {
        searchToggle.addEventListener('click', function(event) {
            event.stopPropagation();
            searchFormContainer.classList.add('active');
            searchOverlay.classList.add('active');
            searchFormContainer.querySelector('input[type="search"]').focus();
        });

        closeBtn.addEventListener('click', function() {
            searchFormContainer.classList.remove('active');
            searchOverlay.classList.remove('active');
        });

        document.addEventListener('click', function(event) {
            if (!searchFormContainer.contains(event.target) && event.target !== searchToggle) {
                searchFormContainer.classList.remove('active');
                searchOverlay.classList.remove('active');
            }
        });

        searchFormContainer.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    } else {
        console.error('One or more elements not found');
    }
});
