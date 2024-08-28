document.addEventListener('DOMContentLoaded', function () {
    const filterTemplateFl = document.getElementById('filter-template-fl');
    const filterTemplateMobile = document.getElementById('filter-template-mobile');
    const sidebarContent = document.getElementById('sidebar-content');
    const modalContent = document.getElementById('modal-content');

    if (filterTemplateFl && filterTemplateMobile && sidebarContent && modalContent) {
        sidebarContent.innerHTML = filterTemplateFl.innerHTML;
        modalContent.innerHTML = filterTemplateMobile.innerHTML;

        initializeFilters(sidebarContent, 'fl');
        initializeFilters(modalContent, 'mobile');
    }

    function initializeFilters(container, type) {
        const genreSelect = $(container).find(`#genre-select-${type}`);
        const authorSelect = $(container).find(`#author-select-${type}`);
        const mangaStatusSelect = $(container).find(`#mangaStatus-${type}`);
        const ageRatingSelect = $(container).find(`#ageRating-${type}`);
        const releaseYearFrom = container.querySelector(`#releaseYearFrom-${type}`);
        const releaseYearTo = container.querySelector(`#releaseYearTo-${type}`);
        const filterForm = container.querySelector(`#filter-form-${type}`);

        $(genreSelect).select2({
            placeholder: 'Оберіть жанри',
            allowClear: true,
            closeOnSelect: false,
            minimumResultsForSearch: Infinity,
        });

        $(authorSelect).select2({
            placeholder: 'Оберіть автора',
            allowClear: true,
            closeOnSelect: false,
            minimumResultsForSearch: Infinity
        });

        $(mangaStatusSelect).select2({
            placeholder: 'Виберіть статус манги',
            minimumResultsForSearch: Infinity
        });

        $(ageRatingSelect).select2({
            placeholder: 'Виберіть віковий рейтинг',
            minimumResultsForSearch: Infinity
        });

        fetch('/genres-filter')
            .then(response => response.json())
            .then(genres => {
            genres.forEach(genre => {
                const option = new Option(genre.genreName, genre.id, false, false);
                genreSelect.append(option);
            });
            genreSelect.trigger('change.select2');
        })
            .catch(error => console.error('Ошибка при загрузке жанров:', error));

        fetch('/authors-filter')
            .then(response => response.json())
            .then(authors => {
            authors.forEach(author => {
                const option = new Option(author.name, author.id, false, false);
                authorSelect.append(option);
            });
            authorSelect.trigger('change.select2');
        })
            .catch(error => console.error('Ошибка при загрузке авторов:', error));

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
                    .catch(error => console.error('Ошибка при загрузке:', error));
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
            } else if (count >= 25) {
                $catalogCount.text((count - 1) + ">");
                setTimeout(() => {
                    $viewButtons.fadeIn();
                }, 50);
            } else {
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
