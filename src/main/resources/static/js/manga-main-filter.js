document.addEventListener('DOMContentLoaded', function () {
    const filterTemplate = document.getElementById('filter-template');
    const sidebarContent = document.getElementById('sidebar-content');
    const modalContent = document.getElementById('modal-content');

    sidebarContent.innerHTML = filterTemplate.innerHTML;
    initializeFilters(sidebarContent);

    modalContent.innerHTML = filterTemplate.innerHTML;
    initializeFilters(modalContent);

    function initializeFilters(container) {
        const genreSelect = container.querySelector('#genre-select');
        const authorSelect = container.querySelector('#author-select');
        const selectedGenresContainer = container.querySelector('#selected-genres');
        const selectedAuthorsContainer = container.querySelector('#selected-authors');
        const releaseYearFrom = container.querySelector('#releaseYearFrom');
        const releaseYearTo = container.querySelector('#releaseYearTo');
        const filterForm = container.querySelector('#filter-form');

        function updateGenreOptions() {
            const remainingOptions = Array.from(genreSelect.options).filter(option => !option.disabled && !option.classList.contains('default'));
            let defaultOption = genreSelect.querySelector('option.default');

            if (remainingOptions.length === 0) {
                if (!defaultOption) {
                    defaultOption = document.createElement('option');
                    defaultOption.textContent = 'Немає жанрів для вибору';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                    defaultOption.classList.add('default');
                    genreSelect.appendChild(defaultOption);
                } else {
                    defaultOption.textContent = 'Немає жанрів для вибору';
                    defaultOption.disabled = true;
                }
            } else {
                if (!defaultOption) {
                    defaultOption = document.createElement('option');
                    defaultOption.textContent = 'Виберіть жанр';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                    defaultOption.classList.add('default');
                    genreSelect.appendChild(defaultOption);
                } else {
                    defaultOption.textContent = 'Виберіть жанр';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                }
            }
        }

        function updateAuthorOptions() {
            const remainingOptions = Array.from(authorSelect.options).filter(option => !option.disabled && !option.classList.contains('default'));
            let defaultOption = authorSelect.querySelector('option.default');

            if (remainingOptions.length === 0) {
                if (!defaultOption) {
                    defaultOption = document.createElement('option');
                    defaultOption.textContent = 'Немає авторів для вибору';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                    defaultOption.classList.add('default');
                    authorSelect.appendChild(defaultOption);
                } else {
                    defaultOption.textContent = 'Немає авторів для вибору';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                }
            } else {
                if (!defaultOption) {
                    defaultOption = document.createElement('option');
                    defaultOption.textContent = 'Виберіть автора';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                    defaultOption.classList.add('default');
                    authorSelect.appendChild(defaultOption);
                } else {
                    defaultOption.textContent = 'Виберіть автора';
                    defaultOption.disabled = true;
                    defaultOption.selected = true;
                }
            }
        }

        fetch('/genres-filter')
            .then(response => response.json())
            .then(genres => {
            genres.forEach(genre => {
                const option = document.createElement('option');
                option.value = genre.id;
                option.textContent = genre.genreName;
                genreSelect.appendChild(option);
            });
            updateGenreOptions();
        })
            .catch(error => console.error('Ошибка при загрузке жанров:', error));

        fetch('/authors-filter')
            .then(response => response.json())
            .then(authors => {
            authors.forEach(author => {
                const option = document.createElement('option');
                option.value = author.id;
                option.textContent = author.name;
                authorSelect.appendChild(option);
            });
            updateAuthorOptions();
        })
            .catch(error => console.error('Ошибка при загрузке авторов:', error));

        genreSelect.addEventListener('change', function () {
            const selectedOption = this.options[this.selectedIndex];
            const genreId = selectedOption.value;
            const genreName = selectedOption.textContent;

            if (genreId && !selectedGenresContainer.querySelector(`.selected-genre[data-id="${genreId}"]`)) {
                const genreElement = document.createElement('div');
                genreElement.classList.add('selected-genre');
                genreElement.setAttribute('data-id', genreId);
                genreElement.innerHTML = `${genreName}<button class="remove-genre" data-id="${genreId}">&times;</button>`;

                selectedGenresContainer.appendChild(genreElement);
                selectedOption.remove();
                this.selectedIndex = 0;
                updateGenreOptions();
            }
        });

        authorSelect.addEventListener('change', function () {
            const selectedOption = this.options[this.selectedIndex];
            const authorId = selectedOption.value;
            const authorName = selectedOption.textContent;

            if (authorId && !selectedAuthorsContainer.querySelector(`.selected-author[data-id="${authorId}"]`)) {
                const authorElement = document.createElement('div');
                authorElement.classList.add('selected-author');
                authorElement.setAttribute('data-id', authorId);
                authorElement.innerHTML = `${authorName}<button class="remove-author" data-id="${authorId}">&times;</button>`;

                selectedAuthorsContainer.appendChild(authorElement);
                selectedOption.remove();
                this.selectedIndex = 0;
                updateAuthorOptions();
            }
        });

        selectedGenresContainer.addEventListener('click', function (event) {
            if (event.target.classList.contains('remove-genre')) {
                const genreId = event.target.getAttribute('data-id');
                document.querySelector(`.selected-genre[data-id="${genreId}"]`).remove();

                fetch('/genres-filter')
                    .then(response => response.json())
                    .then(genres => {
                    const genre = genres.find(g => g.id === parseInt(genreId));
                    if (genre) {
                        const option = document.createElement('option');
                        option.value = genre.id;
                        option.textContent = genre.genreName;
                        genreSelect.appendChild(option);
                        updateGenreOptions();
                    }
                });
            }
        });

        selectedAuthorsContainer.addEventListener('click', function (event) {
            if (event.target.classList.contains('remove-author')) {
                const authorId = event.target.getAttribute('data-id');
                document.querySelector(`.selected-author[data-id="${authorId}"]`).remove();

                fetch('/authors-filter')
                    .then(response => response.json())
                    .then(authors => {
                    const author = authors.find(a => a.id === parseInt(authorId));
                    if (author) {
                        const option = document.createElement('option');
                        option.value = author.id;
                        option.textContent = author.name;
                        authorSelect.appendChild(option);
                        updateAuthorOptions();
                    }
                });
            }
        });

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
            if (fromValue > releaseYearTo.value && releaseYearTo.value !== '') {
                releaseYearTo.value = fromValue;
            }
        });

        releaseYearTo.addEventListener('input', function () {
            const toValue = parseInt(this.value, 10);
            if (toValue < releaseYearFrom.value && releaseYearFrom.value !== '') {
                releaseYearFrom.value = toValue;
            }
        });

        function checkIfNoManga() {
            const $mangaGrid = $('.manga-grid');
            const $noMangaMessage = $('#no-manga-message');
            const $sortButtons = $('#sort-up, .dropdown-sort');
            const $viewButtons = $('.grid-view, .list-view');
            $sortButtons.hide();

            if ($mangaGrid.children().length === 0) {
                $viewButtons.hide();
                $noMangaMessage.show();
            } else {
                $mangaGrid.show();
                $viewButtons.show();
                $noMangaMessage.hide();
            }
        }


        filterForm.addEventListener('submit', function(event) {
            event.preventDefault();

            const genreElements = container.querySelectorAll('#selected-genres .selected-genre');
            const genres = Array.from(genreElements).map(el => el.getAttribute('data-id'));
            const authorElements = container.querySelectorAll('#selected-authors .selected-author');
            const authors = Array.from(authorElements).map(el => el.getAttribute('data-id'));

            const status = container.querySelector('#mangaStatus').value;
            const ageRating = container.querySelector('#ageRating').value;
            const yearFrom = releaseYearFrom.value;
            const yearTo = releaseYearTo.value;

            const params = new URLSearchParams();
            genres.forEach(genre => params.append('genre', genre));
            authors.forEach(author => params.append('author', author));
            params.append('status', status);
            params.append('ageRating', ageRating);
            params.append('yearFrom', yearFrom);
            params.append('yearTo', yearTo);
            params.append('page', '1');

            fetch(`/filter-manga?${params.toString()}`)
                .then(response => response.text())
                .then(html => {
                document.getElementById('manga-content').innerHTML = html;
                checkIfNoManga();
                if (typeof window.updateStarRatings === 'function') {
                    window.updateStarRatings();
                }
                if (typeof window.initializeViewSwitch === 'function') {
                    window.initializeViewSwitch();
                }

            })
                .catch(error => console.error('Error fetching data:', error));
        });
    }
});