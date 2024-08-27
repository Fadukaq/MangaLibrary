document.addEventListener('DOMContentLoaded', function () {
    $(document).ready(function() {
        $('#genre-select').select2({
            placeholder: 'Оберіть жанри',
            allowClear: true,
            minimumResultsForSearch: Infinity,
        });

        $('#author-select').select2({
            placeholder: 'Оберіть автора',
            allowClear: true,
            minimumResultsForSearch: Infinity
        });

        $('#mangaStatus').select2({
            placeholder: 'Виберіть статус манги',
            minimumResultsForSearch: Infinity
        });

        $('#ageRating').select2({
            placeholder: 'Виберіть віковий рейтинг',
            minimumResultsForSearch: Infinity
        });
    });

    const filterTemplate = document.getElementById('filter-template');
    const sidebarContent = document.getElementById('sidebar-content');
    const modalContent = document.getElementById('modal-content');

    if (filterTemplate && sidebarContent && modalContent) {
        sidebarContent.innerHTML = filterTemplate.innerHTML;
        modalContent.innerHTML = filterTemplate.innerHTML;

        initializeFilters(sidebarContent);
        initializeFilters(modalContent);
    }

    function initializeFilters(container) {
        const genreSelect = $(container).find('#genre-select');
        const authorSelect = $(container).find('#author-select');
        const mangaStatusSelect = $(container).find('#mangaStatus');
        const ageRatingSelect = $(container).find('#ageRating');
        const releaseYearFrom = container.querySelector('#releaseYearFrom');
        const releaseYearTo = container.querySelector('#releaseYearTo');
        const filterForm = container.querySelector('#filter-form');

        fetch('/genres-filter')
            .then(response => response.json())
            .then(genres => {
            genres.forEach(genre => {
                const option = new Option(genre.genreName, genre.id, false, false);
                genreSelect.append(option);
            });
            genreSelect.trigger('change.select2');
        })
            .catch(error => console.error('Помилка під час завантаження жанрів:', error));

        fetch('/authors-filter')
            .then(response => response.json())
            .then(authors => {
            authors.forEach(author => {
                const option = new Option(author.name, author.id, false, false);
                authorSelect.append(option);
            });
            authorSelect.trigger('change.select2');
        })
            .catch(error => console.error('Помилка під час завантаження авторів:', error));

        const currentYear = new Date().getFullYear();
        const minYear = 1902;

        releaseYearFrom.min = minYear;
        releaseYearTo.min = minYear;
        releaseYearFrom.value = minYear;

        releaseYearTo.max = currentYear;
        releaseYearFrom.max = currentYear;
        releaseYearTo.value = currentYear;

        releaseYearFrom.addEventListener('input', function () {
            const fromValue = parseInt(this.value, 10);
            if (fromValue > releaseYearTo.value) {
                releaseYearTo.value = fromValue;
            }
        });

        releaseYearTo.addEventListener('input', function () {
            const toValue = parseInt(this.value, 10);
            if (toValue < releaseYearFrom.value) {
                releaseYearFrom.value = toValue;
            }
        });

        filterForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const genres = genreSelect.val();
            const authors = authorSelect.val();
            const status = mangaStatusSelect.val();
            let ageRating = ageRatingSelect.val();
            const yearFrom = releaseYearFrom.value;
            const yearTo = releaseYearTo.value;
            if (ageRating === 'all') {
                ageRating = '';
            }
            const params = new URLSearchParams();
            genres.forEach(genre => params.append('genre', genre));
            authors.forEach(author => params.append('author', author));
            params.append('status', status);
            params.append('ageRating', ageRating);
            params.append('yearFrom', yearFrom);
            params.append('yearTo', yearTo);
            params.append('page', '1');

            const mangaContent = document.getElementById('manga-content');

            mangaContent.style.opacity = '0';
            mangaContent.style.transition = 'opacity 0.3s ease-out';

            setTimeout(() => {
                fetch(`/filter-manga?${params.toString()}`)
                    .then(response => response.text())
                    .then(html => {
                    mangaContent.innerHTML = html;
                    mangaContent.style.opacity = '1';
                    checkIfNoManga();
                    if (typeof window.updateStarRatings === 'function') {
                        window.updateStarRatings();
                    }
                    if (typeof window.initializeViewSwitch === 'function') {
                        window.initializeViewSwitch();
                    }
                })
                    .catch(error => console.error('Помилка під час завантаження:', error));
            }, 300);
        });
        function checkIfNoManga() {
            const $mangaGrid = $('.manga-grid');
            const $noMangaMessage = $('#no-manga-message');
            const $sortButtons = $('#sort-up, .dropdown-sort');
            const $viewButtons = $('.grid-view, .list-view');
            const $catalogCount = $('.catalog-count');
            const count = $mangaGrid.children().length;
            $sortButtons.hide();
            $viewButtons.hide();

            if (count === 0) {
                $noMangaMessage.show();
                $catalogCount.show();
                $catalogCount.text('0');
            } else if(count >= 25){
                $catalogCount.text((count - 1)+">");
                setTimeout(() => {
                    $viewButtons.fadeIn();
                }, 50);
            }else {
                $catalogCount.text(count);
                $catalogCount.show();
                $noMangaMessage.hide();
                $mangaGrid.show();
                setTimeout(() => {
                    $viewButtons.fadeIn();
                }, 50);
            }
        }
    }
});